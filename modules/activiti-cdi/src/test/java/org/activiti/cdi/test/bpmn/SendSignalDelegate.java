package org.activiti.cdi.test.bpmn;

import javax.inject.Inject;
import javax.inject.Named;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

@Named
public class SendSignalDelegate implements JavaDelegate {

  @Inject
  private RuntimeService runtimeService;  

  public void execute(DelegateExecution execution) throws Exception {
    String executionId = (String) execution.getVariable("signalExecutionId");
    runtimeService.signalEventReceived("alert", executionId);
  }

}