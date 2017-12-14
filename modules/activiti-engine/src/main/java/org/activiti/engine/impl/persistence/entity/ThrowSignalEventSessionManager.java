package org.activiti.engine.impl.persistence.entity;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Keep track of fired events for process instance execution 
 * in the current transaction context 
 *
 */
public class ThrowSignalEventSessionManager extends AbstractManager {

  /** collect fired events in process instance scope in the current transaction context  */
  protected Map<String,Set<String>> firedSignalEventsSesssionRegistry = new ConcurrentHashMap<String, Set<String>>();
  
  private final Function<String,Set<String>> emptySetFunc = new Function<String,Set<String>>() {
      @Override
      public Set<String> apply(String t) {
        return new CopyOnWriteArraySet<String>();
      }
  };
  
  /**
   * Registers thrown signal events for execution in current transaction context 
   * 
   * @param execution current execution
   * @param eventName name of the event
   */
  public void registerThrowSignalEventByExecution(ActivityExecution execution, String eventName) {
    firedSignalEventsSesssionRegistry
      .computeIfAbsent( execution.getProcessInstanceId(), emptySetFunc)
      .add(eventName);
  }
  
  /**
   * Check signal events already registered in current transaction context 
   * 
   * @param execution current execution instance
   * @param eventName event name
   * @return true if event has been fired in the execution context
   */
  public boolean hasThrowSignalEventForExecution(ActivityExecution execution, String eventName) {
    return firedSignalEventsSesssionRegistry
        .computeIfAbsent(execution.getProcessInstanceId(), emptySetFunc)
        .contains(eventName);
  }
}