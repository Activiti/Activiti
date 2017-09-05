package org.activiti.engine.test.db;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;

public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  private static final long serialVersionUID = 1L;

  public void execute(DelegateExecution execution) {
  }

}
