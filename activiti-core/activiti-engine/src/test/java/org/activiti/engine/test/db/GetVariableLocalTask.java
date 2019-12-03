package org.activiti.engine.test.db;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;

public class GetVariableLocalTask implements JavaDelegate {
  @Override
  public void execute(DelegateExecution execution) {
    RuntimeService runtimeService = Context.getProcessEngineConfiguration().getRuntimeService();
    runtimeService.getVariableLocal(execution.getProcessInstanceId(), "Variable-That-Does-Not-Exist");
  }
}