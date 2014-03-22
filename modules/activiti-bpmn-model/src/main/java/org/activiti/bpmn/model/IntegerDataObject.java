package org.activiti.bpmn.model;


/**
 * @author Lori Small
 */
public class IntegerDataObject extends ValuedDataObject {

  public void setValue(Object value) {
    this.value = Integer.valueOf(value.toString());
  }

  public IntegerDataObject clone() {
    IntegerDataObject clone = new IntegerDataObject();
    clone.setValues(this);
    return clone;
  }
}
