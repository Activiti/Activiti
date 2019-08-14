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

import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.MessagePayloadMappingProvider;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;
import org.activiti.engine.impl.delegate.invocation.DelegateInvocation;
import org.activiti.engine.impl.delegate.invocation.ThrowMessageDelegateInvocation;
import org.activiti.engine.impl.interceptor.CommandContext;

public abstract class AbstractThrowMessageEventActivityBehavior extends FlowNodeActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final MessageEventDefinition messageEventDefinition;
    private final Message message;
    private final ThrowMessageDelegate delegate;
    private final MessagePayloadMappingProvider messagePayloadMappingProvider;
    
    public AbstractThrowMessageEventActivityBehavior(ThrowMessageDelegate delegate,
                                                     MessageEventDefinition messageEventDefinition,
                                                     Message message,
                                                     MessagePayloadMappingProvider messagePayloadMappingProvider) {
        this.messageEventDefinition = messageEventDefinition;
        this.message = message;
        this.delegate = delegate;
        this.messagePayloadMappingProvider = messagePayloadMappingProvider;
    }
    
    protected boolean send(DelegateExecution execution, ThrowMessage message) {
        DelegateInvocation invocation = new ThrowMessageDelegateInvocation(delegate, execution, message);
        
        Context.getProcessEngineConfiguration()
               .getDelegateInterceptor()
               .handleInvocation(invocation);
        
        return (boolean) invocation.getInvocationResult();
    };

    @Override
    public void execute(DelegateExecution execution) {
        ThrowMessage throwMessage = getThrowMessage(execution);
        
        boolean isSent = send(execution, throwMessage);
        
        if(isSent)
            dispatchEvent(execution, throwMessage);
        
        super.execute(execution);
    }

    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }
    
    public Message getMessage() {
        return message;
    }
    
    protected ThrowMessage getThrowMessage(DelegateExecution execution) {
        String name = getMessageName(execution);
        Optional<String> businessKey = Optional.ofNullable(execution.getProcessInstanceBusinessKey());
        
        Optional<Map<String, Object>> payload = messagePayloadMappingProvider.getMessagePayload(execution);
        
        return ThrowMessage.builder()
                           .name(name)
                           .businessKey(businessKey)
                           .payload(payload)
                           .build();
    }

    protected String getMessageName(DelegateExecution execution) {
        Expression expression = Context.getProcessEngineConfiguration()
                                       .getExpressionManager()
                                       .createExpression(message.getName());

        return expression.getValue(execution)
                         .toString();
    }
    
    protected void dispatchEvent(DelegateExecution execution, ThrowMessage throwMessage) {
        CommandContext commandContext = Context.getCommandContext();
        
        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            commandContext
                .getProcessEngineConfiguration()
                .getEventDispatcher()
                .dispatchEvent(ActivitiEventBuilder.createMessageEvent(ActivitiEventType.ACTIVITY_MESSAGE_SENT, 
                                                                       execution, 
                                                                       throwMessage.getName(), 
                                                                       throwMessage.getPayload()
                                                                                   .orElse(null)));
          }
        
    }
}
