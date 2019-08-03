package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;
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
        
        boolean result = send(execution, throwMessage);
        
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
        
        Object payload = getMessagePayload(execution).orElse(null);
        
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
        // inject payload
        return fieldDeclarations.stream()
                                .filter(it -> "payload".equals(it.getName()))
                                .map(FieldDeclaration::getValue)
                                .map(it -> (Expression.class.isInstance(it)) 
                                                 ? Expression.class.cast(it).getValue(execution) 
                                                 : it)
                                .findFirst();
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
                                                                       throwMessage.getPayload()));
          }
        
    }
}
