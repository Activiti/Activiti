package org.activiti.cycle.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.activiti.cycle.annotations.Interceptors;

/**
 * The {@link CycleComponentInvocationHandler} intercepts method calls to cycle
 * components using the set of interceptors declared using the
 * {@link Interceptors}-annotation on the component.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleComponentInvocationHandler implements InvocationHandler {

  private final Interceptor[] interceptorInstances;

  private final Object instance;

  public CycleComponentInvocationHandler(Object instance, Interceptor[] interceptorInstances) {
    this.interceptorInstances = interceptorInstances;
    this.instance = instance;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // before method invocation
    for (Interceptor interceptor : interceptorInstances) {
      interceptor.beforeInvoke(method, instance, args); // throws exception
    }

    Object invocationResult = null;
    try {
      invocationResult = method.invoke(instance, args);
    } catch (Exception e) {
      // catch the InvocationTargetException and throw the actual exception
      // instead.
      if (e instanceof InvocationTargetException) {
        throw ((InvocationTargetException) e).getTargetException();
      }
      throw e;
    }

    // after method invocation
    for (Interceptor interceptor : interceptorInstances) {
      interceptor.afterInvoke(method, instance, invocationResult, args);
    }
    return invocationResult;

  }
}
