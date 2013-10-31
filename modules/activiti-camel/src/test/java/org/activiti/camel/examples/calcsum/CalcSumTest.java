package org.activiti.camel.examples.calcsum;

import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class CalcSumTest extends SpringActivitiTestCase {
  
  @Test
  public void testCalcSumOfNumbers() {
	  CamelContext ctx = applicationContext.getBean(CamelContext.class);
	    service1 = (MockEndpoint) ctx.getEndpoint("mock:serviceBehavior");
	    service1.reset();
	    
	  ProducerTemplate template context.createProducerTemplate();;
	  template.sendBody("direct:start", "This is a test message");  
    System.out.println("Hello World");
  }

}
