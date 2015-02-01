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
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process definitions.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionEventsTest extends PluggableActivitiTestCase {

	private TestMultipleActivitiEventListener listener;
	
	/**
	 * Test create, update and delete events of process definitions.
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testProcessDefinitionEvents() throws Exception {
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
					.processDefinitionKey("oneTaskProcess")
					.singleResult();
			
			assertNotNull(processDefinition);
			
			// Check create-event
			assertEquals(2, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
			
			// Check update event when category is updated
			repositoryService.setProcessDefinitionCategory(processDefinition.getId(), "test");
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			assertEquals("test", ((ProcessDefinition) event.getEntity()).getCategory());
			listener.clearEventsReceived();
			
			// Check update event when suspended/activated
			repositoryService.suspendProcessDefinitionById(processDefinition.getId());
			repositoryService.activateProcessDefinitionById(processDefinition.getId());
			
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			assertEquals(ActivitiEventType.ENTITY_SUSPENDED, event.getType());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_ACTIVATED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
			
		  // Check delete event when category is updated
			repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
			deploymentIdFromDeploymentAnnotation = null;
			
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertEquals(processDefinition.getId(), ((ProcessDefinition) event.getEntity()).getId());
			listener.clearEventsReceived();
	}

  /**
   * test sequence of events for process definition with timer start event
   */
  @Deployment(resources= {"org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testDurationStartTimerEvent.bpmn20.xml"})
  public void testTimerStartEventDeployment() {
    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.
      createProcessDefinitionQuery().processDefinitionKey("startTimerEventExample").singleResult();
    ActivitiEntityEvent processDefinitionCreated = ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processDefinition);

    TimerEntity timer = (TimerEntity) managementService.createJobQuery().singleResult();
    ActivitiEntityEvent timerCreated = ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, timer);
    assertSequence(processDefinitionCreated, timerCreated);
    listener.clearEventsReceived();
  }

  private void assertSequence(ActivitiEntityEvent before, ActivitiEntityEvent after) {
    int beforeIndex = 0;
    int afterIndex = 0;
    for (int index = 0; index < listener.getEventsReceived().size(); index++) {
      ActivitiEvent activitiEvent = listener.getEventsReceived().get(index);

      if (isEqual(before, activitiEvent))
        beforeIndex = index;
      if (isEqual(after, activitiEvent))
        afterIndex = index;
    }
    assertTrue(beforeIndex < afterIndex);
  }

  /**
   * equals is not implemented.
   */
  private boolean isEqual(ActivitiEntityEvent event1, ActivitiEvent activitiEvent) {
    if (activitiEvent instanceof ActivitiEntityEvent && event1.getType().equals(activitiEvent.getType())) {
      ActivitiEntityEvent activitiEntityEvent = (ActivitiEntityEvent) activitiEvent;
      if (activitiEntityEvent.getEntity().getClass().equals(event1.getEntity().getClass())) {
        return true;
      }
    }
    return false;
  }


  @Override
	protected void initializeServices() {
	  super.initializeServices();

    listener = new TestMultipleActivitiEventListener();
    listener.setEventClasses(ActivitiEntityEvent.class);
    listener.setEntityClasses(ProcessDefinition.class, TimerEntity.class);

	  processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if(listener != null) {
	  	listener.clearEventsReceived();
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  }
	}
}
