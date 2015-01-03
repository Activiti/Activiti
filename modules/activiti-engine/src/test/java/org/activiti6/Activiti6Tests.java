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
package org.activiti6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class Activiti6Tests extends AbstractActvitiTest {

	@Test
	public void simplestProcessPossible() {
		repositoryService
		        .createDeployment()
		        .addClasspathResource("org/activiti6/Activiti6Tests.simplestProcessPossible.bpmn20.xml")
		        .deploy();

		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd");
		assertNotNull(processInstance);
		assertTrue(processInstance.isEnded());

		// Cleanup
		for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testOneTaskProcess() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("The famous task", task.getName());
		assertEquals("kermit", task.getAssignee());
		
		taskService.complete(task.getId());
	}
	
	@Test
	@org.activiti.engine.test.Deployment(resources="org/activiti6/Activiti6Tests.testOneTaskProcess.bpmn20.xml")
	public void testOneTaskProcessCleanupInMiddleOfProcess() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("The famous task", task.getName());
		assertEquals("kermit", task.getAssignee());
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testSimpleParallelGateway() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelGateway");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("simpleParallelGateway").orderByTaskName().asc().list();
		assertEquals(2, tasks.size());
		assertEquals("Task a", tasks.get(0).getName());
		assertEquals("Task b", tasks.get(1).getName());
		
		for (Task task : tasks) {
			taskService.complete(task.getId());
		}
		
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testSimpleNestedParallelGateway() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelGateway");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("simpleParallelGateway").orderByTaskName().asc().list();
		assertEquals(4, tasks.size());
		assertEquals("Task a", tasks.get(0).getName());
		assertEquals("Task b1", tasks.get(1).getName());
		assertEquals("Task b2", tasks.get(2).getName());
		assertEquals("Task c", tasks.get(3).getName());
		
		for (Task task : tasks) {
			taskService.complete(task.getId());
		}
		
		assertEquals(0, runtimeService.createProcessInstanceQuery().count());
	}
	
	/*
	 * This fails on Activiti 5
	 */
	@Test
	@org.activiti.engine.test.Deployment
	public void testLongServiceTaskLoop() {
		int maxCount = 1000; // You can make this as big as you want (as long as it still fits within transaction timeouts). Go on, try it!
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("counter", new Integer(0));
		vars.put("maxCount", maxCount);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLongServiceTaskLoop", vars);
		assertNotNull(processInstance);
		assertTrue(processInstance.isEnded());
		
		assertEquals(maxCount, CountingServiceTaskTestDelegate.CALL_COUNT.get());
		assertEquals(0, runtimeService.createExecutionQuery().count());
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testScriptTask() {
		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("a", 1);
		variableMap.put("b", 2);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variableMap);
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());

		assertEquals(3.0, runtimeService.getVariable(processInstance.getId(), "sum"));

		Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(execution);

		runtimeService.trigger(execution.getId());

		assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult());
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testSimpleTimerBoundaryEvent() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		Job job = managementService.createJobQuery().singleResult();
		managementService.executeJob(job.getId());
		
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("Task after timer", task.getName());
		
		taskService.complete(task.getId());
		assertEquals(0, runtimeService.createExecutionQuery().count());
	}
	
	@Test
	@org.activiti.engine.test.Deployment
	public void testSimpleTimerBoundaryEventTimerDoesNotFire() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleBoundaryTimer");
		assertNotNull(processInstance);
		assertFalse(processInstance.isEnded());
		
		assertEquals(1, managementService.createJobQuery().count());
		
		Task task = taskService.createTaskQuery().singleResult();
		assertEquals("The famous task", task.getName());
		taskService.complete(task.getId());
		
		assertEquals(0, managementService.createJobQuery().count());
		assertEquals(0, runtimeService.createExecutionQuery().count());
	}
	

}
