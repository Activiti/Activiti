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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author Tijs Rademakers
 */
public class ScopeUtil {

  /**
   * we create a separate execution for each compensation handler invocation.
   */
  public static void throwCompensationEvent(List<CompensateEventSubscriptionEntity> eventSubscriptions, ActivityExecution execution, boolean async) {

    // first spawn the compensating executions
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      ExecutionEntity compensatingExecution = null;
      // check whether compensating execution is already created (which is the case when compensating an embedded subprocess,
      // where the compensating execution is created when leaving the subprocess and holds snapshot data).
      if (eventSubscription.getConfiguration() != null) {
        compensatingExecution = Context.getCommandContext().getExecutionEntityManager().findExecutionById(eventSubscription.getConfiguration());
        // move the compensating execution under this execution:
        compensatingExecution.setParent((ExecutionEntity) execution);
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
   * Creates a new event scope execution and moves existing event subscriptions to this new execution
   */
  public static void createCopyOfSubProcessExecutionForCompensation(ExecutionEntity subProcessExecution, ExecutionEntity parentScopeExecution) {
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findEventSubscriptionsByExecutionAndType(subProcessExecution.getId(), "compensate");
    
    List<CompensateEventSubscriptionEntity> compensateEventSubscriptions = new ArrayList<CompensateEventSubscriptionEntity>();
    for (EventSubscriptionEntity event : eventSubscriptions) {
      if (event instanceof CompensateEventSubscriptionEntity) {
        compensateEventSubscriptions.add((CompensateEventSubscriptionEntity) event);
      }
    }

    if (CollectionUtils.isNotEmpty(compensateEventSubscriptions)) {
      ExecutionEntity eventScopeExecution = parentScopeExecution.createExecution();
      eventScopeExecution.setActive(false);
      eventScopeExecution.setConcurrent(false);
      eventScopeExecution.setEventScope(true);
      eventScopeExecution.setCurrentFlowElement(subProcessExecution.getCurrentFlowElement());

      // copy local variables to eventScopeExecution by value. This way,
      // the eventScopeExecution references a 'snapshot' of the local variables
      Map<String, Object> variables = subProcessExecution.getVariablesLocal();
      for (Entry<String, Object> variable : variables.entrySet()) {
        eventScopeExecution.setVariableLocal(variable.getKey(), variable.getValue());
      }

      // set event subscriptions to the event scope execution:
      for (CompensateEventSubscriptionEntity eventSubscriptionEntity : compensateEventSubscriptions) {
        eventSubscriptionEntityManager.delete(eventSubscriptionEntity);

        CompensateEventSubscriptionEntity newSubscription = eventSubscriptionEntityManager.insertCompensationEvent(
            eventScopeExecution, eventSubscriptionEntity.getActivityId());
        newSubscription.setConfiguration(eventSubscriptionEntity.getConfiguration());
        newSubscription.setCreated(eventSubscriptionEntity.getCreated());
      }

      CompensateEventSubscriptionEntity eventSubscription = eventSubscriptionEntityManager.insertCompensationEvent(
          parentScopeExecution, eventScopeExecution.getCurrentFlowElement().getId());
      eventSubscription.setConfiguration(eventScopeExecution.getId());
    }
  }
}
