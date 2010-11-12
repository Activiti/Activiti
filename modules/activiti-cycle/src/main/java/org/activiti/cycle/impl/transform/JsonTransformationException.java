package org.activiti.cycle.impl.transform;


public class JsonTransformationException extends RuntimeException {

  public JsonTransformationException(String message, Exception exception) {
    super(message, exception);
  }

  private static final long serialVersionUID = 1L;

}
