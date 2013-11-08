package org.activiti.camel.examples.pingPong;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class PingPongTest extends SpringActivitiTestCase {
	
	@Deployment
	public void testPingPong() {
		Map<String, Object> variables = new HashMap<String, Object>();
	
		variables.put("input", "Hello");
		Map<String, String> outputMap = new HashMap<String, String>();
		variables.put("outputMap", outputMap);
		
		runtimeService.startProcessInstanceByKey("PingPongProcess", variables);
		assertEquals(1, outputMap.size());
		assertNotNull(outputMap.get("outputValue"));
		assertEquals("Hello World", outputMap.get("outputValue"));
		
	}

}
