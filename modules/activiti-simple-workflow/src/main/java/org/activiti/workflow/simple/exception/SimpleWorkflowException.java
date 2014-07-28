package org.activiti.workflow.simple.exception;

/**
 * Runtime exception that is the superclass of all Simple workflow exceptions.
 * 
 * @author Tijs Rademakers
 */
public class SimpleWorkflowException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SimpleWorkflowException(String message, Throwable cause) {
    super(message, cause);
  }

  public SimpleWorkflowException(String message) {
    super(message);
  }
}
