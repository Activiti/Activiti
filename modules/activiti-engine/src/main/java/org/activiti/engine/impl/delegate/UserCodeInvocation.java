package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;

/**
 * Provides context about the invocation of usercode and handles the actual
 * invocation
 * 
 * @author Daniel Meyer
 * @see DelegateInterceptor
 */
public abstract class UserCodeInvocation {

  protected Object invocationResult;

  protected final ExecutionEntity execution;

  public UserCodeInvocation(ExecutionEntity execution) {
    this.execution = execution;
  }

  /**
   * make the invocation proceed, performing the actual invocation of the user
   * code.
   * 
   * @throws Exception
   *           the exception thrown by the user code
   */
  public void proceed() throws Exception {
    handleInvocation();
  }

  protected abstract void handleInvocation() throws Exception;

  /**
   * return the result of the invocation
   */
  public Object getInvocationResult() {
    return invocationResult;
  }

  /**
   * @return the current {@link Execution}
   */
  public ExecutionEntity getExecution() {
    return execution;
  }

}
