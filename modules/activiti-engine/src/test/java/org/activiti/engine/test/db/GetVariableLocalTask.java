package org.activiti.engine.test.db;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class GetVariableLocalTask implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    RuntimeService runtimeService = execution.getEngineServices().getRuntimeService();
    runtimeService.getVariableLocal(execution.getProcessInstanceId(), "Variable-That-Does-Not-Exist");
  }
}