package org.activiti.rest.exception;

import org.activiti.engine.ActivitiException;

public class ActivitiContentNotSupportedException extends ActivitiException {

  private static final long serialVersionUID = 1L;
  
  public ActivitiContentNotSupportedException(String message) {
    super(message);
  }
}
