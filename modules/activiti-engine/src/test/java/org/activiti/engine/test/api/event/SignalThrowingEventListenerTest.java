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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.bpmn.helper.SignalThrowingEventListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEventListener}s that throws a signal BPMN event when an {@link ActivitiEvent}
 * has been dispatched.
 * 
 * @author Frederik Heremans
 */
public class SignalThrowingEventListenerTest extends PluggableActivitiTestCase {


	@Deployment
	public void testThrowSignal() throws Exception {
		SignalThrowingEventListener listener = null;
		try {
			listener = new SignalThrowingEventListener();
			listener.setSignalName("Signal");
			listener.setProcessInstanceScope(true);
			
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
			assertNotNull(processInstance);
			
			// Fetch the task and re-assign it to trigger the event-listener
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
			
			// Boundary-event should have been signaled and a new task should be available, on top of the already
			// existing one, since the cancelActivity='false'
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("subTask")
					.singleResult();
			assertNotNull(task);
			assertEquals("kermit", task.getAssignee());
			
			Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("boundaryTask")
					.singleResult();
			assertNotNull(boundaryTask);
			
			
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	@Deployment
	public void testThrowSignalDefinedInProcessDefinition() throws Exception {
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
			assertNotNull(processInstance);
			
			// Fetch the task and re-assign it to trigger the event-listener
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
			
			// Boundary-event should have been signaled and a new task should be available, on top of the already
			// existing one, since the cancelActivity='false'
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("subTask")
					.singleResult();
			assertNotNull(task);
			assertEquals("kermit", task.getAssignee());
			
			Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("boundaryTask")
					.singleResult();
			assertNotNull(boundaryTask);
	}
	
	@Deployment
	public void testThrowSignalInterrupting() throws Exception {
		SignalThrowingEventListener listener = null;
		try {
			listener = new SignalThrowingEventListener();
			listener.setSignalName("Signal");
			listener.setProcessInstanceScope(true);
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
			assertNotNull(processInstance);
			
			// Fetch the task and re-assig it to trigger the event-listener
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
			assertNotNull(task);
			taskService.setAssignee(task.getId(), "kermit");
			
			// Boundary-event should have been signalled and a new task should be available, the already
			// existing one is gone, since the cancelActivity='true'
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("subTask")
					.singleResult();
			assertNull(task);
			
			Task boundaryTask = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.taskDefinitionKey("boundaryTask")
					.singleResult();
			assertNotNull(boundaryTask);
			
			
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	/**
	 * Test signal throwing when a job failed and the retries are decremented, affectively
	 * starting a new transaction. 
	 */
	@Deployment
	public void testThrowSignalInNewTransaction() throws Exception {
		SignalThrowingEventListener listener = null;
		try {
		  listener = new SignalThrowingEventListener();
			listener.setSignalName("Signal");
			listener.setProcessInstanceScope(true);
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.JOB_RETRIES_DECREMENTED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
			assertNotNull(processInstance);
			
			Job signalJob = managementService.createJobQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
			
			try {
        managementService.executeJob(signalJob.getId());
        fail("Exception expected");
      } catch(ActivitiException ae) {
        // Ignore, expected exception
      }
			
			Job failedJob = managementService.createJobQuery()
					.withException()
					.processInstanceId(processInstance.getId())
					.singleResult();
			
			assertNotNull(failedJob);
			assertEquals(2, failedJob.getRetries());
			
			// One retry should have triggered dispatching of a retry-decrement event
			assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).count());
			
			try {
				managementService.executeJob(failedJob.getId());
				fail("Exception expected");
			} catch(ActivitiException ae) {
				// Ignore, expected exception
				assertEquals(2, taskService.createTaskQuery().processInstanceId(processInstance.getId()).count());
			}
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	/**
	 * Test signal throwing when a job failed, signaling will happen in the rolled back transaction,
	 * not doing anything in the end...
	 */
	@Deployment(resources = {"org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.testThrowSignalInNewTransaction.bpmn20.xml"})
	public void testThrowSignalInRolledbackTransaction() throws Exception {
		SignalThrowingEventListener listener = null;
		
		try {
			listener = new SignalThrowingEventListener();
			listener.setSignalName("Signal");
			listener.setProcessInstanceScope(true);
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.JOB_EXECUTION_FAILURE);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignal");
			assertNotNull(processInstance);
			
			Job signalJob = managementService.createJobQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
			
			try {
        managementService.executeJob(signalJob.getId());
        fail("Exception expected");
      } catch(ActivitiException ae) {
        // Ignore, expected exception
      }
			
			Job failedJob = managementService.createJobQuery()
					.withException()
					.processInstanceId(processInstance.getId())
					.singleResult();
			
			assertNotNull(failedJob);
			assertEquals(2, failedJob.getRetries());
			
			// Three retries should each have triggered dispatching of a retry-decrement event
			assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).count());
			
			try {
				managementService.executeJob(failedJob.getId());
				fail("Exception expected");
			} catch(ActivitiException ae) {
				// Ignore, expected exception
				assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).count());
			}
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
	
	/**
	 * Test if an engine-wide signal is thrown as response to a dispatched event.   
	 */
	@Deployment(resources = {
			"org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignal.bpmn20.xml",
			"org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalExternalProcess.bpmn20.xml"
	})
	public void testGlobalSignal() throws Exception {
		SignalThrowingEventListener listener = null;
		
		try {
			listener = new SignalThrowingEventListener();
			listener.setSignalName("Signal");
			listener.setProcessInstanceScope(false);
			processEngineConfiguration.getEventDispatcher().addEventListener(listener, ActivitiEventType.TASK_ASSIGNED);
			
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("globalSignalProcess");
			assertNotNull(processInstance);
			
			ProcessInstance externalProcess = runtimeService.startProcessInstanceByKey("globalSignalProcessExternal");
			assertNotNull(processInstance);
			// Make sure process is not ended yet by querying it again
			externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId())
					.singleResult();
			assertNotNull(externalProcess);
			
			
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
			assertNotNull(task);
			
			// Assign task to trigger signal
			taskService.setAssignee(task.getId(), "kermit");
			
			// Second process should have been signaled
			externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId())
					.singleResult();
			assertNull(externalProcess);
			
			// Task assignee should still be set
			task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
					.singleResult();
			assertNotNull(task);
			assertEquals("kermit", task.getAssignee());
			
		} finally {
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
		
	}
	
	/**
	 * Test if an engine-wide signal is thrown as response to a dispatched event.   
	 */
	@Deployment(resources = {
			"org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalDefinedInProcessDefinition.bpmn20.xml",
			"org/activiti/engine/test/api/event/SignalThrowingEventListenerTest.globalSignalExternalProcess.bpmn20.xml"
	})
	public void testGlobalSignalDefinedInProcessDefinition() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("globalSignalProcess");
		assertNotNull(processInstance);
		
		ProcessInstance externalProcess = runtimeService.startProcessInstanceByKey("globalSignalProcessExternal");
		assertNotNull(processInstance);
		// Make sure process is not ended yet by querying it again
		externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId())
				.singleResult();
		assertNotNull(externalProcess);
		
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		
		// Assign task to trigger signal
		taskService.setAssignee(task.getId(), "kermit");
		
		// Second process should have been signaled
		externalProcess = runtimeService.createProcessInstanceQuery().processInstanceId(externalProcess.getId())
				.singleResult();
		assertNull(externalProcess);
		
		// Task assignee should still be set
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		assertEquals("kermit", task.getAssignee());
	}
}
