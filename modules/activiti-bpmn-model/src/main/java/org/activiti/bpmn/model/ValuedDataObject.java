package org.activiti.bpmn.model;


/**
 * @author Lori Small
 */
public abstract class ValuedDataObject extends DataObject {

  protected Object value;
  
  public Object getValue() {
    return value;
  }

  public abstract void setValue(Object value);
  
  public abstract ValuedDataObject clone();

  public void setValues(ValuedDataObject otherElement) {
    super.setValues(otherElement);
    setValue(otherElement.getValue());
  }
  
  public boolean equals(ValuedDataObject otherObject) {
    
    if (otherObject.getItemSubjectRef().getStructureRef() != this.itemSubjectRef.getStructureRef()) return false;
    if (otherObject.getId() != this.id) return false;
    if (otherObject.getName() != this.name) return false;
    if (!otherObject.getValue().equals(this.value.toString())) return false;
    
    return true;
  }
}
