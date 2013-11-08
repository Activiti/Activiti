package org.activiti.camel.examples.pingPong;

import org.apache.camel.builder.RouteBuilder;

public class PingPongRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("activiti:PingPongProcess:ping").transform().simple("${property.input} World");
		
	}

}
