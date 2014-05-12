package org.activiti.bpmn.model;


/**
 * @author Lori Small
 */
public class LongDataObject extends ValuedDataObject {

  public void setValue(Object value) {
    this.value = Long.valueOf(value.toString());
  }

  public LongDataObject clone() {
    LongDataObject clone = new LongDataObject();
    clone.setValues(this);
    return clone;
  }
}
