package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ThrowMessage;
import org.activiti.engine.impl.delegate.ThrowMessageDelegate;

public class DefaultThrowMessageJavaDelegate implements ThrowMessageDelegate {

    @Override
    public boolean send(DelegateExecution execution, ThrowMessage message) {
        return true;
    }
}