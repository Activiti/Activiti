package org.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class DummyServiceTask2 implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Integer count = (Integer) execution.getVariable("count2");
        count = count + 1;
        execution.setVariable("count2", count);
    }

}
