package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.logging.LogMDC;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ContinueMultiInstanceOperation extends AbstractOperation {

  private static Logger logger = LoggerFactory.getLogger(ContinueMultiInstanceOperation.class);

  public ContinueMultiInstanceOperation(CommandContext commandContext, ActivityExecution execution) {
    super(commandContext, execution);
  }

  @Override
  public void run() {

    FlowElement currentFlowElement = execution.getCurrentFlowElement();

    if (currentFlowElement == null) {
      currentFlowElement = findCurrentFlowElement(execution);
      execution.setCurrentFlowElement(currentFlowElement);
    }
    
    if (currentFlowElement instanceof FlowNode) {
      continueThroughFlowNode((FlowNode) currentFlowElement);
     
    } else {
      throw new RuntimeException("Programmatic error: no valid multi instance flow node, type: " + currentFlowElement + ". Halting.");
    }

  }

  protected void continueThroughFlowNode(FlowNode flowNode) {
    // Execution listener
    if (CollectionUtils.isNotEmpty(flowNode.getExecutionListeners())) {
      executeExecutionListeners(flowNode, ExecutionListener.EVENTNAME_START);
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
      } catch (BpmnError error) {
        // re-throw business fault so that it can be caught by an Error Intermediate Event or Error Event Sub-Process in the process
        ErrorPropagation.propagateError(error, execution);
      } catch (RuntimeException e) {
        if (LogMDC.isMDCEnabled()) {
          LogMDC.putMDCExecution(execution);
        }
        throw e;
      }
    } else {
      logger.debug("No activityBehavior on activity '{}' with execution {}", flowNode.getId(), execution.getId());
    }
  }
}
