package org.activiti.examples.bpmn.servicetask;

import java.io.Serializable;

/**
 * @author Christian Stettler
 */
public class OkReturningService implements Serializable {

  private static final long serialVersionUID = 1L;

  public String invoke() {
    return "ok";
  }
  
}
