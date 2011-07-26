package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.interceptor.DelegateInterceptor;

/**
 * Provides context about the invocation of usercode and handles the actual
 * invocation
 * 
 * @author Daniel Meyer
 * @see DelegateInterceptor
 */
public abstract class DelegateInvocation {

  protected Object invocationResult;
  protected Object[] invocationParameters;

  /**
   * make the invocation proceed, performing the actual invocation of the user
   * code.
   * 
   * @throws Exception
   *           the exception thrown by the user code
   */
  public void proceed() throws Exception {
    invoke();
  }

  protected abstract void invoke() throws Exception;

  /**
   * @return the result of the invocation (can be null if the invocation does
   *         not return a result)
   */
  public Object getInvocationResult() {
    return invocationResult;
  }

  /**
   * @return an array of invocation parameters (null if the invocation takes no
   *         parameters)
   */
  public Object[] getInvocationParameters() {
    return invocationParameters;
  }
  
  /**
   * returns the target of the current invocation, ie. JavaDelegate, ValueExpression ... 
   */
  public abstract Object getTarget();

}
