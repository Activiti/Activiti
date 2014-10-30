package org.activiti.management.jmx.mbeans;

import org.activiti.management.jmx.annotations.ManagedAttribute;


public class BadAttributeNameNotCaptal {
  
  @ManagedAttribute
  public String somethingRandom() {
    return null;

  }


}
