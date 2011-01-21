package org.activiti.cycle.context;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.CycleComponentFactory.CycleComponentDescriptor;

/**
 * The cycle application context, holds objects of scope
 * {@link CycleContextType#APPLICATION}.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleApplicationContext {

  private static CycleContext wrappedContext =  new CycleMapContext();

  public static void set(String key, Object value) {
    wrappedContext.set(key, value);
  }

  public static Object get(String key) {
    Object obj = wrappedContext.get(key);
    if (obj == null) {
      // try to restore component instance using the
      CycleComponentDescriptor descriptor = CycleComponentFactory.getCycleComponentDescriptor(key);
      if (descriptor != null)
        if (descriptor.contextType.equals(CycleContextType.APPLICATION)) {
          // note: adds obj to ApplicationContext
          obj = CycleComponentFactory.getCycleComponentInstance(key);
        }
    }
    return obj;
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String key, Class<T> clazz) {
    Object obj = get(key);
    if (obj == null) {
      return null;
    }
    return (T) obj;
  }

  public static <T> T get(Class<T> key) {
    return get(key.getCanonicalName(), key);
  }

  public static void setWrappedContext(CycleContext context) {
    wrappedContext = context;
  }

  public static CycleContext getWrappedContext() {
    return wrappedContext;
  }

}
