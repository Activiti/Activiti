package org.activiti;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class VariablesBulkDeleteTest extends PluggableActivitiTestCase {
	
	public void testje() {
		deployOneTaskTestProcess();
		
		Map<String, Object> vars = new HashMap<String, Object>();
		for (int j=0; j<40; j++) {
			vars.put("var"+j, j);
		} 
		
		long start = System.currentTimeMillis();
//		for (int i=0; i<100; i++) {
			runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
			taskService.complete(taskService.createTaskQuery().singleResult().getId());
			
//			if (i%500 == 0) {
//				System.out.println("Processes done = " + i);
//			}
//		}
		long end = System.currentTimeMillis();
		System.out.println("Time = " + (end-start) + " ms");
		
	}

}
