package org.activiti.bpmn.model;

public class DataSpec extends BaseElement {
  
  protected String name;
  protected String itemSubjectRef;
  protected boolean isCollection;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getItemSubjectRef() {
    return itemSubjectRef;
  }
  public void setItemSubjectRef(String itemSubjectRef) {
    this.itemSubjectRef = itemSubjectRef;
  }
  public boolean isCollection() {
    return isCollection;
  }
  public void setCollection(boolean isCollection) {
    this.isCollection = isCollection;
  }
}
