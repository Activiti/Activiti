package org.activiti.test.spring.executor.jms.delegate;

import java.util.Random;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class RandomDelegate implements JavaDelegate {

    private static Random random = new Random();

    public void execute(DelegateExecution delegateExecution) {
        Number number1 = (Number) delegateExecution.getVariable("input1");
        Number number2 = (Number) delegateExecution.getVariable("input2");
        int result = number1.intValue() + number2.intValue();
        delegateExecution.setVariable("result_" + random.nextInt(), "result is " + result);
    }

}
