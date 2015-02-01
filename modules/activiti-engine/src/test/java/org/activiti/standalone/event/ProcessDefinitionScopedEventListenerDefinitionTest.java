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
package org.activiti.standalone.event;

import java.util.List;

import org.activiti.engine.ActivitiClassLoadingException;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.event.StaticTestActivitiEventListener;
import org.activiti.engine.test.api.event.TestActivitiEventListener;

/**
 * Test for event-listeners that are registered on a process-definition scope,
 * rather than on the global engine-wide scope, declared in the BPMN XML.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionScopedEventListenerDefinitionTest extends ResourceActivitiTestCase {

  public ProcessDefinitionScopedEventListenerDefinitionTest() {
    super("org/activiti/standalone/event/activiti-eventlistener.cfg.xml");
  }

	protected TestActivitiEventListener testListenerBean;

	/**
	 * Test to verify listeners defined in the BPMN xml are added to the process
	 * definition and are active.
	 */
	@Deployment
	public void testProcessDefinitionListenerDefinition() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testEventListeners");
		assertNotNull(testListenerBean);
		
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		taskService.complete(task.getId());
		
		
		// Check if the listener (defined as bean) received events (only creation, not other events)
		assertFalse(testListenerBean.getEventsReceived().isEmpty());
		for(ActivitiEvent event : testListenerBean.getEventsReceived()) {
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		}
		
	// First event received should be creation of Process-definition
		assertTrue(testListenerBean.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
		ActivitiEntityEvent event = (ActivitiEntityEvent) testListenerBean.getEventsReceived().get(0);
		assertTrue(event.getEntity() instanceof ProcessDefinition);
		assertEquals(processInstance.getProcessDefinitionId(), ((ProcessDefinition) event.getEntity()).getId());
			
		// First event received should be creation of Process-instance
		assertTrue(testListenerBean.getEventsReceived().get(1) instanceof ActivitiEntityEvent);
		event = (ActivitiEntityEvent) testListenerBean.getEventsReceived().get(1);
		assertTrue(event.getEntity() instanceof ProcessInstance);
		assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
		
		// Check if listener, defined by classname, received all events
		List<ActivitiEvent> events = StaticTestActivitiEventListener.getEventsReceived();
		assertFalse(events.isEmpty());
		
		boolean insertFound = false;
		boolean deleteFound = false;
		
		for(ActivitiEvent e : events) {
			if(ActivitiEventType.ENTITY_CREATED == e.getType() ) {
				insertFound = true;
			} else if(ActivitiEventType.ENTITY_DELETED == e.getType()) {
				deleteFound = true;
			}
		}
		assertTrue(insertFound);
		assertTrue(deleteFound);
	}
	
	/**
	 * Test to verify listeners defined in the BPMN xml with invalid class/delegateExpression
	 * values cause an exception when process is started.
	 */
	public void testProcessDefinitionListenerDefinitionError() throws Exception {
		
		// Deploy process with expression which references an unexisting bean
		try {
			repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/event/invalidEventListenerExpression.bpmn20.xml")
				.deploy();
			fail("Exception expected");
		} catch(ActivitiException ae) {
			assertEquals("Exception while executing event-listener", ae.getMessage());
			assertTrue(ae.getCause() instanceof ActivitiException);
			assertEquals("Unknown property used in expression: ${unexistingBean}", ae.getCause().getMessage());
		}
		
	    // Deploy process with listener which references an unexisting class
			try {
				repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/event/invalidEventListenerClass.bpmn20.xml")
					.deploy();
				fail("Exception expected");
			} catch(ActivitiException ae) {
				assertEquals("Exception while executing event-listener", ae.getMessage());
				assertTrue(ae.getCause() instanceof ActivitiException);
				assertEquals("couldn't instantiate class org.activiti.engine.test.api.event.UnexistingClass", ae.getCause().getMessage());
				assertTrue(ae.getCause().getCause() instanceof ActivitiClassLoadingException);
				assertTrue(ae.getCause().getCause().getCause() instanceof ClassNotFoundException);
			}
	}
	
	/**
	 * Test to verify if event listeners defined in the BPMN XML which have illegal event-types
	 * cause an exception on deploy. 
	 */
	public void testProcessDefinitionListenerDefinitionIllegalType() throws Exception {
		// In case deployment doesn't fail, we delete the deployment in the finally block to
		// ensure clean DB for subsequent tests
		org.activiti.engine.repository.Deployment deployment = null;
		try {
			
			deployment = repositoryService.createDeployment()
				.addClasspathResource("org/activiti/standalone/event/invalidEventListenerType.bpmn20.xml")
				.deploy();
			
			fail("Exception expected");
			
		} catch(ActivitiException ae) {
			assertTrue(ae instanceof ActivitiIllegalArgumentException);
			assertEquals("Invalid event-type: invalid", ae.getMessage());
		} finally {
			if(deployment != null) {
				repositoryService.deleteDeployment(deployment.getId(), true);
			}
		}
	}
	
	/**
	 * Test to verify listeners defined in the BPMN xml are added to the process
	 * definition and are active, for all entity types
	 */
	@Deployment
	public void testProcessDefinitionListenerDefinitionEntities() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testEventListeners");
		assertNotNull(processInstance);
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(task);
		
		// Attachment entity
		TestActivitiEventListener theListener = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("testAttachmentEventListener");
		assertNotNull(theListener);
		assertEquals(0, theListener.getEventsReceived().size());
		
		taskService.createAttachment("test", task.getId(), processInstance.getId(), "test", "test", "url");
		assertEquals(2, theListener.getEventsReceived().size());
		assertEquals(ActivitiEventType.ENTITY_CREATED, theListener.getEventsReceived().get(0).getType());
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, theListener.getEventsReceived().get(1).getType());
		
	}
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  testListenerBean = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("testEventListener");
	}
}
