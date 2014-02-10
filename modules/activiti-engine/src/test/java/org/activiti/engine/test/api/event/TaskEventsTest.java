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

import java.util.Date;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to tasks.
 * 
 * @author Frederik Heremans
 */
public class TaskEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;
	
	/**
	 * Check create, update and delete events for a task. 
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testTaskEventsInProcess() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		assertNotNull(processInstance);
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		
		// Check create event
		assertEquals(2, listener.getEventsReceived().size());
		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		Task taskFromEvent = (Task) event.getEntity(); 
		assertEquals(task.getId(), taskFromEvent.getId());
		assertExecutionDetails(event, processInstance);
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		listener.clearEventsReceived();
		
		// Update duedate, owner and priority should trigger update-event
		taskService.setDueDate(task.getId(), new Date());
		assertEquals(1, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertExecutionDetails(event, processInstance);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		listener.clearEventsReceived();
		
		taskService.setPriority(task.getId(), 12);
		assertEquals(1, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		assertExecutionDetails(event, processInstance);
		listener.clearEventsReceived();
		
		taskService.setOwner(task.getId(), "kermit");
		assertEquals(1, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		assertExecutionDetails(event, processInstance);
		listener.clearEventsReceived();
		
		// Updating detached task and calling saveTask should trigger a single update-event
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		
		task.setDueDate(new Date());
		task.setOwner("john");
		taskService.saveTask(task);
		
		assertEquals(1, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		assertExecutionDetails(event, processInstance);
		listener.clearEventsReceived();
		
		// Check delete-event on complete
		taskService.complete(task.getId());
		assertEquals(2, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.TASK_COMPLETED, event.getType());
		assertExecutionDetails(event, processInstance);
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		assertExecutionDetails(event, processInstance);
	}
	
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testTaskAssignmentEventInProcess() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		assertNotNull(processInstance);
		listener.clearEventsReceived();
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		
		// Set assignee through API
		taskService.setAssignee(task.getId(), "kermit");
		assertEquals(2, listener.getEventsReceived().size());
		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.TASK_ASSIGNED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		Task taskFromEvent = (Task) event.getEntity();
		assertEquals(task.getId(), taskFromEvent.getId());
		assertEquals("kermit", taskFromEvent.getAssignee());
		assertExecutionDetails(event, processInstance);
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		assertExecutionDetails(event, processInstance);
		listener.clearEventsReceived();
		
		// Set assignee through updateTask
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		task.setAssignee("newAssignee");
		taskService.saveTask(task);
		
		assertEquals(2, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.TASK_ASSIGNED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		taskFromEvent = (Task) event.getEntity();
		assertEquals(task.getId(), taskFromEvent.getId());
		assertEquals("newAssignee", taskFromEvent.getAssignee());
		assertExecutionDetails(event, processInstance);
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		assertExecutionDetails(event, processInstance);
		listener.clearEventsReceived();
	}
	
	/**
	 * Check events related to process instance delete and standalone task delete. 
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testDeleteEventDoesNotDispathComplete() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		assertNotNull(processInstance);
		listener.clearEventsReceived();
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		
		// Delete process, should delete task as well, but not complete
		runtimeService.deleteProcessInstance(processInstance.getId(), "testing task delete events");
		
		assertEquals(1, listener.getEventsReceived().size());
		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		assertTrue(event.getEntity() instanceof Task);
		Task taskFromEvent = (Task) event.getEntity();
		assertEquals(task.getId(), taskFromEvent.getId());
		assertExecutionDetails(event, processInstance);
		
		try {
			task = taskService.newTask();
			task.setCategory("123");
			task.setDescription("Description");
			taskService.saveTask(task);
			listener.clearEventsReceived();
			
			// Delete standalone task, only a delete-event should be dispatched
			taskService.deleteTask(task.getId());
			
			
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			
		} finally {
			if(task != null) {
				String taskId = task.getId();
				task = taskService.createTaskQuery().taskId(taskId).singleResult();
				if(task != null) {
					// If task still exists, delete it to have a clean DB after test
					taskService.deleteTask(taskId);
				}
				historyService.deleteHistoricTaskInstance(taskId);
			}
		}
	}
	
	
	/**
	 * Check all events for tasks not related to a process-instance 
	 */
	public void testStandaloneTaskEvents() throws Exception {
		
		Task task = null;
		try {
			task = taskService.newTask();
			task.setCategory("123");
			task.setDescription("Description");
			taskService.saveTask(task);
			
			assertEquals(2, listener.getEventsReceived().size());
			
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			Task taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
			listener.clearEventsReceived();
			
			// Update task
			taskService.setOwner(task.getId(), "owner");
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertEquals("owner", taskFromEvent.getOwner());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			listener.clearEventsReceived();
			
			// Assign task
			taskService.setAssignee(task.getId(), "kermit");
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.TASK_ASSIGNED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertEquals("kermit", taskFromEvent.getAssignee());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			listener.clearEventsReceived();
			
			// Complete task
			taskService.complete(task.getId());
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.TASK_COMPLETED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertTrue(event.getEntity() instanceof Task);
			taskFromEvent = (Task) event.getEntity(); 
			assertEquals(task.getId(), taskFromEvent.getId());
			assertNull(event.getProcessDefinitionId());
			assertNull(event.getProcessInstanceId());
			assertNull(event.getExecutionId());
			
		} finally {
			if(task != null) {
				String taskId = task.getId();
				task = taskService.createTaskQuery().taskId(taskId).singleResult();
				if(task != null) {
					// If task still exists, delete it to have a clean DB after test
					taskService.deleteTask(taskId);
				}
				historyService.deleteHistoricTaskInstance(taskId);
			}
		}
	}
	
	protected void assertExecutionDetails(ActivitiEvent event, ProcessInstance processInstance) {
		assertEquals(processInstance.getId(), event.getProcessInstanceId());
		assertEquals(processInstance.getId(), event.getExecutionId());
		assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
	}
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  listener = new TestActivitiEntityEventListener(Task.class);
	  processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if(listener != null) {
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  }
	}
}
