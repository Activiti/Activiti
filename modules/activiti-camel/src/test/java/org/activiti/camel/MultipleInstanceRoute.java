package org.activiti.camel;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:camel-activiti-context.xml")
public class MultipleInstanceRoute extends SpringActivitiTestCase {
	


	@Deployment(resources = {"process/multiInstanceCamel.bpmn20.xml"})
	public void testCamelBody() throws Exception {
		try {
			runtimeService.startProcessInstanceByKey("multiInstanceCamelProcess");
		} catch (Exception e) {
			fail("there is an exception in multi instance camel activiti");
		}
		
	}

}
