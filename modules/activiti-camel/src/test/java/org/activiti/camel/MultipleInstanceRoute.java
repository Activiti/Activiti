package org.activiti.camel;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class MultipleInstanceRoute extends SpringActivitiTestCase {
	
    @Autowired
    CamelContext camelContext;

    public void  setUp() throws Exception {
          camelContext.addRoutes(new RouteBuilder() {

		  @Override
		  public void configure() throws Exception {
			 from("activiti:multiInstanceCamelProcess:servicetask1").to("log:logMessage");
//		 	 from("direct:startWithInitiatorHeader")
//	           .setHeader("CamelProcessInitiatorHeader", constant("kermit"))
//	           .to("activiti:InitiatorCamelCallProcess?processInitiatorHeaderName=CamelProcessInitiatorHeader");	
			
		 }
   	  });
    }
	

	@Deployment(resources = {"process/multiInstanceCamel.bpmn20.xml"})
	public void testCamelBody() throws Exception {
			runtimeService.startProcessInstanceByKey("multiInstanceCamelProcess");
	}

}
