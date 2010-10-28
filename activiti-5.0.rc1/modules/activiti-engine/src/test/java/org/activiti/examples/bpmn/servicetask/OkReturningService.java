package org.activiti.examples.bpmn.servicetask;

import java.io.Serializable;

/**
 * @author Christian Stettler
 */
public class OkReturningService implements Serializable {

  public String invoke() {
    return "ok";
  }
  
}
