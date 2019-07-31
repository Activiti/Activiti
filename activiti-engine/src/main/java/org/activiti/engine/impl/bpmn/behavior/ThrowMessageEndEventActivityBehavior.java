package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;

public class ThrowMessageEndEventActivityBehavior extends NoneEndEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final ThrowMessageJavaDelegate delegate;
    private final MessageEventDefinition messageEventDefinition;
    private final Message message;
    
    public ThrowMessageEndEventActivityBehavior(ThrowMessageJavaDelegate activityBehavior,
                                                MessageEventDefinition messageEventDefinition,
                                                Message message) {
        this.delegate = activityBehavior;
        this.messageEventDefinition = messageEventDefinition;
        this.message = message;
    }
    
    @Override
    public void execute(DelegateExecution execution) {
        delegate.execute(execution, message);
        
        super.execute(execution);
    }
}
