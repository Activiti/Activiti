package org.activiti.cdi;

import org.activiti.engine.ActivitiException;

/**
 * Represents an exception in activiti cdi.
 * 
 * @author Daniel Meyer
 */
public class ActivitiCdiException extends ActivitiException {

  private static final long serialVersionUID = 1L;

  public ActivitiCdiException(String message, Throwable cause) {
    super(message, cause);
  }

  public ActivitiCdiException(String message) {
    super(message);  
  }

}
