/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.camel.error.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * Creates routes with different behaviors.
 * 
 * @author stefan.schulze@accelsis.biz
 *
 */
public class OutboundErrorRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		// no problems
		from("activiti:ErrorHandling:NormalExecution").routeId("outbound")
			.log(LoggingLevel.INFO, "Normal execution")
			.to("seda:inbound");
		
		// always fails with default error handling: propagates exception back to the caller
		from("activiti:ErrorHandling:ProvokeError").routeId("error")
			.log(LoggingLevel.INFO, "Provoked error")
			.beanRef("brokenService") // <-- throws Exception
			.to("seda:inbound");
		
		// always fails with specific error handler: exception is not propagated back
		from("activiti:ErrorHandling:HandleError").routeId("errorWithDlq")
			.errorHandler(deadLetterChannel("seda:dlq"))
			.log(LoggingLevel.INFO, "Provoked error")
			.beanRef("brokenService") // <-- throws Exception
			.to("seda:inbound");		
	}
	
}
