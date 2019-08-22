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

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.apache.commons.lang3.StringUtils;

/**

 */
public class BoundaryMessageEventActivityBehavior extends BoundaryEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected MessageEventDefinition messageEventDefinition;

  public BoundaryMessageEventActivityBehavior(MessageEventDefinition messageEventDefinition, boolean interrupting) {
    super(interrupting);
    this.messageEventDefinition = messageEventDefinition;
  }

  @Override
  public void execute(DelegateExecution execution) {
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    String messageName = getMessageName(execution);
    Optional<String> correlationKey = getCorrelationKey(execution);
    
    MessageEventSubscriptionEntity messageEvent = commandContext.getEventSubscriptionEntityManager()
                                                                .insertMessageEvent(messageName, 
                                                                                    executionEntity);
    correlationKey.ifPresent(messageEvent::setConfiguration);
    
    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        commandContext.getProcessEngineConfiguration().getEventDispatcher()
                .dispatchEvent(ActivitiEventBuilder.createMessageEvent(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, 
                                                                       executionEntity, 
                                                                       messageName,
                                                                       correlationKey.orElse(null), 
                                                                       null));
    }
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    BoundaryEvent boundaryEvent = (BoundaryEvent) execution.getCurrentFlowElement();
    String messageName = getMessageName(execution);

    if (boundaryEvent.isCancelActivity()) {
      EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
      List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
      for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
        if (eventSubscription instanceof MessageEventSubscriptionEntity && eventSubscription.getEventName().equals(messageName)) {

          eventSubscriptionEntityManager.delete(eventSubscription);
        }
      }
    }

    super.trigger(executionEntity, triggerName, triggerData);
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