package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateExecution;

public class DefaultThrowMessageJavaDelegate implements ThrowMessageDelegate {

    @Override
    public boolean send(DelegateExecution execution, ThrowMessage message) {
        return true;
    }
}