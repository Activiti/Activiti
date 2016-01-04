/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmScope;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


/**
 * @author Daniel Meyer
 * @author Nico Rehwaldt
 * @author Joram Barrez
 */
public class ScopeUtil {
 
  /**
   * Find the next scope execution in the parent execution hierarchy
   * That method works different than {@link #findScopeExecutionForScope(org.activiti.engine.impl.persistence.entity.ExecutionEntity, org.activiti.engine.impl.pvm.PvmScope)} 
   * which returns the most outer scope execution.
   * 
   * @param execution the execution from which to start the search
   * @return the next scope execution in the parent execution hierarchy
   */
  public static ActivityExecution findScopeExecution(ActivityExecution execution) {
    
    while(execution.getParentId() != null && !execution.isScope()) {
      execution = execution.getParent();
    }
    
    if(execution != null && execution.isConcurrent()) {
      execution = execution.getParent();
    }
    
    return execution;
    
  }
  /**
   * returns the top-most execution sitting in an activity part of the scope defined by 'scopeActivitiy'.
   */
  public static ExecutionEntity findScopeExecutionForScope(ExecutionEntity execution, PvmScope scopeActivity) {
    
    // TODO: this feels hacky!
    
    if (scopeActivity instanceof PvmProcessDefinition) {
      return execution.getProcessInstance();
      
    } else {
      
      ActivityImpl currentActivity = execution.getActivity();      
      ExecutionEntity candiadateExecution = null;
      ExecutionEntity originalExecution = execution;
      
      while (execution != null) {
        currentActivity = execution.getActivity();
        if (scopeActivity.getActivities().contains(currentActivity) /* does not search rec*/ 
                || scopeActivity.equals(currentActivity)) {
          // found a candidate execution; lets still check whether we find an
          // execution which is also sitting in an activity part of this scope
          // higher up the hierarchy
          candiadateExecution = execution;        
        } else if (currentActivity!= null 
                && currentActivity.contains((ActivityImpl)scopeActivity) /*searches rec*/) {
          // now we're too "high", the candidate execution is the one.
          break;
        }
          
        execution = execution.getParent();
      }
      
      // if activity is scope, we need to get the parent at least:
      if(originalExecution == candiadateExecution 
              && originalExecution.getActivity().isScope() 
              && !originalExecution.getActivity().equals(scopeActivity)) {
        candiadateExecution = originalExecution.getParent();
      }      
      
      return candiadateExecution;
    }
  }
  
  public static ActivityImpl findInParentScopesByBehaviorType(ActivityImpl activity, Class<? extends ActivityBehavior> behaviorType) {
    while (activity != null) {
      for (ActivityImpl childActivity : activity.getActivities()) {
        if(behaviorType.isAssignableFrom(childActivity.getActivityBehavior().getClass())) {
          return childActivity;          
        }
      }
      activity = activity.getParentActivity();      
    }    
    return null;
  }

  /**
   * we create a separate execution for each compensation handler invocation. 
   */
  public static void throwCompensationEvent(List<CompensateEventSubscriptionEntity> eventSubscriptions, ActivityExecution execution, boolean async) {

    // first spawn the compensating executions
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      ExecutionEntity compensatingExecution = null;
      // check whether compensating execution is already created 
      // (which is the case when compensating an embedded subprocess, 
      // where the compensating execution is created when leaving the subprocess 
      // and holds snapshot data).
      if(eventSubscription.getConfiguration() !=null) {
        compensatingExecution = Context.getCommandContext()
          .getExecutionEntityManager()
          .findExecutionById(eventSubscription.getConfiguration());
        // move the compensating execution under this execution:
        compensatingExecution.setParent((InterpretableExecution) execution);        
        compensatingExecution.setEventScope(false);
      } else {
        compensatingExecution = (ExecutionEntity) execution.createExecution();
        eventSubscription.setConfiguration(compensatingExecution.getId());
      }
      compensatingExecution.setConcurrent(true);   
    }
    
    // signal compensation events in reverse order of their 'created' timestamp
    Collections.sort(eventSubscriptions, new Comparator<EventSubscriptionEntity>() {
      public int compare(EventSubscriptionEntity o1, EventSubscriptionEntity o2) {
        return o2.getCreated().compareTo(o1.getCreated());
      }
    });
    
    for (CompensateEventSubscriptionEntity compensateEventSubscriptionEntity : eventSubscriptions) {
      compensateEventSubscriptionEntity.eventReceived(null, async);      
    }
  }
  
  /**
   * creates an event scope for the given execution:
   * 
   * create a new event scope execution under the parent of the given 
   * execution and move all event subscriptions to that execution.
   * 
   * this allows us to "remember" the event subscriptions after finishing a 
   * scope
   */
  public static void createEventScopeExecution(ExecutionEntity execution) {

    ExecutionEntity eventScope = ScopeUtil.findScopeExecutionForScope(execution, execution.getActivity().getParent());
    
    List<CompensateEventSubscriptionEntity> eventSubscriptions = execution.getCompensateEventSubscriptions();
    
    if(!eventSubscriptions.isEmpty()) {
      
      ExecutionEntity eventScopeExecution = eventScope.createExecution();
      eventScopeExecution.setActive(false);      
      eventScopeExecution.setConcurrent(false);
      eventScopeExecution.setEventScope(true);      
      eventScopeExecution.setActivity((ActivityImpl) execution.getActivity());
      
      execution.setConcurrent(false);
      
      // copy local variables to eventScopeExecution by value. This way, 
      // the eventScopeExecution references a 'snapshot' of the local variables      
      Map<String, Object> variables = execution.getVariablesLocal();
      for (Entry<String, Object> variable : variables.entrySet()) {
        eventScopeExecution.setVariableLocal(variable.getKey(), variable.getValue());        
      }
      
      // set event subscriptions to the event scope execution:
      for (CompensateEventSubscriptionEntity eventSubscriptionEntity : eventSubscriptions) {
        eventSubscriptionEntity = eventSubscriptionEntity.moveUnder(eventScopeExecution);        
      }
            
      CompensateEventSubscriptionEntity eventSubscription = CompensateEventSubscriptionEntity.createAndInsert(eventScope);
      eventSubscription.setActivity(execution.getActivity());
      eventSubscription.setConfiguration(eventScopeExecution.getId());
      
    }
  }

}
