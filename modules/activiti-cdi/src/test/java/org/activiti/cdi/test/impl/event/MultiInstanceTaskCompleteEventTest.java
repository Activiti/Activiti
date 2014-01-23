package org.activiti.cdi.test.impl.event;

import static org.junit.Assert.assertEquals;

import org.activiti.cdi.test.CdiActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class MultiInstanceTaskCompleteEventTest extends CdiActivitiTestCase {

	@Test
	@Deployment(resources = { "org/activiti/cdi/test/impl/event/MultiInstanceTaskCompleteEventTest.process1.bpmn20.xml.bpmn" })
	public void testReceiveAll() {

		TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
	    listenerBean.reset();
	    
	    assertEquals(0, listenerBean.getCreateTask1());
	    assertEquals(0, listenerBean.getAssignTask1());
	    assertEquals(0, listenerBean.getCompleteTask1());
	    
	    // start the process
	    runtimeService.startProcessInstanceByKey("process1");
	    
	    Task task = taskService.createTaskQuery().singleResult();
	    
	    taskService.claim(task.getId(), "auser");
	    taskService.complete(task.getId());
	    
	    task = taskService.createTaskQuery().singleResult();
	    taskService.complete(task.getId());
	    
	    // assert
	    assertEquals(2, listenerBean.getCreateTask1());
	    assertEquals(1, listenerBean.getAssignTask1());
	    assertEquals(2, listenerBean.getCompleteTask1());

	}
}
