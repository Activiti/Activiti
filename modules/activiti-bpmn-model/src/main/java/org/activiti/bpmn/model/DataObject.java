package org.activiti.bpmn.model;

/**
 * @author Lori Small
 */
public class DataObject extends FlowElement {

  protected ItemDefinition itemSubjectRef;
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ItemDefinition getItemSubjectRef() {
    return itemSubjectRef;
  }

  public void setItemSubjectRef(ItemDefinition itemSubjectRef) {
    this.itemSubjectRef = itemSubjectRef;
  }

  public DataObject clone() {
    DataObject clone = new DataObject();
    clone.setValues(this);
    return clone;
  }

  public void setValues(DataObject otherElement) {
    super.setValues(otherElement);
    
    setId(otherElement.getId());
    setName(otherElement.getName());
    setItemSubjectRef(otherElement.getItemSubjectRef());
  }
}
