package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

public class DataAssociation extends BaseElement {

  protected String sourceRef;
  protected String targetRef;
  protected String transformation;
  protected List<Assignment> assignments = new ArrayList<Assignment>();
  
  public String getSourceRef() {
    return sourceRef;
  }
  public void setSourceRef(String sourceRef) {
    this.sourceRef = sourceRef;
  }
  public String getTargetRef() {
    return targetRef;
  }
  public void setTargetRef(String targetRef) {
    this.targetRef = targetRef;
  }
  public String getTransformation() {
    return transformation;
  }
  public void setTransformation(String transformation) {
    this.transformation = transformation;
  }
  public List<Assignment> getAssignments() {
    return assignments;
  }
  public void setAssignments(List<Assignment> assignments) {
    this.assignments = assignments;
  }
  
  public DataAssociation clone() {
    DataAssociation clone = new DataAssociation();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(DataAssociation otherAssociation) {
    setSourceRef(otherAssociation.getSourceRef());
    setTargetRef(otherAssociation.getTargetRef());
    setTransformation(otherAssociation.getTransformation());
    
    assignments = new ArrayList<Assignment>();
    if (otherAssociation.getAssignments() != null && !otherAssociation.getAssignments().isEmpty()) {
      for (Assignment assignment : otherAssociation.getAssignments()) {
        assignments.add(assignment.clone());
      }
    }
  }
}
