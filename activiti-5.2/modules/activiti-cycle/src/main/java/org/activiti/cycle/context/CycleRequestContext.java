package org.activiti.cycle.context;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.CycleComponentFactory.CycleComponentDescriptor;

/**
 * The cycle request context, scoped to a single user request.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleRequestContext {

  private static ThreadLocal<CycleContext> localContext = new ThreadLocal<CycleContext>();

  public static void set(String key, Object value) {
    CycleContext context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    context.set(key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(String key, Class<T> clazz) {
    Object obj = get(key);
    if (obj == null) {
      return null;
    }
    return (T) obj;
  }

  public static Object get(String key) {
    CycleContext context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    Object obj = context.get(key);
    if (obj == null) {
      // try to restore component instance using the
      CycleComponentDescriptor descriptor = CycleComponentFactory.getCycleComponentDescriptor(key);
      if (descriptor != null)
        if (descriptor.contextType.equals(CycleContextType.REQUEST)) {
          // note: adds obj to the context
          obj = CycleComponentFactory.getCycleComponentInstance(key);
        }
    }
    return obj;
  }

  public static <T> void set(Class<T> key, Object value) {
    set(key.getCanonicalName(), value);
  }

  public static <T> T get(Class<T> key) {
    return get(key.getCanonicalName(), key);
  }

  public static CycleContext getLocalContext() {
    return localContext.get();
  }

  public static void setContext(CycleContext context) {
    localContext.set(context);
  }

  public static void clearContext() {
    localContext.remove();
  }
}
