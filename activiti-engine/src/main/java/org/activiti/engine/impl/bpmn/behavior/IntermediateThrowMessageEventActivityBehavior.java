package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;

public class IntermediateThrowMessageEventActivityBehavior extends AbstractThrowMessageEventActivityBehavior {

    private static final long serialVersionUID = 1L;
    
    private final ThrowMessageJavaDelegate delegate;
    
    public IntermediateThrowMessageEventActivityBehavior(ThrowMessageJavaDelegate activityBehavior,
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
