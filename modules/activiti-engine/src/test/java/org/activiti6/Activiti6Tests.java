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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.engine.repository.Deployment;
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

}
