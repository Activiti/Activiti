package org.activiti.bpmn.model;

import java.util.Date;

public class DateDataObject extends ValuedDataObject {

  public void setValue(Object value) {
    this.value = (Date) value;
  }

  public DateDataObject clone() {
    DateDataObject clone = new DateDataObject();
    clone.setValues(this);
    return clone;
  }
}
