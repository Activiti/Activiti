package org.activiti.osgi.blueprint.bean;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class ActivityBehaviourBean implements ActivityBehavior {
    @Override
    public void execute(ActivityExecution execution) throws Exception {
        execution.setVariable("visitedActivityBehaviour", true);
        execution.end();
    }
}
