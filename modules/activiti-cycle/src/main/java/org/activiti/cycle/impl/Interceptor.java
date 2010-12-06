package org.activiti.cycle.impl;

import java.lang.reflect.Method;

/**
 * Generic interceptor interface.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface Interceptor {

  void interceptMethodCall(Method m, Object object, Object... args);

}
