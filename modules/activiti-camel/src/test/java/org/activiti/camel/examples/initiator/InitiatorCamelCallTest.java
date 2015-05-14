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

package org.activiti.camel.examples.initiator;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class InitiatorCamelCallTest extends SpringActivitiTestCase {
  
  @Autowired
  protected CamelContext camelContext;

  public void setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("direct:startWithInitiatorHeader")
          .setHeader("CamelProcessInitiatorHeader", constant("kermit"))
          .to("activiti:InitiatorCamelCallProcess?processInitiatorHeaderName=CamelProcessInitiatorHeader");
        }
    });
  }
	
  @Deployment
  public void testInitiatorCamelCall() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    String body = "body text";
    
    Exchange exchange = ctx.getEndpoint("direct:startWithInitiatorHeader").createExchange();
    exchange.getIn().setBody(body);
    tpl.send("direct:startWithInitiatorHeader", exchange);
    
    String instanceId = (String) exchange.getProperty("PROCESS_ID_PROPERTY");

    

    String initiator = (String) runtimeService.getVariable(instanceId, "initiator");
    assertEquals("kermit", initiator);
    
    Object camelInitiatorHeader = runtimeService.getVariable(instanceId, "CamelProcessInitiatorHeader");
    assertNull(camelInitiatorHeader);
  }

}
