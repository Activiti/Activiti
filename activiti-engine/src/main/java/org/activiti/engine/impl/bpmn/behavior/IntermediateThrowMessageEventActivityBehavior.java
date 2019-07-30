package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;

public class IntermediateThrowMessageEventActivityBehavior extends IntermediateThrowNoneEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final ThrowEvent throwEvent;
    private final MessageEventDefinition messageEventDefinition;
    private final Message message;
    
    public IntermediateThrowMessageEventActivityBehavior(ThrowEvent throwEvent,
                                                         MessageEventDefinition messageEventDefinition,
                                                         Message message) {
        
        this.throwEvent = throwEvent;
        this.messageEventDefinition = messageEventDefinition;
        this.message = message;

    }

    
    public ThrowEvent getThrowEvent() {
        return throwEvent;
    }

    
    public MessageEventDefinition getMessageEventDefinition() {
        return messageEventDefinition;
    }

    
    public Message getMessage() {
        return message;
    }

}
