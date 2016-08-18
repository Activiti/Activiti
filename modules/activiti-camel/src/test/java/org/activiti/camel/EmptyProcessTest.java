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

import java.util.List;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class EmptyProcessTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;
  	
  @BeforeClass
  public void setUp() throws Exception {
	  camelContext.addRoutes(new RouteBuilder() {

	    @Override
	    public void configure() throws Exception {
	      from("direct:startEmpty").to("activiti:emptyProcess");
	      from("direct:startEmptyWithHeader").setHeader("MyVar", constant("Foo")).to("activiti:emptyProcess?copyVariablesFromHeader=true");
	      from("direct:startEmptyBodyAsString").to("activiti:emptyProcess?copyBodyToCamelBodyAsString=true");
	    }
	  });
  }
  
  public void tearDown() throws Exception {
    List<Route> routes = camelContext.getRoutes();
    for (Route r: routes) {
      camelContext.stopRoute(r.getId());
      camelContext.removeRoute(r.getId());
    }
  }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testRunProcessWithHeader() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    String body = "body text";
    Exchange exchange = ctx.getEndpoint("direct:startEmptyWithHeader").createExchange();
    exchange.getIn().setBody(body);
    tpl.send("direct:startEmptyWithHeader", exchange);

    String instanceId = (String) exchange.getProperty("PROCESS_ID_PROPERTY");
    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(body, var.getValue());
    var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("MyVar").singleResult();
    assertNotNull(var);
    assertEquals("Foo", var.getValue());
  }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testObjectAsVariable() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    Object expectedObj = new Long(99);
    Exchange exchange = ctx.getEndpoint("direct:startEmpty").createExchange();
    exchange.getIn().setBody(expectedObj);
    tpl.send("direct:startEmpty", exchange);    
    String instanceId = (String) exchange.getProperty("PROCESS_ID_PROPERTY");
    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(expectedObj, var.getValue());
  }
  
  @Deployment(resources = {"process/empty.bpmn20.xml"})
  public void testObjectAsStringVariable() throws Exception {
    CamelContext ctx = applicationContext.getBean(CamelContext.class);
    ProducerTemplate tpl = ctx.createProducerTemplate();
    Object expectedObj = new Long(99);
    
    Exchange exchange = ctx.getEndpoint("direct:startEmptyBodyAsString").createExchange();
    exchange.getIn().setBody(expectedObj);
    tpl.send("direct:startEmptyBodyAsString", exchange);
    
    String instanceId = (String) exchange.getProperty("PROCESS_ID_PROPERTY");

    assertProcessEnded(instanceId);
    HistoricVariableInstance var = processEngine.getHistoryService().createHistoricVariableInstanceQuery().variableName("camelBody").singleResult();
    assertNotNull(var);
    assertEquals(expectedObj.toString(), var.getValue().toString());
  }
}
