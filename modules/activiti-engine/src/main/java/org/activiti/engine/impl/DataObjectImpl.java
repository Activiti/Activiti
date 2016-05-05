package org.activiti.engine.impl;

import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.runtime.DataObject;

public class DataObjectImpl implements DataObject {
  private String name;
  private Object value;
  private String description;
  private String localizedName;
  private String localizedDescription;

  private String type;
  
  public DataObjectImpl(VariableInstance variable, String description, String localizedName, String localizedDescription) {
    this.name = variable.getName();
    this.value = variable.getValue();
    this.description = description;
    this.localizedName = localizedName;
    this.localizedDescription = localizedDescription;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getLocalizedName() {
    if (localizedName != null && localizedName.length() > 0) {
      return localizedName;
    } else {
      return name;
    }
  }
  
  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }
  
  public String getDescription() {
    if (localizedDescription != null && localizedDescription.length() > 0) {
      return localizedDescription;
    } else {
      return description;
    }
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
}
