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

package org.activiti.camel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class CustomContextTest extends SpringActivitiTestCase {

	@Autowired
	protected CamelContext camelContext;
	
	protected MockEndpoint service1;

	protected MockEndpoint service2;

	public void setUp() throws Exception {
   
    camelContext.addRoutes(new RouteBuilder() {

   		@Override
   		public void configure() throws Exception {	 
   		  from("direct:start").to("activiti:camelProcess");	   		
  
   		  from("activiti:camelProcess:serviceTask1").setBody().property("var1")
   	      .to("mock:service1").setProperty("var2").constant("var2")
   	      .setBody().properties();
  
   		  from("activiti:camelProcess:serviceTask2?copyVariablesToBodyAsMap=true")
   	      .to("mock:service2");
  
   		  from("direct:receive").to("activiti:camelProcess:receive");	   		  
   		}
   	});
	       
		service1 = (MockEndpoint) camelContext.getEndpoint("mock:service1");
		service1.reset();
		service2 = (MockEndpoint) camelContext.getEndpoint("mock:service2");
		service2.reset();
	}
	
  public void tearDown() throws Exception {
    List<Route> routes = camelContext.getRoutes();
    for (Route r: routes) {
      camelContext.stopRoute(r.getId());
      camelContext.removeRoute(r.getId());
    }
  }
	

	@Deployment(resources = { "process/custom.bpmn20.xml" })
	public void testRunProcess() throws Exception {
		CamelContext ctx = applicationContext.getBean(CamelContext.class);
		ProducerTemplate tpl = ctx.createProducerTemplate();
		service1.expectedBodiesReceived("ala");

		Exchange exchange = ctx.getEndpoint("direct:start").createExchange();
		exchange.getIn().setBody(Collections.singletonMap("var1", "ala"));
		tpl.send("direct:start", exchange);
		
		String instanceId = (String) exchange.getProperty("PROCESS_ID_PROPERTY");
	
		tpl.sendBodyAndProperty("direct:receive", null,
				ActivitiProducer.PROCESS_ID_PROPERTY, instanceId);

		assertProcessEnded(instanceId);

		service1.assertIsSatisfied();
		
		@SuppressWarnings("rawtypes")
    Map m = service2.getExchanges().get(0).getIn().getBody(Map.class);
		assertEquals("ala", m.get("var1"));
		assertEquals("var2", m.get("var2"));
	}
}
