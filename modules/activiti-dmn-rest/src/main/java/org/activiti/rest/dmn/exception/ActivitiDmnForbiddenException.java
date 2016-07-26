package org.activiti.rest.dmn.exception;

import org.activiti.dmn.engine.ActivitiDmnException;

public class ActivitiDmnForbiddenException extends ActivitiDmnException {

  private static final long serialVersionUID = 1L;

  public ActivitiDmnForbiddenException(String message) {
    super(message);
  }
}
