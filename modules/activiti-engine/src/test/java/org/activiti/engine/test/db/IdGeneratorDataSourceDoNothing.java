package org.activiti.engine.test.db;

import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  @Override
  public void execute(ActivityExecution execution) throws Exception {
  }

}
