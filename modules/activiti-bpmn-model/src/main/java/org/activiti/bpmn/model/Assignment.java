package org.activiti.bpmn.model;

public class Assignment extends BaseElement {

  protected String from;
  protected String to;
  
  public String getFrom() {
    return from;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public String getTo() {
    return to;
  }
  public void setTo(String to) {
    this.to = to;
  }
}
