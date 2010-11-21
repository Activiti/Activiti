package com.camunda.training.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegation;
import org.activiti.engine.impl.runtime.ExecutionEntity;

public class SysoutDelegate implements JavaDelegation {

	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("Execution " + execution.getId() + " passes by " + ((ExecutionEntity)execution).getActivityId() + "...");
	}

}
