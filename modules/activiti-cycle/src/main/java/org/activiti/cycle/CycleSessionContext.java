package org.activiti.cycle;

/**
 * Context for session-scoped variables. Wraps a {@link Context}-object and
 * stores it as a {@link ThreadLocal}, providing static access to it.
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleSessionContext {

  public static interface Context {

    public void set(String key, Object value);

    public Object get(String key);

  }

  private static ThreadLocal<Context> localContext = new ThreadLocal<CycleSessionContext.Context>();

  public static void setInCurrentContext(String key, Object value) {
    Context context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    context.set(key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFromCurrentContext(String key, Class<T> clazz) {
    Context context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    Object obj = context.get(key);
    if (obj != null) {
      return (T) obj;
    }
    return null;
  }

  public static <T> void setInCurrentContext(Class<T> key, Object value) {
    Context context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    context.set(key.getName(), value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getFromCurrentContext(Class<T> key) {
    Context context = localContext.get();
    if (context == null)
      throw new IllegalStateException("No context available");
    Object obj = context.get(key.getName());
    if (obj != null) {
      return (T) obj;
    }
    return null;
  }

  public static void setCurrentContext(Context context) {
    localContext.set(context);
  }

  public static void clearCurrentContext() {
    localContext.remove();
  }

}
