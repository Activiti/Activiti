package org.activiti5.examples.bpmn.scripttask;

import java.io.Serializable;


public class ScriptTaskTestPojo implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public static void foo() {
    throw new IllegalStateException();
  }

}
