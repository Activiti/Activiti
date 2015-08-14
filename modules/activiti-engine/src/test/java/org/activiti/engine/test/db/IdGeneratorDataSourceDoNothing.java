package org.activiti.engine.test.db;

import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;

public class IdGeneratorDataSourceDoNothing implements ActivityBehavior {

  public void execute(ActivityExecution execution) {
  }

}
