package org.activiti.rest.dmn.exception;

import org.activiti.dmn.engine.ActivitiDmnException;

public class ActivitiDmnContentNotSupportedException extends ActivitiDmnException {

  private static final long serialVersionUID = 1L;

  public ActivitiDmnContentNotSupportedException(String message) {
    super(message);
  }
}
