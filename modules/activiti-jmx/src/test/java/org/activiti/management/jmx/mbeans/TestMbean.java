package org.activiti.management.jmx.mbeans;

import org.activiti.management.jmx.annotations.ManagedAttribute;
import org.activiti.management.jmx.annotations.ManagedOperation;
import org.activiti.management.jmx.annotations.ManagedResource;

@ManagedResource(description = "test description")
public class TestMbean {

  @ManagedAttribute(description = "test attribute String description")
  public String getTestAttributeString() {
    return null;

  }
  
  
  @ManagedAttribute(description = "test attribute Boolean description")
  public Boolean isTestAttributeBoolean() {
    return null;
  }

  
  @ManagedOperation(description = "test operation description")
  public void getTestOperation() {
    

  }
  

}
