package org.activiti.examples.bpmn.executionlistener;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class CustomSetConditionsExecutionListener implements ExecutionListener {

  private static final long serialVersionUID = 1L;

  protected List<String> conditions = new ArrayList<String>();
  protected String flowId;

  @Override
  public void notify(DelegateExecution execution) {
    execution.setVariable(flowId + "_activiti_conditions", conditions);
  }

  public List<String> getConditions() {
    return conditions;
  }

  public void setConditions(List<String> conditions) {
    this.conditions = conditions;
  }

  public void addCondition(String condition) {
    this.conditions.add(condition);
  }

  public String getFlowId() {
    return flowId;
  }

  public void setFlowId(String flowId) {
    this.flowId = flowId;
  }

}
