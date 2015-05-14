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

/**
 * @author Saeid Mirzaei  
 */

import java.util.List;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class AsyncProcessTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;

  public void  setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

       @Override
       public void configure() throws Exception {
    	    from("activiti:asyncCamelProcess:serviceTaskAsync1").setHeader("destination", constant("activiti:asyncCamelProcess:receive1")).to("seda:asyncQueue");
    	    from("activiti:asyncCamelProcess:serviceTaskAsync2").setHeader("destination", constant("activiti:asyncCamelProcess:receive2")).to("seda:asyncQueue2");
    	    from("seda:asyncQueue").to("bean:sleepBean?method=sleep").to("seda:receiveQueue");
    	    
    	    from("seda:asyncQueue2").to("bean:sleepBean?method=sleep").to("seda:receiveQueue");
    	    
    	    from("seda:receiveQueue").recipientList(header("destination"));
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


  @Deployment(resources = {"process/async.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncCamelProcess");
    List<Execution> executionList = runtimeService.createExecutionQuery().list();
    assertEquals(3, executionList.size());
    Thread.sleep(4000);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
}
