package org.activiti.rest.dmn.exception;

import org.activiti.dmn.engine.ActivitiDmnException;

public class ActivitiDmnConflictException extends ActivitiDmnException {

  private static final long serialVersionUID = 1L;

  public ActivitiDmnConflictException(String message) {
    super(message);
  }
}
