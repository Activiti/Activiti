package org.activiti.cycle.impl;

import java.lang.reflect.Method;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.annotations.Interceptors;

/**
 * Interceptor for cycle components. Supports a "before" and an "after" -
 * interceptor. Declared using the {@link Interceptors} annotation.
 * 
 * @see CycleComponent
 * @see CycleComponentFactory
 * @see Interceptors
 * 
 * @author daniel.meyer@camunda.com
 */
public interface Interceptor {

  /**
   * Interceptor-method called before the method-invocation. Implementors can
   * also block the method invocation by throwing an exception.
   * 
   * @param m
   *          the Method to be invoked
   * @param object
   *          the proxied object, the method is invoked on.
   * @param args
   *          array of parameter objects for the method invocation (can be null)
   */
  void beforeInvoke(Method m, Object object, Object... args); // TODO: throws
                                                              // Exception?

  /**
   * Interceptor-method called after the method-invocation.
   * 
   * @param m
   *          the Method to be invoked
   * @param object
   *          the object, the method is invoked on.
   * @param invocationResult
   *          the object returned by the method invocation (can be null).
   * @param args
   *          array of parameter objects for the method invocation (can be
   *          null).
   */
  void afterInvoke(Method m, Object object, Object invocationResult, Object... args); // TODO:
                                                                                      // throws
                                                                                      // Exception?
}
