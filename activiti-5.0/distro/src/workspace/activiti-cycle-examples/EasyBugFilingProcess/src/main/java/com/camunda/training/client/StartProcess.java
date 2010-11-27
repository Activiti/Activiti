package com.camunda.training.client;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;

public class StartProcess {

	public static void main(String[] args) {
		ProcessEngines.init();		
		ProcessEngine processEngine = ProcessEngines.getProcessEngine(ProcessEngines.NAME_DEFAULT);		
	    RuntimeService runtimeService = processEngine.getRuntimeService();
	    
	    Map<String,Object> variables = new HashMap<String,Object>();
	    variables.put("test", "test");
	    
	    runtimeService.startProcessInstanceByKey("EasyBugFilingProcess", variables);  
	}
}
