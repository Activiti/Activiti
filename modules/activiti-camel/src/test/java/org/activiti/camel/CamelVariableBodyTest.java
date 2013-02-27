package org.activiti.camel;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class CamelVariableBodyTest extends SpringActivitiTestCase {
	
	MockEndpoint service1;
	
  public void setUp() {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    service1 = (MockEndpoint) ctx.getEndpoint("mock:serviceBehavior");
    service1.reset();
  }
	
	@Deployment(resources = {"process/HelloCamelBody.bpmn20.xml"})
	public void testCamelBody() throws Exception {
	  service1.expectedBodiesReceived("hello world");
		Map<String, Object> varMap = new HashMap<String, Object>();
		varMap.put("camelBody", "hello world");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HelloCamel", varMap);
		//Ensure that the variable is equal to the expected value.
		assertEquals("hello world", runtimeService.getVariable(processInstance.getId(), "camelBody"));
		service1.assertIsSatisfied();
		
		Task task = taskService.createTaskQuery().singleResult();
		
		//Ensure that the name of the task is correct.
		assertEquals("Hello Task", task.getName());
		
		//Complete the task.
		taskService.complete(task.getId());
	}
	
}
