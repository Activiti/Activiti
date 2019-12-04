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
package org.activiti.engine.impl.bpmn.parser.factory;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import java.util.Map;
import java.util.Optional;

public class DefaultMessageExecutionContext implements MessageExecutionContext {
    private final ExpressionManager expressionManager;
    private final MessagePayloadMappingProvider messagePayloadMappingProvider;
    private final MessageEventDefinition messageEventDefinition;

    public DefaultMessageExecutionContext(MessageEventDefinition messageEventDefinition,
                                          ExpressionManager expressionManager,
                                          MessagePayloadMappingProvider messagePayloadMappingProvider) {
        this.messageEventDefinition = messageEventDefinition;
        this.expressionManager = expressionManager;
        this.messagePayloadMappingProvider = messagePayloadMappingProvider;
    }    
    
    @Override
    public String getMessageName(DelegateExecution execution) {
        return evaluateExpression(Optional.ofNullable(messageEventDefinition.getMessageRef())
                                          .orElseGet(() -> messageEventDefinition.getMessageExpression()),
                                  execution);
    }

    public Optional<String> getCorrelationKey(DelegateExecution execution) {
        return Optional.ofNullable(messageEventDefinition.getCorrelationKey())
                       .map(correlationKey -> {
                           return evaluateExpression(messageEventDefinition.getCorrelationKey(),
                                                     execution);
                       });
    }
    
    
    
    public Optional<Map<String, Object>> getMessagePayload(DelegateExecution execution) {
        return messagePayloadMappingProvider.getMessagePayload(execution);
    }
    
    @Override
    public ThrowMessage createThrowMessage(DelegateExecution execution) {
        String name = getMessageName(execution);
        Optional<String> correlationKey = getCorrelationKey(execution);
        Optional<String> businessKey = Optional.ofNullable(execution.getProcessInstanceBusinessKey());
        Optional<Map<String, Object>> payload = getMessagePayload(execution);
        
        return ThrowMessage.builder()
                           .name(name)
                           .correlationKey(correlationKey)
                           .businessKey(businessKey)
                           .payload(payload)
                           .build();
    }    
    
    @Override
    public MessageEventSubscriptionEntity createMessageEventSubscription(CommandContext commandContext,
                                                                         DelegateExecution execution) {
        
        String messageName = getMessageName(execution);
        Optional<String> correlationKey = getCorrelationKey(execution); 

        correlationKey.ifPresent(key -> assertNoExistingDuplicateEventSubscriptions(messageName,
                                                                                    key,
                                                                                    commandContext));
        
        MessageEventSubscriptionEntity messageEvent = commandContext.getEventSubscriptionEntityManager()
                                                                    .insertMessageEvent(messageName,
                                                                                        ExecutionEntity.class.cast(execution));
        correlationKey.ifPresent(messageEvent::setConfiguration);
        
        return messageEvent;
    }

    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }

    public MessagePayloadMappingProvider getMessagePayloadMappingProvider() {
        return messagePayloadMappingProvider;
    }
    
    protected String evaluateExpression(String expression, 
                                        DelegateExecution execution) {
        return Optional.ofNullable(expressionManager.createExpression(expression))
                       .map(it -> it.getValue(execution))
                       .map(Object::toString)
                       .orElseThrow(() -> new ActivitiIllegalArgumentException("Expression '" + expression + "' is null"));
    }
    
    protected void assertNoExistingDuplicateEventSubscriptions(String messageName,
                                                               String correlationKey,
                                                               CommandContext commandContext) {

        List<EventSubscriptionEntity> existing = commandContext.getEventSubscriptionEntityManager()
                                                               .findEventSubscriptionsByName("message",
                                                                                             messageName,
                                                                                             null);
        existing.stream()
                .filter(subscription -> Objects.equals(subscription.getConfiguration(),
                                                       correlationKey))
                .findFirst()
                .ifPresent(subscription -> {
                    throw new ActivitiIllegalArgumentException("Duplicate message subscription '" + subscription.getEventName() + 
                                                               "' with correlation key '" + subscription.getConfiguration() + "'");
                });

    }
}
