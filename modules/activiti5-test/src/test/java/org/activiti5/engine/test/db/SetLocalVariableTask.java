package org.activiti5.engine.test.db;

import org.activiti5.engine.delegate.DelegateExecution;
import org.activiti5.engine.delegate.JavaDelegate;

public class SetLocalVariableTask implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
	  execution.setVariableLocal("test", "test2");
  }

}
