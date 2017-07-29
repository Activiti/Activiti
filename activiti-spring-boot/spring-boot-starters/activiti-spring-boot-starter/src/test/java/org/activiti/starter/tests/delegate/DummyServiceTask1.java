package org.activiti.starter.tests.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class DummyServiceTask1 implements JavaDelegate {

    public void execute(DelegateExecution execution) {
        Integer count = (Integer) execution.getVariable("count");
        count = count + 1;
        execution.setVariable("count", count);
    }

}
