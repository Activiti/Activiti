package org.activiti.examples.bpmn.tasklistener;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class GlobalTaskListenerTest extends PluggableActivitiTestCase{
	
	@Deployment
	public void testGlobalListener () {
		 runtimeService.startProcessInstanceByKey("globalTaskListenerTest");
	}

}
