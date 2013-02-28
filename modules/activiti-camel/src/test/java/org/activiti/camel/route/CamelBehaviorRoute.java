package org.activiti.camel.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelBehaviorRoute extends SpringRouteBuilder {
	
	public void configure() {
	  
		from("activiti:HelloCamel:serviceTask1")
			.log(LoggingLevel.INFO,"Received message on service task")
			.to("mock:serviceBehavior");
	}

}
