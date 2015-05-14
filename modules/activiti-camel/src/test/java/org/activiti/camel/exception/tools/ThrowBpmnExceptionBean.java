package org.activiti.camel.exception.tools;

import org.activiti.engine.delegate.BpmnError;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ThrowBpmnExceptionBean {
  
  public enum ExceptionType {
    NO_EXCEPTION, BPMN_EXCEPTION, NON_BPMN_EXCEPTION 
  }
  
  protected static ExceptionType exceptionType;
  
  
  public static ExceptionType getExceptionType() {
    return exceptionType;
  }

  
  public static void setExceptionType(ExceptionType exceptionType) {
    ThrowBpmnExceptionBean.exceptionType = exceptionType;
  }

  private static final Logger LOG = LoggerFactory.getLogger(ThrowBpmnExceptionBean.class);

  @Handler
  
  public void throwNonBpmnException() throws Exception {
    LOG.debug("throwing non bpmn bug");
    
    switch(getExceptionType()) {
      case NO_EXCEPTION: break;
      case NON_BPMN_EXCEPTION: throw new Exception("arbitary non bpmn exception");
      case BPMN_EXCEPTION: throw new BpmnError("testError");
    }
  }
}
