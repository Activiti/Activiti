package org.activiti.engine.test.logging.mdc;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class TestService implements JavaDelegate{
	static String processInstanceId = null;
	static String processDefinitionId = null;
	static String executionId = null;
	static String businessKey = null;
	

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		TestService.processDefinitionId = execution.getProcessDefinitionId();
		TestService.processInstanceId =  execution.getProcessInstanceId();
		TestService.executionId = execution.getId();
		TestService.businessKey = execution.getBusinessKey();
		
		throw new Exception("test");
		
	}
	
	public void clearProcessVariables() {
		processDefinitionId = null;
		processInstanceId = null;
		executionId = null;
		businessKey = null;
	}

}
