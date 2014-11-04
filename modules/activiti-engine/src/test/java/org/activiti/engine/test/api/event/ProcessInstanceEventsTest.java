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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process instances.
 * 
 * @author Frederik Heremans
 */
public class ProcessInstanceEventsTest extends PluggableActivitiTestCase {

	private TestInitializedEntityEventListener listener;
	
	/**
	 * Test create, update and delete events of process instances.
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testProcessInstanceEvents() throws Exception {
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

			assertNotNull(processInstance);

			// Check create-event
			assertEquals(2, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);
			
			ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
            assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
            assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
            assertEquals(processInstance.getId(), event.getProcessInstanceId());
            assertEquals(processInstance.getId(), event.getExecutionId());
            assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			listener.clearEventsReceived();

			// Check update event when suspended/activated
			runtimeService.suspendProcessInstanceById(processInstance.getId());
			runtimeService.activateProcessInstanceById(processInstance.getId());
			
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(ActivitiEventType.ENTITY_SUSPENDED, event.getType());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_ACTIVATED, event.getType());
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			listener.clearEventsReceived();
			
			// Check update event when process-definition is supended (should cascade suspend/activate all process instances)
			repositoryService.suspendProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);
			repositoryService.activateProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);
			
			assertEquals(2, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(ActivitiEventType.ENTITY_SUSPENDED, event.getType());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
			assertEquals(ActivitiEventType.ENTITY_ACTIVATED, event.getType());
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			listener.clearEventsReceived();
			
			// Check update-event when business-key is updated
			runtimeService.updateBusinessKey(processInstance.getId(), "thekey");
			assertEquals(1, listener.getEventsReceived().size());
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			listener.clearEventsReceived();
			
			runtimeService.deleteProcessInstance(processInstance.getId(), "Testing events");
			
			event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
			assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
			assertEquals(processInstance.getId(), event.getProcessInstanceId());
			assertEquals(processInstance.getId(), event.getExecutionId());
			assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
			listener.clearEventsReceived();
	}

  /**
   * Test create, update and delete events of process instances.
   */
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testSubProcessInstanceEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");

    assertNotNull(processInstance);

    // Check create-event one main process the second one Scope execution, and the third one subprocess
    assertEquals(6, listener.getEventsReceived().size());
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiEntityEvent);

    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());
    assertEquals(processInstance.getId(), event.getProcessInstanceId());
    assertEquals(processInstance.getId(), event.getExecutionId());
    assertEquals(processInstance.getProcessDefinitionId(), event.getProcessDefinitionId());
    
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getParentId());
    
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    assertEquals(processInstance.getId(), ((ProcessInstance) event.getEntity()).getParentId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    assertEquals("simpleSubProcess", ((ExecutionEntity) event.getEntity()).getProcessDefinition().getKey());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    assertEquals("simpleSubProcess", ((ExecutionEntity) event.getEntity()).getProcessDefinition().getKey());

    listener.clearEventsReceived();
  }

  /**
   * Test process with signals start.
   */
  @Deployment(resources = {"org/activiti/engine/test/bpmn/event/signal/SignalEventTest.testSignalWithGlobalScope.bpmn20.xml"})
  public void testSignalProcessInstanceStart() throws Exception {
    this.runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    listener.clearEventsReceived();

    runtimeService.startProcessInstanceByKey("processWithSignalThrow");
    listener.clearEventsReceived();
  }

  /**
   * Test Start->End process on PROCESS_COMPLETED event
   */
  @Deployment(resources = {"org/activiti/engine/test/api/event/ProcessInstanceEventsTest.noneTaskProcess.bpmn20.xml"})
  public void testProcessCompleted_StartEnd() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noneTaskProcess");

    listener.checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
  }

  /**
   * Test Start->User Task  process on PROCESS_COMPLETED event
   */
  @Deployment(resources = {"org/activiti/engine/test/api/event/ProcessInstanceEventsTest.noEndProcess.bpmn20.xml"})
  public void testProcessCompleted_NoEnd() throws Exception {
    ProcessInstance noEndProcess = this.runtimeService.startProcessInstanceByKey("noEndProcess");
    Task task = taskService.createTaskQuery().processInstanceId(noEndProcess.getId()).singleResult();
    taskService.complete(task.getId());

    listener.checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
  }

  /**
   * Test
   *        +-->Task1
   * Start-<>
   *        +-->Task1
   *
   * process on PROCESS_COMPLETED event
   */
  @Deployment(resources = {"org/activiti/engine/test/api/event/ProcessInstanceEventsTest.parallelGatewayNoEndProcess.bpmn20.xml"})
  public void testProcessCompleted_ParallelGatewayNoEnd() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noEndProcess");

    listener.checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
  }

  /**
   * Test
   *        +-->End1
   * Start-<>
   *        +-->End2
   * <p/>
   * process on PROCESS_COMPLETED event
   */
  @Deployment(resources = {"org/activiti/engine/test/api/event/ProcessInstanceEventsTest.parallelGatewayTwoEndsProcess.bpmn20.xml"})
  public void testProcessCompleted_ParallelGatewayTwoEnds() throws Exception {
    this.runtimeService.startProcessInstanceByKey("noEndProcess");

    listener.checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
  }

  @Override
	protected void initializeServices() {
	  super.initializeServices();
	  this.listener = new TestInitializedEntityEventListener();
	  processEngineConfiguration.getEventDispatcher().addEventListener(this.listener);
	}
	
	@Override
	protected void tearDown() throws Exception {
	  super.tearDown();
	  
	  if(listener != null) {
	  	listener.clearEventsReceived();
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  }
	}

  private class TestInitializedEntityEventListener implements ActivitiEventListener {

    private List<ActivitiEvent> eventsReceived;

    public TestInitializedEntityEventListener() {

      eventsReceived = new ArrayList<ActivitiEvent>();
    }

    public List<ActivitiEvent> getEventsReceived() {
      return eventsReceived;
    }

    public void clearEventsReceived() {
      eventsReceived.clear();
    }

    @Override
    public void onEvent(ActivitiEvent event) {
      if (event instanceof ActivitiEntityEvent && ProcessInstance.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
          // check whether entity in the event is initialized before adding to the list.
          assertNotNull(((ExecutionEntity) ((ActivitiEntityEvent) event).getEntity()).getId());
          eventsReceived.add(event);
      }
    }

    @Override
    public boolean isFailOnException() {
      return true;
    }

    public void checkEventCount(int expectedCount, ActivitiEventType eventType) {// count timer cancelled events
      int actualCount = 0;
      List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
      for (ActivitiEvent eventReceived : eventsReceived) {
        if (eventType.equals(eventReceived.getType())) {
          actualCount++;
        }
      }
      assertEquals(eventType.name() + " event was expected " + expectedCount + " times.", expectedCount, actualCount);
    }

  }
}
