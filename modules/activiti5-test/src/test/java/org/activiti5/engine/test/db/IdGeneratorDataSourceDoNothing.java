package org.activiti5.engine.test.db;

import org.activiti5.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;


public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
  }

}
