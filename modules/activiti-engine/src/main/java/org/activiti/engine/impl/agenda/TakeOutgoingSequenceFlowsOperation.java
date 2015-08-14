package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.condition.ConditionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class TakeOutgoingSequenceFlowsOperation extends AbstractOperation {

  private static final Logger logger = LoggerFactory.getLogger(TakeOutgoingSequenceFlowsOperation.class);

  protected boolean evaluateConditions;

  public TakeOutgoingSequenceFlowsOperation(CommandContext commandContext, ActivityExecution activityExecution, boolean evaluateConditions) {
    super(commandContext, activityExecution);
    this.evaluateConditions = evaluateConditions;
  }

  @Override
  public void run() {
    FlowElement currentFlowElement = execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      currentFlowElement = findCurrentFlowElement(execution);
      execution.setCurrentFlowElement(currentFlowElement);
    }

    // If execution is a scope (and not the process instance), the scope must first be destroyed before we can continue
    if (execution.getParentId() != null && execution.isScope()) {
      agenda.planDestroyScopeOperation(execution);
    
    } else if (currentFlowElement instanceof Activity) {
      Activity activity = (Activity) currentFlowElement;
      if (CollectionUtils.isNotEmpty(activity.getBoundaryEvents())) {
        
        List<String> notToDeleteEvents = new ArrayList<String>();
        for (BoundaryEvent event : activity.getBoundaryEvents()) {
          if (CollectionUtils.isNotEmpty(event.getEventDefinitions()) && 
              event.getEventDefinitions().get(0) instanceof CancelEventDefinition) {
            
            notToDeleteEvents.add(event.getId());
          }
        }
        
        // Delete all child executions
        Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(execution.getId());
        for (ExecutionEntity childExecution : childExecutions) {
          if (childExecution.getCurrentFlowElement() == null || notToDeleteEvents.contains(childExecution.getCurrentFlowElement().getId()) == false) {
            commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(childExecution);
          }
        }
      }
    }
    
    // Execution listener for end: the flow node is now ended
    if (CollectionUtils.isNotEmpty(currentFlowElement.getExecutionListeners())
        && !execution.isProcessInstanceType()) { // a process instance execution can never leave a flownode, but it can pass here whilst cleaning up
      executeExecutionListeners(currentFlowElement, ExecutionListener.EVENTNAME_END);
    }
    
    // No scope, can continue
    if (currentFlowElement instanceof FlowNode) {
      
      FlowNode flowNode = (FlowNode) currentFlowElement;
      
      if (execution.getId().equals(execution.getProcessInstanceId()) == false) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPLETED, flowNode.getId(), flowNode.getName(),
                execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), flowNode));
      }
      
      leaveFlowNode(flowNode);
      
    } else if (currentFlowElement instanceof SequenceFlow) {
      // Nothing to do here. The operation wasn't really needed, so simply pass it through
      agenda.planContinueProcessOperation(execution);
    }
  }

  protected void leaveFlowNode(FlowNode flowNode) {

    logger.debug("Leaving flow node {} with id '{}' by following it's {} outgoing sequenceflow", flowNode.getClass(), flowNode.getId(), flowNode.getOutgoingFlows().size());

    // Get default sequence flow (if set)
    String defaultSequenceFlowId = null;
    if (flowNode instanceof Activity) {
      defaultSequenceFlowId = ((Activity) flowNode).getDefaultFlow();
    } else if (flowNode instanceof Gateway) {
      defaultSequenceFlowId = ((Gateway) flowNode).getDefaultFlow();
    }

    // Determine which sequence flows can be used for leaving
    List<SequenceFlow> outgoingSequenceFlow = new ArrayList<SequenceFlow>();
    for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {

      String skipExpressionString = sequenceFlow.getSkipExpression();
      if (!SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpressionString)) {

        if (!evaluateConditions
            || (evaluateConditions && ConditionUtil.hasTrueCondition(sequenceFlow, execution) && (defaultSequenceFlowId == null || !defaultSequenceFlowId.equals(sequenceFlow.getId())))) {
          outgoingSequenceFlow.add(sequenceFlow);
        }

      } else if (flowNode.getOutgoingFlows().size() == 1 || SkipExpressionUtil.shouldSkipFlowElement(commandContext, execution, skipExpressionString)) {
        // The 'skip' for a sequence flow means that we skip the condition, not the sequence flow.
        outgoingSequenceFlow.add(sequenceFlow);
      }
    }

    // Check if there is a default sequence flow
    if (outgoingSequenceFlow.size() == 0 && evaluateConditions) { // The elements that set this to false also have no support for default sequence flow
      if (defaultSequenceFlowId != null) {
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
          if (defaultSequenceFlowId.equals(sequenceFlow.getId())) {
            outgoingSequenceFlow.add(sequenceFlow);
            break;
          }
        }
      }
    }

    // No outgoing found. Ending the execution
    if (outgoingSequenceFlow.size() == 0) {
      if (flowNode.getOutgoingFlows() == null || flowNode.getOutgoingFlows().size() == 0) {
        logger.info("No outgoing sequence flow found for flow node '{}'.", flowNode.getId());
        
        if (flowNode.getSubProcess() != null && flowNode.getSubProcess() instanceof EventSubProcess) {
          
          ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
          executionEntityManager.deleteChildExecutions((ExecutionEntity) execution);
          executionEntityManager.deleteExecutionAndRelatedData((ExecutionEntity) execution);
          
          // event sub process nested in sub process
          if (flowNode.getSubProcess().getSubProcess() != null) {
            executionEntityManager.deleteChildExecutions((ExecutionEntity) execution.getParent());
            executionEntityManager.deleteExecutionAndRelatedData((ExecutionEntity) execution.getParent());
            ActivityExecution parentExecution = execution.getParent().getParent();
            parentExecution.setCurrentFlowElement(flowNode.getSubProcess().getSubProcess());
            agenda.planTakeOutgoingSequenceFlowsOperation(parentExecution);
          
          // event sub process on process root level
          } else {
            executionEntityManager.deleteChildExecutions((ExecutionEntity) execution.getParent());
            agenda.planEndExecutionOperation(execution.getParent());
          }
          
        } else {
          agenda.planEndExecutionOperation(execution);
        }
        
        return;
      } else {
        throw new ActivitiException("No outgoing sequence flow of element '" + flowNode.getId() + "' could be selected for continuing the process");
      }
    }
    
    // Leave, and reuse the incoming sequence flow, make executions for all the others (if applicable)

    ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
    List<ExecutionEntity> outgoingExecutions = new ArrayList<ExecutionEntity>(flowNode.getOutgoingFlows().size());

    // Reuse existing one
    SequenceFlow sequenceFlow = outgoingSequenceFlow.get(0);
    execution.setCurrentFlowElement(sequenceFlow);
    execution.setActive(true);
    // execution.setScope(false);
    outgoingExecutions.add((ExecutionEntity) execution);

    // Executions for all the other one
    if (outgoingSequenceFlow.size() > 1) {
      for (int i = 1; i < outgoingSequenceFlow.size(); i++) {

        ExecutionEntity outgoingExecutionEntity = new ExecutionEntity();
        outgoingExecutionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
        outgoingExecutionEntity.setProcessInstanceId(execution.getProcessInstanceId());
        outgoingExecutionEntity.setRootProcessInstanceId(execution.getRootProcessInstanceId());
        outgoingExecutionEntity.setTenantId(execution.getTenantId());

        outgoingExecutionEntity.setScope(false);
        outgoingExecutionEntity.setActive(true);

        outgoingExecutionEntity.setParentId(execution.getParentId() != null ? execution.getParentId() : execution.getId());

        sequenceFlow = outgoingSequenceFlow.get(i);
        outgoingExecutionEntity.setCurrentFlowElement(sequenceFlow);

        executionEntityManager.insert(outgoingExecutionEntity);
        outgoingExecutions.add(outgoingExecutionEntity);
      }
    }

    // Leave (only done when all executions have been made, since some queries depend on this)
    for (ExecutionEntity outgoingExecution : outgoingExecutions) {
      agenda.planContinueProcessOperation(outgoingExecution);
    }
  }

}
