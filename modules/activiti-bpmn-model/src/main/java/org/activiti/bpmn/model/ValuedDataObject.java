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
    if (otherElement.getValue() != null) {
      setValue(otherElement.getValue());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ValuedDataObject otherObject = (ValuedDataObject) o;

    if (!otherObject.getItemSubjectRef().getStructureRef().equals(this.itemSubjectRef.getStructureRef())) return false;
    if (!otherObject.getId().equals(this.id)) return false;
    if (!otherObject.getName().equals(this.name)) return false;
    if (!otherObject.getValue().equals(this.value.toString())) return false;

    return true;
  }

}
