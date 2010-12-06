package org.activiti.cycle.context;

/**
 * Context interface for Cycle Contexts. Used to acces Key/Value stores. We use
 * this interface to abstract form concrete contexts such as the http-session
 * context.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleContext {

  public void set(String key, Object value);

  public Object get(String key);

}
