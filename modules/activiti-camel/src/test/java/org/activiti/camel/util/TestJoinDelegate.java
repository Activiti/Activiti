package org.activiti.camel.util;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class TestJoinDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // dummy task
  }

}
