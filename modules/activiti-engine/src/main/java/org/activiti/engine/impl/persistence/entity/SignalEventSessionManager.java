package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Keep track of fired events for process instance execution 
 * in the current command context session and historic activity instance 
 *
 */
public class SignalEventSessionManager extends AbstractManager {

  /** collect fired events in process instance scope in the current command context session */
  protected Map<String,Set<String>> firedSignalEventsSesssionRegistry = new HashMap<String, Set<String>>();
  
  /**
   * Registers thrown signal events for execution in current session 
   * 
   * @param execution current execution
   * @param eventName name of the event
   */
  public void registerThrowSignalEventByExecution(ActivityExecution execution, String eventName) {
    String processInstanceId = execution.getProcessInstanceId();
    
    synchronized (firedSignalEventsSesssionRegistry) {
      Set<String> events = firedSignalEventsSesssionRegistry
          .getOrDefault(processInstanceId, new LinkedHashSet<String>());

      events.add(eventName);

      firedSignalEventsSesssionRegistry.put(processInstanceId, events);
    }
    
  }
  
  /**
   * Check signal events already registered in current session command context 
   * or recorded history for the process instance scope. 
   * Requires full audit level to query historic activity events 
   * 
   * @param execution current execution instance
   * @param eventName event name
   * @return true if event has been fired in the execution context
   * @throws Exception
   */
  public boolean hasThrowSignalEventForExecution(ActivityExecution execution, String eventName) throws Exception {
  
    // Check signal events already registered in current session command context 
    if(mayBeHasThrowSignalEventsInCurrentSession(execution.getProcessInstanceId(), eventName)) {
      return true;
    } // Then check signal events in recorded history for the process instance scope
    else if(mayBeHasThrowSignalEventsInHistoricActivityInstance(execution, eventName)) {
      return true;
    }

    return false;
  }
    
  protected boolean mayBeHasThrowSignalEventsInCurrentSession(String processInstanceId, String eventName) {
    return firedSignalEventsSesssionRegistry
      .getOrDefault(processInstanceId, new LinkedHashSet<String>(0))
      .contains(eventName);
  }

  protected boolean mayBeHasThrowSignalEventsInHistoricActivityInstance(ActivityExecution execution, String eventName) {
    boolean hasThrowSignalEvent = false;

    // check signal events in recorded history for the process instance scope 
    CommandContext commandContext = Context.getCommandContext();

    HistoricActivityInstanceQueryImpl activityInstanceQuery = new HistoricActivityInstanceQueryImpl(commandContext)
     .processInstanceId(execution.getProcessInstanceId())
     .activityType("intermediateSignalThrow");

    List<HistoricActivityInstance> thrownSignalActivities = commandContext.getHistoricActivityInstanceEntityManager()
      .findHistoricActivityInstancesByQueryCriteria(activityInstanceQuery, null);

    for(HistoricActivityInstance thrownSignal : thrownSignalActivities) {
      
      if(execution instanceof ExecutionEntity) {
        ExecutionEntity executionEntity = (ExecutionEntity) execution;

        ActivityBehavior activityBehavior = executionEntity.getProcessDefinition()
         .findActivity(thrownSignal.getActivityId())
         .getActivityBehavior();

        if(activityBehavior != null && activityBehavior instanceof IntermediateThrowSignalEventActivityBehavior) {
          IntermediateThrowSignalEventActivityBehavior intermediateThrowSignalEventBehavior = 
              (IntermediateThrowSignalEventActivityBehavior) activityBehavior;

          if(intermediateThrowSignalEventBehavior.isProcessInstanceScope()) {
            String thrownSignalEventName = intermediateThrowSignalEventBehavior.getSignalDefinition().getEventName();  

            // Cache events from history in the current session context to optimize performance 
            registerThrowSignalEventByExecution(execution, thrownSignalEventName);

            // Match registered signal event name with subscription event name
            if(thrownSignalEventName.equals(eventName) && hasThrowSignalEvent == false) {
              hasThrowSignalEvent = true;
            }
          }
        }
      }
    }

    return hasThrowSignalEvent;
  }
  

}
