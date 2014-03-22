package org.activiti.bpmn.model;


/**
 * @author Lori Small
 */
public class StringDataObject extends ValuedDataObject {

  public void setValue(Object value) {
    this.value = value.toString();
  }

  public StringDataObject clone() {
    StringDataObject clone = new StringDataObject();
    clone.setValues(this);
    return clone;
  }
}
