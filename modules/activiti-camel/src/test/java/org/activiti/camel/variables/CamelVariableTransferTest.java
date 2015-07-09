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
package org.activiti.camel.variables;

import java.util.List;
import java.util.Map;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.task.Task;
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

/**
 * 
 * @author Saeid Mirzaei
 */
@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class CamelVariableTransferTest extends SpringActivitiTestCase {
  @Autowired
  protected CamelContext camelContext;
  
  @Autowired
  protected TaskService taskService;

  protected MockEndpoint service1;
  
  public void setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

      @Override
      public void configure() throws Exception {
        from("direct:startAllProperties")
          .setProperty("property1", simple("sampleValueForProperty1"))
          .setProperty("property2", simple("sampleValueForProperty2"))
          .setProperty("property3", simple("sampleValueForProperty3"))
          .transform(simple("sampleBody"))
          .to("log:testVariables?showProperties=true")
          .to("activiti:testPropertiesProcess?copyVariablesFromProperties=true");

        from("direct:startNoProperties")
        .setProperty("property1", simple("sampleValueForProperty1"))
        .setProperty("property2", simple("sampleValueForProperty2"))
        .setProperty("property3", simple("sampleValueForProperty3"))
        .transform(simple("sampleBody"))
        .to("log:testVariables?showProperties=true")
        .to("activiti:testPropertiesProcess?copyVariablesFromProperties=false");

        from("direct:startFilteredProperties")
        .setProperty("property1", simple("sampleValueForProperty1"))
        .setProperty("property2", simple("sampleValueForProperty2"))
        .setProperty("property3", simple("sampleValueForProperty3"))
        .to("log:testVariables?showProperties=true")
        .to("activiti:testPropertiesProcess?copyVariablesFromProperties=(property1|property2)"); 
        
        from("direct:startAllHeaders")
        .setHeader("property1", simple("sampleValueForProperty1"))
        .setHeader("property2", simple("sampleValueForProperty2"))
        .setHeader("property3", simple("sampleValueForProperty3"))
        .to("log:testVariables?showProperties=true");

        from("direct:startNoHeaders")
        .setHeader("property1", simple("sampleValueForProperty1"))
        .setHeader("property2", simple("sampleValueForProperty2"))
        .setHeader("property3", simple("sampleValueForProperty3"))
        .to("log:testVariables?showProperties=true")
        .to("activiti:testPropertiesProcess?copyVariablesFromHeader=false");   
        
        from("direct:startFilteredHeaders")
        .setHeader("property1", simple("sampleValueForProperty1"))
        .setHeader("property2", simple("sampleValueForProperty2"))
        .setHeader("property3", simple("sampleValueForProperty3"))
        .to("log:testVariables?showProperties=true")
        .to("activiti:testPropertiesProcess?copyVariablesFromHeader=(property1|property2)");   
 
        
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
  
  
// check that at least all properties are passed from camel to activiti when copyVariablesFromProperties=true is simply true
  @Deployment
  public void testCamelPropertiesAll() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();
    tpl.send("direct:startAllProperties", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals("sampleValueForProperty1", variables.get("property1"));
    assertEquals("sampleValueForProperty2", variables.get("property2"));
    assertEquals("sampleValueForProperty3", variables.get("property3"));
  }
  
  
//check that body will be copied into variables even if copyVariablesFromProperties=true 
 @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
 public void testCamelPropertiesAndBody() throws Exception {
   ProducerTemplate tpl = camelContext.createProducerTemplate();
   Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();


   tpl.send("direct:startAllProperties", exchange);
   
   assertNotNull(taskService);
   assertNotNull(runtimeService);
   assertEquals(1, taskService.createTaskQuery().count());
   Task task = taskService.createTaskQuery().singleResult();
   assertNotNull(task);
   Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
   assertEquals("sampleValueForProperty1", variables.get("property1"));
   assertEquals("sampleValueForProperty2", variables.get("property2"));
   assertEquals("sampleValueForProperty3", variables.get("property3"));
   assertEquals("sampleBody", variables.get("camelBody"));
 }
  
  @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
  public void testCamelPropertiesFiltered() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startFilteredProperties").createExchange();
    tpl.send("direct:startFilteredProperties", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals("sampleValueForProperty1", variables.get("property1"));
    assertEquals("sampleValueForProperty2", variables.get("property2"));
    assertNull(variables.get("property3"));
  }

  @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
  public void testCamelPropertiesNone() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startNoProperties").createExchange();
    tpl.send("direct:startNoProperties", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertNull(variables.get("property1"));
    assertNull(variables.get("property2"));
    assertNull(variables.get("property3"));
  }
  
  @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
  public void testCamelHeadersAll() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startAllProperties").createExchange();
    tpl.send("direct:startAllProperties", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals("sampleValueForProperty1", variables.get("property1"));
    assertEquals("sampleValueForProperty2", variables.get("property2"));
    assertEquals("sampleValueForProperty3", variables.get("property3"));
  }

  @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
  public void testCamelHeadersFiltered() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startFilteredHeaders").createExchange();
    tpl.send("direct:startFilteredHeaders", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertEquals("sampleValueForProperty1", variables.get("property1"));
    assertEquals("sampleValueForProperty2", variables.get("property2"));
    assertNull(variables.get("property3"));
  }

  @Deployment(resources = {"org/activiti/camel/variables/CamelVariableTransferTest.testCamelPropertiesAll.bpmn20.xml"})
  public void testCamelHeadersNone() throws Exception {
    ProducerTemplate tpl = camelContext.createProducerTemplate();
    Exchange exchange = camelContext.getEndpoint("direct:startNoHeaders").createExchange();
    tpl.send("direct:startNoHeaders", exchange);
    
    assertNotNull(taskService);
    assertNotNull(runtimeService);
    assertEquals(1, taskService.createTaskQuery().count());
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
    assertNull(variables.get("property1"));
    assertNull(variables.get("property2"));
    assertNull(variables.get("property3"));
  }


  

}
