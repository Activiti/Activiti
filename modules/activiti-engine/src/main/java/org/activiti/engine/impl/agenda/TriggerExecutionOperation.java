package org.activiti.engine.impl.agenda;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.TriggerableActivityBehavior;

/**
 * @author Joram Barrez
 */
public class TriggerExecutionOperation extends AbstractOperation {

    public TriggerExecutionOperation(CommandContext commandContext, ActivityExecution execution) {
        super(commandContext, execution);
    }

    @Override
    public void run() {
        FlowElement currentFlowElement = execution.getCurrentFlowElement();

        if (currentFlowElement == null) {
            currentFlowElement = findCurrentFlowElement(execution);
        } else {
            execution.setCurrentActivityId(currentFlowElement.getId());
        }

        if (currentFlowElement instanceof FlowNode) {

            FlowNode flowNode = (FlowNode) currentFlowElement;
            ActivityBehavior activityBehavior = (ActivityBehavior) flowNode.getBehavior();
            if (activityBehavior instanceof TriggerableActivityBehavior) {
                ((TriggerableActivityBehavior) activityBehavior).trigger(execution, null, null);
            } else {
                throw new ActivitiException("Invalid behavior: " + activityBehavior + " should implement " + TriggerableActivityBehavior.class.getCanonicalName());
            }

        } else {
            throw new ActivitiException("Programmatic error: no current flow element found or invalid type: " + currentFlowElement + ". Halting.");
        }
    }

}
