package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;

public class ThrowMessageEndEventActivityBehavior extends AbstractThrowMessageEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final EndEvent endEvent;
    
    public ThrowMessageEndEventActivityBehavior(EndEvent endEvent,
                                                ThrowMessageDelegate delegate,
                                                MessageEventDefinition messageEventDefinition,
                                                Message message,
                                                List<FieldDeclaration> fieldDeclarations) {
        super(delegate, messageEventDefinition, message, fieldDeclarations);
        
        this.endEvent = endEvent;
    }
    
    public EndEvent getEndEvent() {
        return endEvent;
    }

}
