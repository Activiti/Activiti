package org.activiti.workflow.simple.definition;


public class ConditionDefinition {

  private static final long serialVersionUID = 1L;
  
  protected String leftOperand;
  protected String operator;
  protected String rightOperand;
  
  public String getLeftOperand() {
    return leftOperand;
  }
  public void setLeftOperand(String leftOperand) {
    this.leftOperand = leftOperand;
  }
  public String getOperator() {
    return operator;
  }
  public void setOperator(String operator) {
    this.operator = operator;
  }
  public String getRightOperand() {
    return rightOperand;
  }
  public void setRightOperand(String rightOperand) {
    this.rightOperand = rightOperand;
  }
  
  public ConditionDefinition clone() {
    ConditionDefinition clone = new ConditionDefinition();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(ConditionDefinition otherDefinition) {
    setLeftOperand(otherDefinition.getLeftOperand());
    setOperator(otherDefinition.getOperator());
    setRightOperand(otherDefinition.getRightOperand());
  }
}
