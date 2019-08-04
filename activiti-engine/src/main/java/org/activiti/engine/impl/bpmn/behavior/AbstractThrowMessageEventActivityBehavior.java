package org.activiti.engine.impl.bpmn.behavior;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
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
    private final List<FieldDeclaration> fieldDeclarations;
    
    public AbstractThrowMessageEventActivityBehavior(ThrowMessageDelegate delegate,
                                                     MessageEventDefinition messageEventDefinition,
                                                     Message message,
                                                     List<FieldDeclaration> fieldDeclarations) {
        this.messageEventDefinition = messageEventDefinition;
        this.message = message;
        this.delegate = delegate;
        this.fieldDeclarations = fieldDeclarations;
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
        
        Optional<Object> payload = getMessagePayload(execution);
        
        return ThrowMessage.builder()
                           .name(name)
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
    
    protected Optional<Object> getMessagePayload(DelegateExecution execution) {
        Map<String, Object> payload = new LinkedHashMap<>();  
                
        fieldDeclarations.stream()
                         .map(field -> applyFieldDeclaration(execution, 
                                                             field))
                         .forEach(entry -> payload.put(entry.getKey(), entry.getValue()));
        
        return Optional.of(payload)
                       .filter(map -> !map.isEmpty())
                       .map(map -> Collections.unmodifiableMap(map));
    }
    
    protected Map.Entry<String, Object> applyFieldDeclaration(DelegateExecution execution, FieldDeclaration field) {
        return Optional.of(field)
                       .map(f -> {
                           Object value = Optional.ofNullable(f.getValue())
                                                  .map(v -> (Expression.class.isInstance(v)) 
                                                               ? Expression.class.cast(v).getValue(execution) 
                                                               : v)
                                                  .orElse(null);
                           
                           return new AbstractMap.SimpleImmutableEntry<>(field.getName(), value);
                        })
                       .get();
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
