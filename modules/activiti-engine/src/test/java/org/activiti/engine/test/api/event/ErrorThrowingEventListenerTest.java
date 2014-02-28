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
package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.bpmn.helper.ErrorThrowingEventListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEventListener}s that throws an error BPMN event when an {@link ActivitiEvent}
 * has been dispatched.
 * 
 * @author Frederik Heremans
 */
public class ErrorThrowingEventListenerTest extends PluggableActivitiTestCase {

	@Deployment
	public void testThrowError() throws Exception {
		ErrorThrowingEventListener listener = null;
		try {
			listener = new ErrorThrowingEventListener();
			
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
			assertNotNull(processInstance);
			
			// Fetch the task and assign it. Should cause error-event to be dispatched
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("userTask")
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
	
			// Error-handling should have been called, and "escalate" task should be available instead of original one
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("escalatedTask")
					.singleResult();
			assertNotNull(task);
			
			
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	@Deployment
	public void testThrowErrorDefinedInProcessDefinition() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
		assertNotNull(processInstance);
		
		// Fetch the task and assign it. Should cause error-event to be dispatched
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey("userTask")
				.singleResult();
		assertNotNull(task);
		taskService.setAssignee(task.getId(), "kermit");

		// Error-handling should have been called, and "escalate" task should be available instead of original one
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey("escalatedTask")
				.singleResult();
		assertNotNull(task);
	}
	
	@Deployment
	public void testThrowErrorWithErrorcode() throws Exception {
		ErrorThrowingEventListener listener = null;
		try {
			listener = new ErrorThrowingEventListener();
			listener.setErrorCode("123");
			
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
			assertNotNull(processInstance);
			
			// Fetch the task and assign it. Should cause error-event to be dispatched
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("userTask")
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
	
			// Error-handling should have been called, and "escalate" task should be available instead of original one
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("escalatedTask")
					.singleResult();
			assertNotNull(task);
			
			// Try with a different error-code, resulting in a different task being created
			listener.setErrorCode("456");
			
			processInstance = runtimeService.startProcessInstanceByKey("testError");
			assertNotNull(processInstance);
			
			// Fetch the task and assign it. Should cause error-event to be dispatched
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("userTask")
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
			
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("escalatedTask2")
					.singleResult();
			assertNotNull(task);
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	@Deployment
	public void testThrowErrorWithErrorcodeDefinedInProcessDefinition() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testError");
		assertNotNull(processInstance);
		
		// Fetch the task and assign it. Should cause error-event to be dispatched
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey("userTask")
				.singleResult();
		assertNotNull(task);
		taskService.setAssignee(task.getId(), "kermit");

		// Error-handling should have been called, and "escalate" task should be available instead of original one
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.taskDefinitionKey("escalatedTask")
				.singleResult();
		assertNotNull(task);
	}
}
