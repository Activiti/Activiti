package org.activiti5.engine.test.db;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;


public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  public void execute(DelegateExecution execution) {
  }

}
