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

package org.activiti.camel.revisited;

/**
 * @author Saeid Mirzaei  
 */

import java.util.List;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class AsyncProcessRevisitedTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;

  public void  setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

  		@Override
  		public void configure() throws Exception {
  		  from("activiti:asyncCamelProcessRevisited:serviceTaskAsync1").to("bean:sleepBean?method=sleep").to("seda:continueAsync1");
  		  from("seda:continueAsync1").to("activiti:asyncCamelProcessRevisited:receive1");
  		    
  		  from("activiti:asyncCamelProcessRevisited:serviceTaskAsync2").to("bean:sleepBean?method=sleep").to("bean:sleepBean?method=sleep").to("seda:continueAsync2");    
        from("seda:continueAsync2").to("activiti:asyncCamelProcessRevisited:receive2");
  		}
  	});
  }

  @Deployment(resources = {"process/revisited/async-revisited.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncCamelProcessRevisited");
    List<Execution> executionList = runtimeService.createExecutionQuery().list();
    assertEquals(3, executionList.size());
    waitForJobExecutorToProcessAllJobs(10000, 500);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
}
