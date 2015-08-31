package org.activiti5.camel.util;

import org.activiti.camel.ActivitiProducer;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class InitDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    execution.setVariable(ActivitiProducer.PROCESS_ID_PROPERTY, execution.getProcessInstanceId());
  }

}
