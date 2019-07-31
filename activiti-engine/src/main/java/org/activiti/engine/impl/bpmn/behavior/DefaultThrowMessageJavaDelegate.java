package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.Message;
import org.activiti.engine.delegate.DelegateExecution;

public class DefaultThrowMessageJavaDelegate implements ThrowMessageJavaDelegate {

    @Override
    public void execute(DelegateExecution execution, Message message) {
        // Nothing here
    }
}