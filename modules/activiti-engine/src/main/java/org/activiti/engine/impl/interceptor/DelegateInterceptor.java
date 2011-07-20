package org.activiti.engine.impl.interceptor;

import org.activiti.engine.impl.delegate.DelegateInvocation;

/**
 * Interceptor responsible for handling calls to 'user code'. User code
 * represents external Java code (e.g. services and listeners). The following is
 * a list of classes that represent user code:
 * <ul>
 * <li>{@link org.activiti.engine.delegate.JavaDelegate}</li>
 * <li>{@link org.activiti.engine.delegate.ExecutionListener}</li>
 * <li>{@link org.activiti.engine.delegate.Expression}</li>
 * <li>{@link org.activiti.engine.delegate.TaskListener}</li>
 * </ul>
 * 
 * The interceptor is passed in an instance of {@link DelegateInvocation}.
 * Implementations are responsible for calling
 * {@link DelegateInvocation#proceed()} to make the call to the usercode.
 * 
 * @author Daniel Meyer
 */
public interface DelegateInterceptor {

  public void handleInvocation(DelegateInvocation invocation) throws Exception;  

}
