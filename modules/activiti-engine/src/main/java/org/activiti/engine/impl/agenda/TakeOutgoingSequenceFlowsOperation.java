package org.activiti.engine.impl.agenda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.AdhocSubProcess;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.impl.util.condition.ConditionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class TakeOutgoingSequenceFlowsOperation extends AbstractOperation {

  private static final Logger logger = LoggerFactory.getLogger(TakeOutgoingSequenceFlowsOperation.class);

  protected boolean evaluateConditions;

  public TakeOutgoingSequenceFlowsOperation(CommandContext commandContext, ExecutionEntity activityExecution, boolean evaluateConditions) {
    super(commandContext, activityExecution);
    this.evaluateConditions = evaluateConditions;
  }

  @Override
  public void run() {
    FlowElement currentFlowElement = execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      currentFlowElement = findCurrentFlowElement(execution);
    }

    // If execution is a scope (and not the process instance), the scope must first be destroyed before we can continue
    if (execution.getParentId() != null && execution.isScope()) {
      agenda.planDestroyScopeOperation(execution);
    
    } else if (currentFlowElement instanceof Activity) {
      Activity activity = (Activity) currentFlowElement;
      if (CollectionUtil.isNotEmpty(activity.getBoundaryEvents())) {
        
        List<String> notToDeleteEvents = new ArrayList<String>();
        for (BoundaryEvent event : activity.getBoundaryEvents()) {
          if (CollectionUtil.isNotEmpty(event.getEventDefinitions()) && 
              event.getEventDefinitions().get(0) instanceof CancelEventDefinition) {
            
            notToDeleteEvents.add(event.getId());
          }
        }
        
        // Delete all child executions
        Collection<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(execution.getId());
        for (ExecutionEntity childExecution : childExecutions) {
          if (childExecution.getCurrentFlowElement() == null || notToDeleteEvents.contains(childExecution.getCurrentFlowElement().getId()) == false) {
            commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(childExecution, null, false);
          }
        }
      }
    }
    
    // Execution listener for end: the flow node is now ended
    if (CollectionUtil.isNotEmpty(currentFlowElement.getExecutionListeners())
        && !execution.isProcessInstanceType()) { // a process instance execution can never leave a flow node, but it can pass here whilst cleaning up
      executeExecutionListeners(currentFlowElement, ExecutionListener.EVENTNAME_END);
    }
    
    // a process instance execution can never leave a flow node, but it can pass here whilst cleaning up
    if (!execution.isProcessInstanceType() && currentFlowElement instanceof SequenceFlow == false) {
      commandContext.getHistoryManager().recordActivityEnd(execution);
    }
    
    // No scope, can continue
    if (currentFlowElement instanceof FlowNode) {
      
      FlowNode flowNode = (FlowNode) currentFlowElement;
      
      if (execution.getId().equals(execution.getProcessInstanceId()) == false && execution.getCurrentFlowElement() instanceof SubProcess == false) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
            ActivitiEventBuilder.createActivityEvent(ActivitiEventType.ACTIVITY_COMPLETED, flowNode.getId(), flowNode.getName(),
                execution.getId(), execution.getProcessInstanceId(), execution.getProcessDefinitionId(), flowNode));
      }
      
      if (flowNode.getParentContainer() != null && flowNode.getParentContainer() instanceof AdhocSubProcess) {
        
        boolean completeAdhocSubProcess = false;
        AdhocSubProcess adhocSubProcess = (AdhocSubProcess) flowNode.getParentContainer();
        if (adhocSubProcess.getCompletionCondition() != null) {
          Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(adhocSubProcess.getCompletionCondition());
          Condition condition = new UelExpressionCondition(expression);
          if (condition.evaluate(adhocSubProcess.getId(), execution)) {
            completeAdhocSubProcess = true;
          }
        }
        
        if (flowNode.getOutgoingFlows().size() > 0) {
          leaveFlowNode(flowNode);
        } else {
          commandContext.getExecutionEntityManager().deleteExecutionAndRelatedData(execution, null, false);
        }
        
        if (completeAdhocSubProcess) {
          boolean endAdhocSubProcess = true;
          if (adhocSubProcess.isCancelRemainingInstances() == false) {
            List<ExecutionEntity> childExecutions = commandContext.getExecutionEntityManager().findChildExecutionsByParentExecutionId(execution.getParentId());
            for (ExecutionEntity executionEntity : childExecutions) {
              if (executionEntity.getId().equals(execution.getId()) == false) {
                endAdhocSubProcess = false;
                break;
              }
            }
          }
          
          if (endAdhocSubProcess) {
            agenda.planEndExecutionOperation(execution.getParent());
          }
        }
      
      } else {
        leaveFlowNode(flowNode);
      }
      
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
    List<SequenceFlow> outgoingSequenceFlows = new ArrayList<SequenceFlow>();
    for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {

      String skipExpressionString = sequenceFlow.getSkipExpression();
      if (!SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpressionString)) {

        if (!evaluateConditions
            || (evaluateConditions && ConditionUtil.hasTrueCondition(sequenceFlow, execution) && (defaultSequenceFlowId == null || !defaultSequenceFlowId.equals(sequenceFlow.getId())))) {
          outgoingSequenceFlows.add(sequenceFlow);
        }

      } else if (flowNode.getOutgoingFlows().size() == 1 || SkipExpressionUtil.shouldSkipFlowElement(commandContext, execution, skipExpressionString)) {
        // The 'skip' for a sequence flow means that we skip the condition, not the sequence flow.
        outgoingSequenceFlows.add(sequenceFlow);
      }
    }

    // Check if there is a default sequence flow
    if (outgoingSequenceFlows.size() == 0 && evaluateConditions) { // The elements that set this to false also have no support for default sequence flow
      if (defaultSequenceFlowId != null) {
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
          if (defaultSequenceFlowId.equals(sequenceFlow.getId())) {
            outgoingSequenceFlows.add(sequenceFlow);
            break;
          }
        }
      }
    }

    // No outgoing found. Ending the execution
    if (outgoingSequenceFlows.size() == 0) {
      if (flowNode.getOutgoingFlows() == null || flowNode.getOutgoingFlows().size() == 0) {
        logger.debug("No outgoing sequence flow found for flow node '{}'.", flowNode.getId());
        
        agenda.planEndExecutionOperation(execution);
        
      } else {
        throw new ActivitiException("No outgoing sequence flow of element '" + flowNode.getId() + "' could be selected for continuing the process");
      }
    
    } else {
    
      // Leave, and reuse the incoming sequence flow, make executions for all the others (if applicable)
  
      ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
      List<ExecutionEntity> outgoingExecutions = new ArrayList<ExecutionEntity>(flowNode.getOutgoingFlows().size());
  
      SequenceFlow sequenceFlow = outgoingSequenceFlows.get(0);
      
      // Reuse existing one
      execution.setCurrentFlowElement(sequenceFlow);
      execution.setActive(true);
      outgoingExecutions.add((ExecutionEntity) execution);
  
      // Executions for all the other one
      if (outgoingSequenceFlows.size() > 1) {
        for (int i = 1; i < outgoingSequenceFlows.size(); i++) {
  
          ExecutionEntity outgoingExecutionEntity = commandContext.getExecutionEntityManager().create(); 
          outgoingExecutionEntity.setProcessDefinitionId(execution.getProcessDefinitionId());
          outgoingExecutionEntity.setProcessInstanceId(execution.getProcessInstanceId());
          outgoingExecutionEntity.setRootProcessInstanceId(execution.getRootProcessInstanceId());
          outgoingExecutionEntity.setTenantId(execution.getTenantId());
  
          outgoingExecutionEntity.setScope(false);
          outgoingExecutionEntity.setActive(true);
  
          ExecutionEntity parent = execution.getParentId() != null ? execution.getParent() : execution; 
          outgoingExecutionEntity.setParent(parent);
          parent.addChildExecution(outgoingExecutionEntity);
          
          SequenceFlow outgoingSequenceFlow = outgoingSequenceFlows.get(i);
          outgoingExecutionEntity.setCurrentFlowElement(outgoingSequenceFlow);
  
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

}
