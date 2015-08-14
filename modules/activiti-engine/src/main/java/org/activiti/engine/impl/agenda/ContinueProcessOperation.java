package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.logging.LogMDC;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ContinueProcessOperation extends AbstractOperation {

  private static Logger logger = LoggerFactory.getLogger(ContinueProcessOperation.class);

  protected boolean forceSynchronousOperation;
  protected boolean inCompensation;

  public ContinueProcessOperation(CommandContext commandContext, ActivityExecution execution, 
      boolean forceSynchronousOperation, boolean inCompensation) {
    
    super(commandContext, execution);
    this.forceSynchronousOperation = forceSynchronousOperation;
    this.inCompensation = inCompensation;
  }

  public ContinueProcessOperation(CommandContext commandContext, ActivityExecution execution) {
    this(commandContext, execution, false, false);
  }

  @Override
  public void run() {

    FlowElement currentFlowElement = execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      currentFlowElement = findCurrentFlowElement(execution);
      execution.setCurrentFlowElement(currentFlowElement);
    }
    
    if (currentFlowElement instanceof FlowNode) {
    	
      // Check if it's the initial flow element. If so, we must fire the execution listeners for the process too
      FlowNode currentFlowNode = (FlowNode) currentFlowElement;
      if (currentFlowNode.getIncomingFlows() != null && currentFlowNode.getIncomingFlows().size() == 0 && currentFlowNode.getSubProcess() == null) {
    	  executeProcessStartExecutionListeners();
      }
    	
      continueThroughFlowNode(currentFlowNode);
      
    } else if (currentFlowElement instanceof SequenceFlow) {
      continueThroughSequenceFlow((SequenceFlow) currentFlowElement);
    } else {
      throw new RuntimeException("Programmatic error: no current flow element found or invalid type: " + currentFlowElement + ". Halting.");
    }

  }

  protected void executeProcessStartExecutionListeners() {
    org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(execution.getProcessDefinitionId());
    executeExecutionListeners(process, execution.getParent(), ExecutionListener.EVENTNAME_START, false);
  }

  protected void continueThroughFlowNode(FlowNode flowNode) {
    // See if flowNode is an async activity and schedule as a job if that evaluates to true
    if (!forceSynchronousOperation) {
      boolean isAsynchronous = flowNode.isAsynchronous();
      boolean isExclusive = flowNode.isExclusive();
      
      if (isAsynchronous) {
        scheduleJob(isExclusive);
        return;
      }
    }

    // Synchronous execution

    // Execution listener
    if (CollectionUtils.isNotEmpty(flowNode.getExecutionListeners())) {
      executeExecutionListeners(flowNode, ExecutionListener.EVENTNAME_START);
    }

    // Execute any boundary events
    if (inCompensation == false) {
      Collection<BoundaryEvent> boundaryEvents = findBoundaryEventsForFlowNode(execution.getProcessDefinitionId(), flowNode);
      if (CollectionUtils.isNotEmpty(boundaryEvents)) {
        executeBoundaryEvents(boundaryEvents, execution);
      }
    }

    // Execute actual behavior
    ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();
    
    if (activityBehavior != null) {
      logger.debug("Executing activityBehavior {} on activity '{}' with execution {}", activityBehavior.getClass(), flowNode.getId(), execution.getId());
      
      if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_STARTED, flowNode.getId(), flowNode.getName(), execution.getId(),
                execution.getProcessInstanceId(), execution.getProcessDefinitionId(), flowNode));
      }
      
      try {
        activityBehavior.execute(execution);
      } catch (RuntimeException e) {
        if (LogMDC.isMDCEnabled()) {
          LogMDC.putMDCExecution(execution);
        }
        throw e;
      }
    } else {
      logger.debug("No activityBehavior on activity '{}' with execution {}", flowNode.getId(), execution.getId());
      Context.getAgenda().planTakeOutgoingSequenceFlowsOperation(execution, true);
    }
  }

  protected void continueThroughSequenceFlow(SequenceFlow sequenceFlow) {

    // Execution listener
    if (CollectionUtils.isNotEmpty(sequenceFlow.getExecutionListeners())) {
      executeExecutionListeners(sequenceFlow, null, ExecutionListener.EVENTNAME_TAKE, true); // True -> any event type will be treated as 'take' for a sequence flow
    }
    
    // Firing event that transition is being taken       
    if(Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
      FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
        ActivitiEventBuilder.createSequenceFlowTakenEvent(
            (ExecutionEntity) execution,
            ActivitiEventType.SEQUENCEFLOW_TAKEN, 
            sequenceFlow.getId(),
            sourceFlowElement != null ? sourceFlowElement.getId() : null, 
            sourceFlowElement != null ? (String) sourceFlowElement.getName() : null, 
            sourceFlowElement != null ? sourceFlowElement.getClass().getName() : null,
            sourceFlowElement != null ? ((FlowNode) sourceFlowElement).getBehavior(): null,
            targetFlowElement != null ? targetFlowElement.getId() : null, 
            targetFlowElement != null ? targetFlowElement.getName() : null, 
            targetFlowElement != null ? targetFlowElement.getClass().getName() : null,
            targetFlowElement != null ? ((FlowNode) targetFlowElement).getBehavior(): null));
    }

    FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
    execution.setCurrentFlowElement(targetFlowElement);
    logger.debug("Sequence flow '{}' encountered. Continuing process by following it using execution {}", sequenceFlow.getId(), execution.getId());
    agenda.planContinueProcessOperation(execution);
  }

  protected void scheduleJob(boolean exclusive) {
    MessageEntity message = new MessageEntity();
    message.setExecutionId(execution.getId());
    message.setProcessInstanceId(execution.getProcessInstanceId());
    message.setProcessDefinitionId(execution.getProcessDefinitionId());
    message.setExclusive(exclusive);
    message.setJobHandlerType(AsyncContinuationJobHandler.TYPE);

    // Inherit tenant id (if applicable)
    if (execution.getTenantId() != null) {
      message.setTenantId(execution.getTenantId());
    }

    commandContext.getJobEntityManager().send(message);
  }

  protected void executeBoundaryEvents(Collection<BoundaryEvent> boundaryEvents, ActivityExecution execution) {

    // The parent execution becomes a scope, and a child execution is created for each of the boundary events
    for (BoundaryEvent boundaryEvent : boundaryEvents) {

      if (CollectionUtils.isEmpty(boundaryEvent.getEventDefinitions())) {
        continue;
      }
      
      if (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
        continue;
      }

      ExecutionEntity childExecutionEntity = (ExecutionEntity) execution.createExecution();
      childExecutionEntity.setParentId(execution.getId());
      childExecutionEntity.setCurrentFlowElement(boundaryEvent);
      childExecutionEntity.setScope(false);

      ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
      logger.debug("Executing boundary event activityBehavior {} with execution {}", boundaryEventBehavior.getClass(), childExecutionEntity.getId());
      boundaryEventBehavior.execute(childExecutionEntity);
    }

  }

  protected Collection<BoundaryEvent> findBoundaryEventsForFlowNode(final String processDefinitionId, final FlowNode flowNode) {
    Process process = getProcessDefinition(processDefinitionId);

    // This could be cached or could be done at parsing time
    List<BoundaryEvent> results = new ArrayList<BoundaryEvent>(1);
    Collection<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
    for (BoundaryEvent boundaryEvent : boundaryEvents) {
      if (boundaryEvent.getAttachedToRefId() != null && boundaryEvent.getAttachedToRefId().equals(flowNode.getId())) {
        results.add(boundaryEvent);
      }
    }
    return results;
  }

  protected Process getProcessDefinition(String processDefinitionId) {
    // TODO: must be extracted / cache should be accessed in another way
    return ProcessDefinitionUtil.getProcess(processDefinitionId);
  }
}
