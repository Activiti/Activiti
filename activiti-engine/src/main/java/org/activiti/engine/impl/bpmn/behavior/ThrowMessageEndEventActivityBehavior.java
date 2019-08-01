package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;

public class ThrowMessageEndEventActivityBehavior extends AbstractThrowMessageEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final ThrowMessageJavaDelegate delegate;
    
    public ThrowMessageEndEventActivityBehavior(ThrowMessageJavaDelegate activityBehavior,
                                                MessageEventDefinition messageEventDefinition,
                                                Message message) {
        super(messageEventDefinition, message);
        
        this.delegate = activityBehavior;
    }
    
    @Override
    public Object execute(DelegateExecution execution, Message message) {
        return delegate.execute(execution, message);
    }

}
