package org.activiti.osgi.blueprint.bean;

import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;

public class ActivityBehaviourBean implements ActivityBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void execute(ActivityExecution execution) {
    execution.setVariable("visitedActivityBehaviour", true);
    execution.end();
  }
}
