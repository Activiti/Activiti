package org.activiti.test.bpmn.property;

import java.io.Serializable;

public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String address;
    
    public Order() {
      
    }
    
    public Order(String address) {
      this.address = address;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }
    
  }