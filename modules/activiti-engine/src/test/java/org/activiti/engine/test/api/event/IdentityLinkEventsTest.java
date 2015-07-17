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

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process definitions.
 * 
 * @author Frederik Heremans
 */
public class IdentityLinkEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;

	/**
	 * Check identity links on process definitions. 
	 */
	@Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
	public void testProcessDefinitionIdentityLinkEvents() throws Exception {
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
		    .processDefinitionKey("oneTaskProcess").singleResult();

		assertNotNull(processDefinition);

		// Add candidate user and group
		repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
		repositoryService.addCandidateStarterGroup(processDefinition.getId(), "sales");
		assertEquals(4, listener.getEventsReceived().size());

		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		assertEquals(processDefinition.getId(), event.getProcessDefinitionId());
		assertNull(event.getProcessInstanceId());
		assertNull(event.getExecutionId());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		assertEquals(processDefinition.getId(), event.getProcessDefinitionId());
		assertNull(event.getProcessInstanceId());
		assertNull(event.getExecutionId());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		listener.clearEventsReceived();

		// Delete identity links
		repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "kermit");
		repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), "sales");
		assertEquals(2, listener.getEventsReceived().size());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		assertEquals(processDefinition.getId(), event.getProcessDefinitionId());
		assertNull(event.getProcessInstanceId());
		assertNull(event.getExecutionId());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		assertEquals(processDefinition.getId(), event.getProcessDefinitionId());
		assertNull(event.getProcessInstanceId());
		assertNull(event.getExecutionId());
	}
	
	/**
	 * Check identity links on process instances. 
	 */
	@Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
	public void testProcessInstanceIdentityLinkEvents() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

		// Add identity link
		runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "test");
		assertEquals(2, listener.getEventsReceived().size());

		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		assertEquals(processInstance.getId(), event.getProcessInstanceId());
		assertEquals(processInstance.getId(), event.getExecutionId());
		assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
		IdentityLink link = (IdentityLink) event.getEntity();
		assertEquals("kermit", link.getUserId());
		assertEquals("test", link.getType());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		
		listener.clearEventsReceived();

		// Deleting process should delete identity link
		runtimeService.deleteProcessInstance(processInstance.getId(), "test");
		assertEquals(1, listener.getEventsReceived().size());

		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		link = (IdentityLink) event.getEntity();
		assertEquals("kermit", link.getUserId());
		assertEquals("test", link.getType());
	}
	
	/**
	 * Check identity links on process instances. 
	 */
	@Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
	public void testTaskIdentityLinks() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNotNull(task);
		
		// Add identity link
		taskService.addCandidateUser(task.getId(), "kermit");
		taskService.addCandidateGroup(task.getId(), "sales");
		
		// Three events are received, since the user link on the task also creates an involvment in the process
		assertEquals(6, listener.getEventsReceived().size());

		ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		IdentityLink link = (IdentityLink) event.getEntity();
		assertEquals("kermit", link.getUserId());
		assertEquals("candidate", link.getType());
		assertEquals(task.getId(), link.getTaskId());
		assertEquals(task.getExecutionId(), event.getExecutionId());
		assertEquals(task.getProcessDefinitionId(), event.getProcessDefinitionId());
		assertEquals(task.getProcessInstanceId(), event.getProcessInstanceId());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		assertEquals("kermit", link.getUserId());
		assertEquals("candidate", link.getType());
		
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(4);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		assertTrue(event.getEntity() instanceof IdentityLink);
		link = (IdentityLink) event.getEntity();
		assertEquals("sales", link.getGroupId());
		assertEquals("candidate", link.getType());
		assertEquals(task.getId(), link.getTaskId());
		assertEquals(task.getExecutionId(), event.getExecutionId());
		assertEquals(task.getProcessDefinitionId(), event.getProcessDefinitionId());
		assertEquals(task.getProcessInstanceId(), event.getProcessInstanceId());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(5);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		assertEquals("sales", link.getGroupId());
		assertEquals("candidate", link.getType());
		
		listener.clearEventsReceived();

		// Deleting process should delete identity link
		runtimeService.deleteProcessInstance(processInstance.getId(), "test");
		assertEquals(3, listener.getEventsReceived().size());

		event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
	}
	
	/**
	 * Check deletion of links on process instances.
	 */
	@Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
	public void testProcessInstanceIdentityDeleteCandidateGroupEvents() throws Exception {
	
	  ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
	
	  Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
	  assertNotNull(task);

	  // Add identity link
	  taskService.addCandidateUser(task.getId(), "kermit");
	  taskService.addCandidateGroup(task.getId(), "sales");

	  // Three events are received, since the user link on the task also creates an involvement in the process. See previous test
	  assertEquals(6, listener.getEventsReceived().size());

	  listener.clearEventsReceived();
	  taskService.deleteCandidateUser(task.getId(), "kermit");
	  assertEquals(1, listener.getEventsReceived().size());

	  listener.clearEventsReceived();
	  taskService.deleteCandidateGroup(task.getId(), "sales");
	  assertEquals(1, listener.getEventsReceived().size());
	}
	

	@Override
	protected void initializeServices() {
		super.initializeServices();

		listener = new TestActivitiEntityEventListener(IdentityLink.class);
		processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (listener != null) {
			listener.clearEventsReceived();
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
}
