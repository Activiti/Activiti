package org.activiti.camel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class CamelVariableBodyTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;

  protected MockEndpoint service1;
	
  public void setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

	 		@Override
	 		public void configure() throws Exception {
				from("activiti:HelloCamel:serviceTask1")
				  .log(LoggingLevel.INFO,"Received message on service task")
				  .to("mock:serviceBehavior");					
	 		}
    });	  
    service1 = (MockEndpoint) camelContext.getEndpoint("mock:serviceBehavior");
    service1.reset();
  }
  
  public void tearDown() throws Exception {
    List<Route> routes = camelContext.getRoutes();
    for (Route r: routes) {
      camelContext.stopRoute(r.getId());
      camelContext.removeRoute(r.getId());
    }
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
