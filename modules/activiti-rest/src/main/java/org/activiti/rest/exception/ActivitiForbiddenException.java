package org.activiti.rest.exception;

import org.activiti.engine.ActivitiException;

public class ActivitiForbiddenException extends ActivitiException {

  private static final long serialVersionUID = 1L;
  
  public ActivitiForbiddenException(String message) {
    super(message);
  }
}
