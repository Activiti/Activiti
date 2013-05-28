package org.activiti.examples.bpmn.tasklistener;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class GlobalTaskListenerTest extends PluggableActivitiTestCase{
	
	@Deployment
	public void testGlobalListener () {
		ProcessInstance instance = runtimeService.startProcessInstanceByKey("globalTaskListenerTest");
		assertEquals(runtimeService.getVariable(instance.getProcessInstanceId(), "executeCount") , 3);
	}

}
