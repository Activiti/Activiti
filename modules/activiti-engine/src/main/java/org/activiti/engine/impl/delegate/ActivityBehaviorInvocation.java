package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * 
 * @author Daniel Meyer
 */
public class ActivityBehaviorInvocation extends DelegateInvocation {

  protected final ActivityBehavior behaviorInstance;

  protected final ActivityExecution execution;

  public ActivityBehaviorInvocation(ActivityBehavior behaviorInstance, ActivityExecution execution) {
    this.behaviorInstance = behaviorInstance;
    this.execution = execution;
  }

  protected void invoke() throws Exception {
    behaviorInstance.execute(execution);
  }

}
