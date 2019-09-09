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

import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.el.ExpressionManager;

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

    @Override
    public Optional<String> getCorrelationKey(DelegateExecution execution) {
        return Optional.ofNullable(messageEventDefinition.getCorrelationKey())
                       .map(correlationKey -> {
                           return evaluateExpression(messageEventDefinition.getCorrelationKey(),
                                                     execution);
                       });
    }
    
    @Override
    public Optional<Map<String, Object>> getMessagePayload(DelegateExecution execution) {
        return messagePayloadMappingProvider.getMessagePayload(execution);
    }

    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }

    public MessagePayloadMappingProvider getMessagePayloadMappingProvider() {
        return messagePayloadMappingProvider;
    }
    
    protected String evaluateExpression(String expression, 
                                        DelegateExecution execution) {
        return expressionManager.createExpression(expression)
                                .getValue(execution)
                                .toString();
    }
    

}
