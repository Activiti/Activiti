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

package org.activiti.engine.impl.event;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * @author Daniel Meyer
 * @author Falko Menge
 */
public abstract class AbstractEventHandler implements EventHandler {

  public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {

    ExecutionEntity execution = eventSubscription.getExecution();
    ActivityImpl activity = eventSubscription.getActivity();

    if (activity == null) {
      throw new ActivitiException("Error while sending signal for event subscription '" + eventSubscription.getId() + "': "
              + "no activity associated with event subscription");
    }

    if (payload instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> processVariables = (Map<String, Object>) payload;
      execution.setVariables(processVariables);
    }

    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    if (activityBehavior instanceof BoundaryEventActivityBehavior
            || activityBehavior instanceof EventSubProcessStartEventActivityBehavior) {

      try {

        dispatchActivitiesCanceledIfNeeded(eventSubscription, execution, activity, commandContext);

        activityBehavior.execute(execution);

      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new ActivitiException("exception while sending signal for event subscription '" + eventSubscription + "':" + e.getMessage(), e);
      }

    } else { // not boundary
      if (!activity.equals( execution.getActivity() )) {
        execution.setActivity(activity);
      }
      execution.signal(eventSubscription.getEventName(), payload);
    }
  }

  protected void dispatchActivitiesCanceledIfNeeded(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, ActivityImpl boundaryEventActivity, CommandContext commandContext) {
    ActivityBehavior boundaryActivityBehavior = boundaryEventActivity.getActivityBehavior();
    if (boundaryActivityBehavior instanceof BoundaryEventActivityBehavior) {
      BoundaryEventActivityBehavior boundaryEventActivityBehavior = (BoundaryEventActivityBehavior) boundaryActivityBehavior;
      if (boundaryEventActivityBehavior.isInterrupting()) {
        dispatchExecutionCancelled(eventSubscription, execution, commandContext);
      }
    }
  }

  protected void dispatchExecutionCancelled(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, CommandContext commandContext) {
    // subprocesses
    for (ExecutionEntity subExecution : execution.getExecutions()) {
      dispatchExecutionCancelled(eventSubscription, subExecution, commandContext);
    }

    // call activities
    ExecutionEntity subProcessInstance = commandContext.getExecutionEntityManager().findSubProcessInstanceBySuperExecutionId(execution.getId());
    if (subProcessInstance != null) {
      dispatchExecutionCancelled(eventSubscription, subProcessInstance, commandContext);
    }

    // activity with message/signal boundary events
    ActivityImpl activity = execution.getActivity();
    if (activity != null && activity.getActivityBehavior() != null) {
      dispatchActivityCancelled(eventSubscription, execution, activity, commandContext);
    }
  }

  protected void dispatchActivityCancelled(EventSubscriptionEntity eventSubscription, ExecutionEntity execution, ActivityImpl activity, CommandContext commandContext) {
    commandContext.getEventDispatcher().dispatchEvent(
      ActivitiEventBuilder.createActivityCancelledEvent(activity.getId(),
        (String) activity.getProperties().get("name"),
        execution.getId(),
        execution.getProcessInstanceId(), execution.getProcessDefinitionId(),
        (String) activity.getProperties().get("type"),
        activity.getActivityBehavior().getClass().getCanonicalName(),
        eventSubscription)
    );
  }

}
