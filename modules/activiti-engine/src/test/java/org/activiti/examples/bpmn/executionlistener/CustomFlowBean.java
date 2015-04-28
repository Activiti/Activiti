package org.activiti.examples.bpmn.executionlistener;

import java.io.Serializable;

import org.activiti.engine.delegate.DelegateExecution;

public class CustomFlowBean implements Serializable {

  private static final long serialVersionUID = 1L;

  public boolean executeLogic(String flowId, DelegateExecution execution) {
    Object conditionsObject = execution.getVariable(flowId + "_activiti_conditions");
    return conditionsObject != null;
  }
}
