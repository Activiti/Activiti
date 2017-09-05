package org.activiti.engine.test.logging.mdc;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class TestService implements JavaDelegate {
  static String processInstanceId;
  static String processDefinitionId;
  static String executionId;
  static String businessKey;

  @Override
  public void execute(DelegateExecution execution) {
    TestService.processDefinitionId = execution.getProcessDefinitionId();
    TestService.processInstanceId = execution.getProcessInstanceId();
    TestService.executionId = execution.getId();
    TestService.businessKey = execution.getProcessInstanceBusinessKey();

    throw new RuntimeException("test");

  }

  public void clearProcessVariables() {
    processDefinitionId = null;
    processInstanceId = null;
    executionId = null;
    businessKey = null;
  }

}
