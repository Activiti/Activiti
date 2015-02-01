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
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to deployments.
 * 
 * @author Frederik Heremans
 */
public class DeploymentEventsTest extends PluggableActivitiTestCase {

	private TestActivitiEntityEventListener listener;
	
	/**
	 * Test create, update and delete events of deployment entities.
	 */
	public void testDeploymentEvents() throws Exception {
		Deployment deployment = null;
		try {
			listener.clearEventsReceived();
			deployment = repositoryService.createDeployment()
					.addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
					.deploy();
			assertNotNull(deployment);
			
			// Check create-event
			assertEquals(2, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertEquals(deployment.getId(), ((Deployment) event.getEntity()).getId());
			
			assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiEntityEvent);
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
			assertEquals(deployment.getId(), ((Deployment) event.getEntity()).getId());
			
			listener.clearEventsReceived();
			
			// Check update event when category is updated
			repositoryService.setDeploymentCategory(deployment.getId(), "test");
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertEquals(deployment.getId(), ((Deployment) event.getEntity()).getId());
			assertEquals("test", ((Deployment) event.getEntity()).getCategory());
			listener.clearEventsReceived();
			
		  // Check delete event when category is updated
			repositoryService.deleteDeployment(deployment.getId(), true);
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertEquals(deployment.getId(), ((Deployment) event.getEntity()).getId());
			listener.clearEventsReceived();
			
		} finally {
			if(deployment != null && repositoryService.createDeploymentQuery().deploymentId(deployment.getId()).count() > 0) {
				repositoryService.deleteDeployment(deployment.getId(), true);
			}
		}
	}
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  listener = new TestActivitiEntityEventListener(Deployment.class);
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
