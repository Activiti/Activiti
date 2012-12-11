package org.activiti.engine.test.db;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
  }

}
