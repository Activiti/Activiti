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

import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.parser.factory.MessageExecutionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;
import org.activiti.engine.impl.delegate.invocation.DelegateInvocation;
import org.activiti.engine.impl.delegate.invocation.ThrowMessageDelegateInvocation;

import java.util.Optional;

public abstract class AbstractThrowMessageEventActivityBehavior extends FlowNodeActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final MessageEventDefinition messageEventDefinition;
    private final ThrowMessageDelegate delegate;
    private final MessageExecutionContext messageExecutionContext;
    
    public AbstractThrowMessageEventActivityBehavior(MessageEventDefinition messageEventDefinition,
                                                     ThrowMessageDelegate delegate,
                                                     MessageExecutionContext messageExecutionContext) {
        this.messageEventDefinition = messageEventDefinition;
        this.delegate = delegate;
        this.messageExecutionContext = messageExecutionContext;
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
        
        if(isSent) {
            dispatchEvent(execution, throwMessage);
        }
        
        super.execute(execution);
    }

    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }
    
    protected ThrowMessage getThrowMessage(DelegateExecution execution) {
        return messageExecutionContext.createThrowMessage(execution);
    }

    protected void dispatchEvent(DelegateExecution execution, ThrowMessage throwMessage) {
        Optional.ofNullable(Context.getCommandContext())
                .filter(commandContext -> commandContext.getProcessEngineConfiguration()
                                                        .getEventDispatcher()
                                                        .isEnabled())   
                .ifPresent(commandContext -> {
                    String messageName = throwMessage.getName();
                    String correlationKey = throwMessage.getCorrelationKey()
                                                        .orElse(null);
                    Object payload = throwMessage.getPayload()
                                                 .orElse(null);
                    
                    commandContext.getProcessEngineConfiguration()
                                  .getEventDispatcher()
                                  .dispatchEvent(ActivitiEventBuilder.createMessageSentEvent(execution,
                                                                                             messageName,
                                                                                             correlationKey,
                                                                                             payload));
                });
    }

    public ThrowMessageDelegate getDelegate() {
        return delegate;
    }
    
    public MessageExecutionContext getMessageExecutionContext() {
        return messageExecutionContext;
    }
}
