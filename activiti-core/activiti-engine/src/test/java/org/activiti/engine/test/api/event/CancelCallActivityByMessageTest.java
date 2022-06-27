/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

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
    assertThat(processInstance).isNotNull();

    Execution executionWithMessageEvent = runtimeService.createExecutionQuery()
            .activityId("cancelBoundaryMessageEvent")
            .singleResult();
    assertThat(executionWithMessageEvent).isNotNull();
    Execution callActivityExecution = runtimeService.createExecutionQuery()
            .activityId("callActivity1")
            .singleResult();
    assertThat(callActivityExecution).isNotNull();

    runtimeService.messageEventReceived("cancel", executionWithMessageEvent.getId());

    ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(0);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    ExecutionEntity executionEntity = (ExecutionEntity) entityEvent.getEntity();
    // this is process so parent null
    assertThat(executionEntity.getParentId()).isNull();
    String processExecutionId = executionEntity.getId();

    // this is callActivity
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(1);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertThat(executionEntity.getParentId()).isNotNull();
    assertThat(executionEntity.getParentId()).isEqualTo(processExecutionId);

    ActivitiEvent activitiEvent = (ActivitiEvent) myEventListener.getEventsReceived().get(2);
    assertThat(activitiEvent.getType()).isEqualTo(ActivitiEventType.PROCESS_STARTED);

    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(3);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityType()).isEqualTo("startEvent");

    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(4);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityType()).isEqualTo("startEvent");


    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(5);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertThat(executionEntity.getActivityId()).isEqualTo("cancelBoundaryMessageEvent");
    String boundaryExecutionId = executionEntity.getId();

    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(6);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("callActivity1");

    // this is external subprocess. Workflow uses the ENTITY_CREATED event to determine when to send our event.
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(7);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertThat(executionEntity.getParentId()).isNull();
    assertThat(executionEntity.getProcessInstanceId()).isEqualTo(executionEntity.getId());

    // this is the task within the external subprocess
    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(8);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    executionEntity = (ExecutionEntity) entityEvent.getEntity();
    assertThat(executionEntity.getActivityId()).isEqualTo("calledSubprocessTask");

    // start event in external subprocess
    activitiEvent = (ActivitiEvent) myEventListener.getEventsReceived().get(9);
    assertThat(activitiEvent.getType()).isEqualTo(ActivitiEventType.PROCESS_STARTED);

    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(10);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityType()).isEqualTo("startEvent");
    assertThat(activityEvent.getActivityId()).isEqualTo("startevent2");

    // start event in external subprocess
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(11);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityType()).isEqualTo("startEvent");
    assertThat(activityEvent.getActivityId()).isEqualTo("startevent2");

    // this is user task within external subprocess
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(12);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("calledSubprocessTask");
    assertThat(activityEvent.getActivityType()).isEqualTo("userTask");

    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(13);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.TASK_CREATED);
    TaskEntity taskEntity = (TaskEntity) entityEvent.getEntity();
    assertThat(taskEntity.getName()).isEqualTo("Sample User Task2 in External");
    String externalTaskExecutionId = taskEntity.getExecutionId();

    // activityId is the call activity and the execution is the boundary event as we have seen before
    // We get this event in workflow but we ignore the activityType of "callActivity"
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(14);
    assertThat(activityEvent)
            .extracting(ActivitiActivityEvent::getType,
                    ActivitiActivityEvent::getActivityType,
                        ActivitiEvent::getExecutionId,
                        ActivitiActivityEvent::getActivityName)
            .containsExactly(ActivitiEventType.ACTIVITY_CANCELLED,
                                   "userTask",
                                   externalTaskExecutionId,
                                   "Sample User Task2 in External");

    assertThat(((ActivitiCancelledEvent) activityEvent).getCause()).isEqualTo("boundary event (cancelBoundaryMessageEvent)");

    ActivitiEntityEvent taskCancelledEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(15);
    assertThat(taskCancelledEvent.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(((TaskEntity) taskCancelledEvent.getEntity()).getName()).isEqualTo(taskEntity.getName());

    ActivitiCancelledEvent processCancelledEvent = (ActivitiCancelledEvent) myEventListener.getEventsReceived().get(16);
    assertThat(processCancelledEvent.getType()).isEqualTo(ActivitiEventType.PROCESS_CANCELLED);
    assertThat(processCancelledEvent.getExecutionId()).isEqualTo(processCancelledEvent.getProcessInstanceId());

    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(17);
    assertThat(activityEvent)
            .extracting(ActivitiActivityEvent::getType,
                        ActivitiActivityEvent::getActivityType,
                        ActivitiEvent::getExecutionId,
                        ActivitiActivityEvent::getActivityName)
            .containsExactly(ActivitiEventType.ACTIVITY_CANCELLED,
                                   "callActivity",
                                   callActivityExecution.getId(),
                                   "Call activity");
    assertThat(((ActivitiCancelledEvent) activityEvent).getCause()).isEqualTo("boundary event (cancelBoundaryMessageEvent)");


    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(18);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityType()).isEqualTo("boundaryEvent");
    assertThat(activityEvent.getActivityId()).isEqualTo("cancelBoundaryMessageEvent");

    // task in the main definition
    activityEvent = (ActivitiActivityEvent) myEventListener.getEventsReceived().get(19);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("usertask1");
    assertThat(activityEvent.getActivityType()).isEqualTo("userTask");

    entityEvent = (ActivitiEntityEvent) myEventListener.getEventsReceived().get(20);
    assertThat(entityEvent.getType()).isEqualTo(ActivitiEventType.TASK_CREATED);
     taskEntity = (TaskEntity) entityEvent.getEntity();
    assertThat(taskEntity.getName()).isEqualTo("Sample User Task1");

    assertThat(myEventListener.getEventsReceived()).hasSize(21);
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
            case ENTITY_DELETED:
                if(event instanceof ActivitiEntityEvent && ((ActivitiEntityEvent) event).getEntity() instanceof TaskEntity) {
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
