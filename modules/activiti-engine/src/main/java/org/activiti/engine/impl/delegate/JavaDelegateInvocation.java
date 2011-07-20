package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * Class handling invocations of JavaDelegates
 * 
 * @author Daniel Meyer
 */
public class JavaDelegateInvocation extends DelegateInvocation {

  protected final JavaDelegate delegateInstance;
  protected final DelegateExecution execution;

  public JavaDelegateInvocation(JavaDelegate delegateInstance, DelegateExecution execution) {
    this.delegateInstance = delegateInstance;
    this.execution = execution;
  }

  protected void invoke() throws Exception {
    delegateInstance.execute((DelegateExecution) execution);
  }

}
