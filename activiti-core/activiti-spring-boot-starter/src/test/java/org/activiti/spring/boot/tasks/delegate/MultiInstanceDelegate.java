package org.activiti.spring.boot.tasks.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class MultiInstanceDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) {
    Integer result = (Integer) execution.getVariable("result");

    Integer item = (Integer) execution.getVariable("item");
    if (item != null) {
      result = result * item;
    } else {
      result = (result != null) ? result * 2 : new Integer(1);
    }
    execution.setVariable("result", result);
  }

}
