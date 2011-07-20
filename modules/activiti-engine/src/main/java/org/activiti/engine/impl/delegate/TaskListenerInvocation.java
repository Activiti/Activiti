package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Class handling invocations of {@link TaskListener TaskListeners}
 * 
 * @author Daniel Meyer
 */
public class TaskListenerInvocation extends DelegateInvocation {

  protected final TaskListener executionListenerInstance;
  protected final DelegateTask delegateTask;

  public TaskListenerInvocation(TaskListener executionListenerInstance, DelegateTask delegateTask) {
    this.executionListenerInstance = executionListenerInstance;
    this.delegateTask = delegateTask;
  }

  protected void invoke() throws Exception {
    executionListenerInstance.notify(delegateTask);
  }

}
