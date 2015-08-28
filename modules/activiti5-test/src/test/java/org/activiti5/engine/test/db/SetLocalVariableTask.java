package org.activiti5.engine.test.db;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class SetLocalVariableTask implements JavaDelegate {

	public void execute(DelegateExecution execution) {
	  execution.setVariableLocal("test", "test2");
  }

}
