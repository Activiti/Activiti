package org.activiti.engine.impl.persistence.entity;


import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
  
  /**
   * Registers thrown signal events for execution in current transaction context 
   * 
   * @param execution current execution
   * @param eventName name of the event
   */
  public void registerThrowSignalEventByExecution(ActivityExecution execution, String eventName) {
    String processInstanceId = execution.getProcessInstanceId();
    
    Set<String> values=firedSignalEventsSesssionRegistry.get(processInstanceId);
    if(values == null) {
        values=new LinkedHashSet<String>();
        
        Set<String> tmp=firedSignalEventsSesssionRegistry.putIfAbsent(processInstanceId, values);
        if(tmp != null)
            values=tmp;
    }

    values.add(eventName);
    
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
        .getOrDefault(execution.getProcessInstanceId(), new LinkedHashSet<String>(0))
        .contains(eventName);
  }
  
}