package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

public class Interface extends BaseElement {

  protected String name;
  protected String implementationRef;
  protected List<Operation> operations = new ArrayList<Operation>();
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getImplementationRef() {
    return implementationRef;
  }
  public void setImplementationRef(String implementationRef) {
    this.implementationRef = implementationRef;
  }
  public List<Operation> getOperations() {
    return operations;
  }
  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }
}
