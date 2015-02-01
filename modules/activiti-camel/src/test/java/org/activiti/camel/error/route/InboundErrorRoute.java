package org.activiti.camel.error.route;

import org.activiti.camel.util.TimeConsumingService;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;


/**
 * @author stefan.schulze@accelsis.biz
 * 
 */
public class InboundErrorRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("seda:inbound").routeId("inbound")
				// give Activiti some time to reach synchronization point
				.bean(TimeConsumingService.class)
				.log(LoggingLevel.INFO, "Returning result ...")
				.to("activiti:ErrorHandling:ReceiveResult");

		from("seda:dlq").routeId("dlq").log(LoggingLevel.INFO,
				"Error handled by camel ...");
	}
}
