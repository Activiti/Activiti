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

package org.activiti.camel.examples.multiinstance;

/**
 * @author Saeid Mirzaei  
 */

import java.util.List;

import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class MultiInstanceTest extends SpringActivitiTestCase {

  @Autowired
  protected CamelContext camelContext;

  public void  setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

  		@Override
  		public void configure() throws Exception {
  		  from("activiti:miProcessExample:serviceTask1").to("seda:continueAsync1");
  		  from("seda:continueAsync1").to("bean:sleepBean?method=sleep").to("activiti:miProcessExample:receive1");
  		}
  	});
  }

  @Deployment(resources = {"process/multiinstanceReceive.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miProcessExample");
    List<Job> jobList = managementService.createJobQuery().list();
    assertEquals(5, jobList.size());
    
    assertEquals(5, runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .activityId("serviceTask1").count());
    
    waitForJobExecutorToProcessAllJobs(3000, 500);
    int counter = 0;
    long processInstanceCount = 1;
    while (processInstanceCount == 1 && counter < 20) {
      Thread.sleep(500);
      processInstanceCount = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count();
      counter++;
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
}
