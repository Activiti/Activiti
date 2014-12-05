package org.activiti.camel.exception.tools;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class ExceptionServiceMock implements JavaDelegate {
  static boolean isCalled = false;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    isCalled = true;
  }
  
  public static void reset() {
    isCalled = false;
  }

  public static boolean isCalled() {
    return isCalled;
  }

}
