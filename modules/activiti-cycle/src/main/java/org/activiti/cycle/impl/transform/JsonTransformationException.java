package org.activiti.cycle.impl.transform;


public class JsonTransformationException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public JsonTransformationException(String message, Exception exception) {
    super(message, exception);
  }

  public JsonTransformationException(String message) {
    super(message);
  }

}
