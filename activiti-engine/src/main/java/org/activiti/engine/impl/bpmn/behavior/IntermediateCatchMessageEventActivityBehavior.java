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
import java.util.Optional;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.apache.commons.lang3.StringUtils;

public class IntermediateCatchMessageEventActivityBehavior extends IntermediateCatchEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected final MessageEventDefinition messageEventDefinition;

  public IntermediateCatchMessageEventActivityBehavior(MessageEventDefinition messageEventDefinition) {
    this.messageEventDefinition = messageEventDefinition;
  }

  public void execute(DelegateExecution execution) {
    CommandContext commandContext = Context.getCommandContext();
    
    String messageName = getMessageName(execution);
    
    MessageEventSubscriptionEntity messageEvent = commandContext.getEventSubscriptionEntityManager()
                                                                .insertMessageEvent(messageName, 
                                                                                    ExecutionEntity.class.cast(execution));
    Optional<String> correlationKey = getCorrelationKey(execution);

    correlationKey.ifPresent(messageEvent::setConfiguration);
    
    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        commandContext.getProcessEngineConfiguration().getEventDispatcher()
                .dispatchEvent(ActivitiEventBuilder.createMessageWaitingEvent(execution,
                                                                              messageName,
                                                                              correlationKey.orElse(null)));
      }
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = deleteMessageEventSubScription(execution);
    leaveIntermediateCatchEvent(executionEntity);
  }
  
  @Override
  public void eventCancelledByEventGateway(DelegateExecution execution) {
    deleteMessageEventSubScription(execution);
    Context.getCommandContext().getExecutionEntityManager().deleteExecutionAndRelatedData((ExecutionEntity) execution, 
        DeleteReason.EVENT_BASED_GATEWAY_CANCEL, false);
  }

  protected ExecutionEntity deleteMessageEventSubScription(DelegateExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    String messageName = getMessageName(execution);
    
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      if (eventSubscription instanceof MessageEventSubscriptionEntity && eventSubscription.getEventName().equals(messageName)) {

        eventSubscriptionEntityManager.delete(eventSubscription);
      }
    }
    return executionEntity;
  }
  
  protected String getMessageName(DelegateExecution execution) {
      Expression messageExpression = null;
      
      if (StringUtils.isNotEmpty(messageEventDefinition.getMessageRef())) {
        messageExpression = getExpressionManager().createExpression(messageEventDefinition.getMessageRef());
      } else {
        messageExpression = getExpressionManager().createExpression(messageEventDefinition.getMessageExpression());
      }
      
      return messageExpression.getValue(execution)
                              .toString();

  }
  
  protected Optional<String> getCorrelationKey(DelegateExecution execution) {
      return Optional.ofNullable(messageEventDefinition.getCorrelationKey())
                     .map(correlationKey -> {
                          Expression expression = getExpressionManager().createExpression(messageEventDefinition.getCorrelationKey());

                          return expression.getValue(execution)
                                           .toString();
                     });    
  }
  
  protected ExpressionManager getExpressionManager() {
      return Context.getCommandContext()
                    .getProcessEngineConfiguration()
                    .getExpressionManager();
  }
  
}
