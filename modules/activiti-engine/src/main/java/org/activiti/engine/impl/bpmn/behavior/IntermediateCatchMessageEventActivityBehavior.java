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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

public class IntermediateCatchMessageEventActivityBehavior extends IntermediateCatchEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected MessageEventDefinition messageEventDefinition;

  public IntermediateCatchMessageEventActivityBehavior(MessageEventDefinition messageEventDefinition) {
    this.messageEventDefinition = messageEventDefinition;
  }

  public void execute(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    Context.getCommandContext().getEventSubscriptionEntityManager().insertMessageEvent(messageEventDefinition, executionEntity);
  }

  @Override
  public void trigger(ActivityExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = deleteMessageEventSubScription(execution);
    leaveIntermediateCatchEvent(executionEntity);
  }
  
  @Override
  public void cancelEvent(ActivityExecution execution) {
    deleteMessageEventSubScription(execution);
    Context.getCommandContext().getExecutionEntityManager().deleteExecutionAndRelatedData((ExecutionEntity) execution);
  }

  protected ExecutionEntity deleteMessageEventSubScription(ActivityExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      if (eventSubscription instanceof MessageEventSubscriptionEntity && eventSubscription.getEventName().equals(messageEventDefinition.getMessageRef())) {

        eventSubscriptionEntityManager.deleteEventSubscription(eventSubscription);
      }
    }
    return executionEntity;
  }
}
