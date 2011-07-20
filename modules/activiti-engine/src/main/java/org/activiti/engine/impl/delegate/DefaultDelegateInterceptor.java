package org.activiti.engine.impl.delegate;

import org.activiti.engine.impl.interceptor.DelegateInterceptor;

/**
 * Default implementation, simply proceeding the call. 
 * 
 * @author Daniel Meyer
 */
public class DefaultDelegateInterceptor implements DelegateInterceptor {

  public void handleInvocation(DelegateInvocation invocation) throws Exception {
    invocation.proceed();
  }

}
