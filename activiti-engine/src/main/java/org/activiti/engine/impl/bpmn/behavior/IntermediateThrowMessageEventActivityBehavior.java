package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;

public class IntermediateThrowMessageEventActivityBehavior extends AbstractThrowMessageEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final ThrowEvent throwEvent;
    
    public IntermediateThrowMessageEventActivityBehavior(ThrowEvent throwEvent,
                                                         ThrowMessageDelegate delegate,
                                                         MessageEventDefinition messageEventDefinition,
                                                         Message message,
                                                         List<FieldDeclaration> fieldDeclarations) {
        super(delegate, messageEventDefinition, message, fieldDeclarations);
        
        this.throwEvent = throwEvent;
    }

    public ThrowEvent getThrowEvent() {
        return throwEvent;
    }

}
