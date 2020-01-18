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

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.EventLogger;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

public class CancelCallActivityByMessageTest extends PluggableActivitiTestCase {

  private CallActivityByMessageEventListener listener;

  protected EventLogger databaseEventLogger;

  @Deployment(resources = {
      "org/activiti/engine/test/api/event/CancelCallActivityByMessageTest.testActivityMessageBoundaryEventsOnCallActivity.bpmn20.xml",
      "org/activiti/engine/test/api/event/CancelCallActivityByMessageTest.testActivityMessageBoundaryEventsExternalSubProcess.bpmn20.xml" })
  public void testCancelCallActivityByMessage() throws Exception {

	  CallActivityByMessageEventListener myEventListener = new CallActivityByMessageEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(myEventListener);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageEventOnCallActivity");
    assertNotNull(processInstance);

    Execution executionWithMessageEvent = runtimeService.createExecutionQuery().activityId("cancelBoundaryMessageEvent")
        .singleResult();
    assertNotNull(executionWithMessageEvent);

    runtimeService.messageEventReceived("cancel", executionWithMessageEvent.getId());

    ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, entityEvent.getType());
    ExecutionEntity executionEntity = (ExecutionEntity) entityEvent.getEntity();
    // this is process so parent null
    assertNull(executionEntity.getParentId());
    String processExecutionId = executionEntity.getId();

    // this is callActivity
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_CREATED, entityEvent.getType());
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertNotNull(executionEntity.getParentId());
    assertEquals(processExecutionId, executionEntity.getParentId());

    ActivitiEvent activitiEvent = (ActivitiEvent) myEventListener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.PROCESS_STARTED, activitiEvent.getType());
    
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("startEvent", activityEvent.getActivityType());
    
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("startEvent", activityEvent.getActivityType());
    
    
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.ENTITY_CREATED, entityEvent.getType());
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertEquals("cancelBoundaryMessageEvent", executionEntity.getActivityId());
    String boundaryExecutionId = executionEntity.getId();
        
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(6);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("callActivity1", activityEvent.getActivityId());
    
    // this is external subprocess. Workflow uses the ENTITY_CREATED event to determine when to send our event.
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(7);
    assertEquals(ActivitiEventType.ENTITY_CREATED, entityEvent.getType());
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertNull(executionEntity.getParentId());
    assertEquals(executionEntity.getId(), executionEntity.getProcessInstanceId());
    
    // this is the task within the external subprocess
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(8);
    assertEquals(ActivitiEventType.ENTITY_CREATED, entityEvent.getType());
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertEquals("calledSubprocessTask", executionEntity.getActivityId());
    
    // start event in external subprocess
    activitiEvent = (ActivitiEvent) myEventListener.getEventsReceived().get(9);
    assertEquals(ActivitiEventType.PROCESS_STARTED, activitiEvent.getType());   
    
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(10);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("startEvent", activityEvent.getActivityType());
    assertEquals("startevent2", activityEvent.getActivityId());
    
    // start event in external subprocess
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(11);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("startEvent", activityEvent.getActivityType());
    assertEquals("startevent2", activityEvent.getActivityId());
    
    // this is user task within external subprocess
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(12);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("calledSubprocessTask", activityEvent.getActivityId());
    assertEquals("userTask", activityEvent.getActivityType());
    
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(13);
    assertEquals(ActivitiEventType.TASK_CREATED, entityEvent.getType());
    TaskEntity taskEntity = (TaskEntity) entityEvent.getEntity();
    assertEquals("Sample User Task2 in External", taskEntity.getName());
    
    // activityId is the call activity and the execution is the boundary event as we have seen before
    // We get this event in workflow but we ignore the activityType of "callActivity"
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(14);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, activityEvent.getType());
    assertEquals("callActivity", activityEvent.getActivityType());
    assertEquals(boundaryExecutionId, activityEvent.getExecutionId());
        
    ActivitiActivityCancelledEvent taskCancelledEvent = (ActivitiActivityCancelledEvent) myEventListener.getEventsReceived().get(15);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, taskCancelledEvent.getType());
    assertEquals(taskEntity.getName(), taskCancelledEvent.getActivityName()); 
    
    ActivitiCancelledEvent processCancelledEvent = (ActivitiCancelledEvent) myEventListener.getEventsReceived().get(16);
    assertEquals(ActivitiEventType.PROCESS_CANCELLED, processCancelledEvent.getType());
    assertEquals(processCancelledEvent.getProcessInstanceId(), processCancelledEvent.getExecutionId());
    
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(17);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("boundaryEvent", activityEvent.getActivityType());
    assertEquals("cancelBoundaryMessageEvent", activityEvent.getActivityId());
    
    // task in the main definition
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(18);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("usertask1", activityEvent.getActivityId());
    assertEquals("userTask", activityEvent.getActivityType());
    
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(19);
    assertEquals(ActivitiEventType.TASK_CREATED, entityEvent.getType());
     taskEntity = (TaskEntity) entityEvent.getEntity();
    assertEquals("Sample User Task1", taskEntity.getName());
    
    assertEquals(20, myEventListener.getEventsReceived().size());
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Database event logger setup
    databaseEventLogger = new EventLogger(processEngineConfiguration.getClock(),
        processEngineConfiguration.getObjectMapper());
    runtimeService.addEventListener(databaseEventLogger);
  }

  @Override
  protected void tearDown() throws Exception {
    if (listener != null) {
      listener.clearEventsReceived();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }

    // Remove entries
    for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
      managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
    }

    // Database event logger teardown
    runtimeService.removeEventListener(databaseEventLogger);
    super.tearDown();
  }

  @Override
  protected void initializeServices() {
    super.initializeServices();
    listener = new CallActivityByMessageEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  class CallActivityByMessageEventListener implements ActivitiEventListener {

    private List<ActivitiEvent> eventsReceived;

    public CallActivityByMessageEventListener() {
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
    	switch (event.getType()) {
    	case ENTITY_CREATED:
    		ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
    		if (entityEvent.getEntity() instanceof ExecutionEntity) {
    			eventsReceived.add(event);
    		}
    		break;
    	case ACTIVITY_STARTED:
    	case ACTIVITY_COMPLETED:
    	case ACTIVITY_CANCELLED:
    	case TASK_CREATED:
    	case TASK_COMPLETED:
    	case PROCESS_STARTED:
    	case PROCESS_COMPLETED:
    	case PROCESS_CANCELLED:
    		eventsReceived.add(event);
    		break;
    	default:
    		break;
    	}
    }

    @Override
    public boolean isFailOnException() {
      return false;
    }
  }

}