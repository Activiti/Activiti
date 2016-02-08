package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.TriggerableActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * @author Joram Barrez
 */
public class TriggerExecutionOperation extends AbstractOperation {

  public TriggerExecutionOperation(CommandContext commandContext, ExecutionEntity execution) {
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

      FlowNode flowNode = (FlowNode) currentFlowElement;
      ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();
      if (activityBehavior instanceof TriggerableActivityBehavior) {
        if (flowNode instanceof BoundaryEvent) {
          commandContext.getHistoryManager().recordActivityStart(execution);
        }
        
        ((TriggerableActivityBehavior) activityBehavior).trigger(execution, null, null);
        
        if (flowNode instanceof BoundaryEvent) {
          commandContext.getHistoryManager().recordActivityEnd(execution);
        }
        
      } else {
        throw new ActivitiException("Invalid behavior: " + activityBehavior + " should implement " + TriggerableActivityBehavior.class.getCanonicalName());
      }

    } else {
      throw new ActivitiException("Programmatic error: no current flow element found or invalid type: " + currentFlowElement + ". Halting.");
    }
  }

}
