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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivityEntityEvent;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process definitions.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;
	private TestActivitiEntityEventListener identityLinkListener;
	
	/**
	 * Test create, update and delete events of deployment entities.
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testProcessDefinitionEvents() throws Exception {
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey("oneTaskProcess")
					.singleResult();
			
			assertNotNull(processDefinition);
			
			// Check create-event
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivityEntityEvent);
			
			ActivityEntityEvent event = (ActivityEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
			
			// Check update event when category is updated
			repositoryService.setProcessDefinitionCategory(processDefinition.getId(), "test");
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivityEntityEvent);
			
			event = (ActivityEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			assertEquals("test", ((ProcessDefinition) event.getEntity()).getCategory());
			listener.clearEventsReceived();
			
			// Check update event when suspended/activated
			repositoryService.suspendProcessDefinitionById(processDefinition.getId());
			repositoryService.activateProcessDefinitionById(processDefinition.getId());
			
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivityEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			assertEquals(ActivitiEventType.ENTITY_SUSPENDED, event.getType());
			event = (ActivityEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_ACTIVATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
			
		  // Check delete event when category is updated
			repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
			deploymentId = null;
			
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivityEntityEvent);
			
			event = (ActivityEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
	}
	
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testProcessDefinitionIdentityLinkEvents() throws Exception {
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey("oneTaskProcess")
					.singleResult();
			
			assertNotNull(processDefinition);
			
			// Add candidate user and group
			repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
			repositoryService.addCandidateStarterGroup(processDefinition.getId(), "sales");
			assertEquals(2, identityLinkListener.getEventsReceived().size());
			
			ActivityEntityEvent event = (ActivityEntityEvent) identityLinkListener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertTrue(event.getEntity() instanceof IdentityLink);
			event = (ActivityEntityEvent) identityLinkListener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertTrue(event.getEntity() instanceof IdentityLink);
			identityLinkListener.clearEventsReceived();
			
			// Delete identity links
			repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "kermit");
			repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), "sales");
			assertEquals(2, identityLinkListener.getEventsReceived().size());
			event = (ActivityEntityEvent) identityLinkListener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertTrue(event.getEntity() instanceof IdentityLink);
			event = (ActivityEntityEvent) identityLinkListener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertTrue(event.getEntity() instanceof IdentityLink);
	}
	
	@Override
	protected void initializeServices() {
	  super.initializeServices();
	  
	  listener = new TestActivitiEntityEventListener(ProcessDefinition.class);
	  identityLinkListener = new TestActivitiEntityEventListener(IdentityLink.class);
	  processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	  processEngineConfiguration.getEventDispatcher().addEventListener(identityLinkListener);
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if(listener != null) {
	  	identityLinkListener.clearEventsReceived();
	  	listener.clearEventsReceived();
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(identityLinkListener);
	  }
	}
}
