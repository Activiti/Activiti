package org.activiti.engine.impl.delegate;

import org.activiti.engine.delegate.DelegateExecution;

public interface ThrowMessageDelegate {

    boolean send(DelegateExecution execution, ThrowMessage message);

}
