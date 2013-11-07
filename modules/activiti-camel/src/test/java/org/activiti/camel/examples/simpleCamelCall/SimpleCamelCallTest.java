package org.activiti.camel.examples.simpleCamelCall;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:org/activiti/camel/examples/simpleCamelCall/simpleCamelCall-context.xml")
public class SimpleCamelCallTest extends SpringActivitiTestCase {
  
 
  @Deployment
  public void testSimpleCamelCall() {
	  runtimeService.startProcessInstanceByKey("SimpleCamelCallProcess");
  }

}
