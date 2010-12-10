package org.activiti.cycle.context;

import java.util.HashMap;

/**
 * The cycle application context, holds objects of scope
 * {@link CycleContextType#APPLICATION}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleApplicationContext {

  private static CycleContext wrappedContext = new CycleContext() {

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public void set(String key, Object value) {
      map.put(key, value);
    }

    public Object get(String key) {
      return map.get(key);
    }
  };

  public static void set(String key, Object value) {
    wrappedContext.set(key, value);
  }

  public static Object get(String key) {
    // TODO: restore discarded or un-initialized instances. 
    return wrappedContext.get(key);
  }

  public static void setWrappedContext(CycleContext context) {
    wrappedContext = context;
  }

}
