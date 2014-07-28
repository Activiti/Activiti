package org.activiti.rest.service.api.form;

import java.io.Serializable;

public class Address implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected String street;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }
}
