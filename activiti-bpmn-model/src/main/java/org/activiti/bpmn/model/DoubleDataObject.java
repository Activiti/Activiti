package org.activiti.bpmn.model;

public class DoubleDataObject extends ValuedDataObject {

  public void setValue(Object value) {
    this.value = Double.valueOf(value.toString());
  }

  public DoubleDataObject clone() {
    DoubleDataObject clone = new DoubleDataObject();
    clone.setValues(this);
    return clone;
  }
}
