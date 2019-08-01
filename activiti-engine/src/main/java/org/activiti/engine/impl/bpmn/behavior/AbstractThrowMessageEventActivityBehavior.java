package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.context.Context;

public abstract class AbstractThrowMessageEventActivityBehavior extends FlowNodeActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final MessageEventDefinition messageEventDefinition;
    private final Message message;
    
    public AbstractThrowMessageEventActivityBehavior(MessageEventDefinition messageEventDefinition,
                                                     Message message) {
        this.messageEventDefinition = messageEventDefinition;
        this.message = message;
    }
    
    protected abstract Object execute(DelegateExecution execution, Message message);

    @Override
    public void execute(DelegateExecution execution) {
        Message executionMessage = getExecutionMessage(execution);
        
        Object payload = execute(execution, executionMessage);
        
        // TODO dispatch event
        
        super.execute(execution);
    }

    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }
    
    public Message getMessage() {
        return message;
    }
    
    protected Message getExecutionMessage(DelegateExecution execution) {
        Expression expression = Context.getProcessEngineConfiguration()
                                       .getExpressionManager()
                                       .createExpression(message.getName());

        String name = expression.getValue(execution)
                                .toString();
        
        return Message.builderFrom(message)
                      .name(name)
                      .build();
    }
}
