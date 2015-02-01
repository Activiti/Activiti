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

import java.util.HashMap;
import java.util.Map;

import org.activiti.camel.util.Routing;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * Demonstrates an issue in Activiti 5.12. Exception on Camel routes will be lost, 
 * if default error handling is used. 
 * 
 * @author stefan.schulze@accelsis.biz
 *
 */
@ContextConfiguration("classpath:error-camel-activiti-context.xml")
public class ErrorHandlingTest extends SpringActivitiTestCase {

  private static final int WAIT = 3000;
  
	private static final String PREVIOUS_WAIT_STATE = "LogProcessStart";
	private static final String NEXT_WAIT_STATE = "ReceiveResult";
	
	/**
	 * Process instance should be removed after completion. Works as intended, 
	 * if no exception interrupts the Camel route. 
	 * 
	 * @throws Exception
	 */
	@Deployment(resources = {"process/errorHandling.bpmn20.xml"})
	public void testCamelRouteWorksAsIntended() throws Exception {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("routing", Routing.DEFAULT);
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
		    "ErrorHandling", variables);
	    
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());
    
    Thread.sleep(WAIT);
    
    assertEquals("Process instance not completed", 0, runtimeService.createProcessInstanceQuery()
    		.processInstanceId(processInstance.getId())
    		.count());
	}
	
	/**
	 * Expected behavior, with  default error handling in Camel: 
	 * Roll-back to previous wait state. Fails with Activiti 5.12.
	 * 
	 * @throws Exception
	 */
	@Deployment(resources = {"process/errorHandling.bpmn20.xml"})
	public void testRollbackOnException() throws Exception {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("routing", Routing.PROVOKE_ERROR);
		
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        "ErrorHandling", variables);
    
    assertEquals("No roll-back to previous wait state", 1, runtimeService.createExecutionQuery()
    		.processInstanceId(processInstance.getId())
    		.activityId(PREVIOUS_WAIT_STATE)
    		.count());
    
    assertEquals("Process instance advanced to next wait state", 0, runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .activityId(NEXT_WAIT_STATE)
        .count());
	}
	
	/**
	 * Exception caught and processed by Camel dead letter queue handler. 
	 * Process instance proceeds to ReceiveTask as expected.
	 * 
	 * @throws Exception
	 */
	@Deployment(resources = {"process/errorHandling.bpmn20.xml"})
	public void testErrorHandledByCamel() throws Exception {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("routing", Routing.HANDLE_ERROR);
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
		    "ErrorHandling", variables);
		
		Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());
    
    Thread.sleep(WAIT);
	    
    assertEquals("Process instance did not reach next wait state", 1, runtimeService.createExecutionQuery()
    		.processInstanceId(processInstance.getId())
    		.activityId(NEXT_WAIT_STATE)
    		.count());
	}
}
