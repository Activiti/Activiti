package org.activiti.camel.examples.pingPong;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class SaveOutput implements  JavaDelegate {

	
	private static final long serialVersionUID = 1L;

	

	@SuppressWarnings("unchecked")
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		Map<String, String> outputMap = (Map<String, String>) execution.getVariable("outputMap");
		outputMap.put("outputValue",  (String) execution.getVariable("camelBody"));
		
		
	}

}
