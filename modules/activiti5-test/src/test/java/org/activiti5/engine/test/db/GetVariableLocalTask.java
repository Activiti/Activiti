package org.activiti5.engine.test.db;

import org.activiti5.engine.RuntimeService;
import org.activiti5.engine.delegate.DelegateExecution;
import org.activiti5.engine.delegate.JavaDelegate;

public class GetVariableLocalTask implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    RuntimeService runtimeService = execution.getEngineServices().getRuntimeService();
    runtimeService.getVariableLocal(execution.getProcessInstanceId(), "Variable-That-Does-Not-Exist");
  }
}