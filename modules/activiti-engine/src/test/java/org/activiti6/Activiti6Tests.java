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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

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

}
