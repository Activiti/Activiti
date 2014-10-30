package org.activiti.management.jmx.mbeans;

import org.activiti.management.jmx.annotations.ManagedAttribute;


public class BadAttributeNameNoGetter {
  
  @ManagedAttribute(description = "test non public attribute description1")
  public String getestAttribute1() {
    return null;

  }


}
