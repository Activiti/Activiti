package org.activiti.kickstart.dto;

import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.activity.type.ServiceTask;

public class ServiceTaskDto extends BaseTaskDto {

  private String className;

  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }

  private String delegateExpression;

  public String getDelegateExpression() {
    return delegateExpression;
  }
  public void setDelegateExpression(String delegateExpression) {
    this.delegateExpression = delegateExpression;
  }

  private String expression;

  public String getExpression() {
    return expression;
  }
  public void setExpression(String expression) {
    this.expression = expression;
  }

  @Override
  public FlowElement createFlowElement() {
    ServiceTask serviceTask = new ServiceTask();
    serviceTask.setDelegateExpression(getDelegateExpression());
    serviceTask.setClassName(getClassName());
    serviceTask.setExpression(getExpression());
    return serviceTask;
  }
}
