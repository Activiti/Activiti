package org.activiti.examples.variables;

import java.io.Serializable;

public class SomeSerializable implements Serializable {

  private static final long serialVersionUID = 1L;
  private final String value;

  public SomeSerializable(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
