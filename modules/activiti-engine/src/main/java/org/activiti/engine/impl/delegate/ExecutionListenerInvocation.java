package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * Class handling invocations of ExecutionListeners
 * 
 * @author Daniel Meyer
 */
public class ExecutionListenerInvocation extends DelegateInvocation {

  protected final ExecutionListener executionListenerInstance;
  protected final DelegateExecution execution;

  public ExecutionListenerInvocation(ExecutionListener executionListenerInstance, DelegateExecution execution) {
    this.executionListenerInstance = executionListenerInstance;
    this.execution = execution;
  }

  protected void invoke() throws Exception {
    executionListenerInstance.notify(execution);
  }

}
