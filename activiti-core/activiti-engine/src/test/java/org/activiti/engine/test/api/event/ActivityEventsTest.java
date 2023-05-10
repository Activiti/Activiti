/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiMessageEvent;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.delegate.event.impl.ActivitiActivityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiSignalEventImpl;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.EventLogger;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to activities.
 *
 */
public class ActivityEventsTest extends PluggableActivitiTestCase {

  private TestActivitiActivityEventListener listener;

  protected EventLogger databaseEventLogger;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Database event logger setup
    databaseEventLogger = new EventLogger(processEngineConfiguration.getClock(), processEngineConfiguration.getObjectMapper());
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

    listener = new TestActivitiActivityEventListener(true);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  /**
   * Test starting and completed events for activity. Since these events are dispatched in the core of the PVM, not all individual activity-type is tested. Rather, we test the main types (tasks,
   * gateways, events, subprocesses).
   */
  @Deployment
  public void testActivityEvents() throws Exception {
    // We're interested in the raw events, alter the listener to keep those as well
    listener.setIgnoreRawActivityEvents(false);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("activityProcess");
    assertThat(processInstance).isNotNull();

    assertThat(listener.getEventsReceived()).hasSize(3);

    // Start-event activity started
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("theStart");
    assertThat(!processInstance.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // Start-event finished
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("theStart");
    assertThat(!processInstance.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // Usertask started
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(!processInstance.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // Complete usertask
    listener.clearEventsReceived();
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    // Subprocess execution is created
    Execution execution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertThat(execution).isNotNull();
    assertThat(listener.getEventsReceived()).hasSize(5);
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(!processInstance.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subProcess");
    assertThat(activityEvent.getExecutionId()).isEqualTo(execution.getId());
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subProcessStart");
    assertThat(!execution.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(3);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subProcessStart");
    assertThat(!execution.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(4);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subTask");
    assertThat(!execution.getId().equals(activityEvent.getExecutionId())).isTrue();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    listener.clearEventsReceived();

    // Check gateway and intermediate throw event
    Task subTask = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
    assertThat(subTask).isNotNull();

    taskService.complete(subTask.getId());

    assertThat(listener.getEventsReceived()).hasSize(10);

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subTask");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("gateway");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("gateway");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(3);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("throwMessageEvent");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(4);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("throwMessageEvent");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(5);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("endSubProcess");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(6);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("endSubProcess");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(7);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("subProcess");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(8);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED);
    assertThat(activityEvent.getActivityId()).isEqualTo("theEnd");

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(9);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED);
    assertThat(activityEvent.getActivityId()).isEqualTo("theEnd");
  }

  /**
   * Test events related to signalling
   */
  @Deployment
  public void testActivitySignalEvents() throws Exception {
    // Two paths are active in the process, one receive-task and one intermediate catching signal-event
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalProcess");
    assertThat(processInstance).isNotNull();

    // Check regular signal through API
    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("receivePayment").singleResult();
    assertThat(executionWithSignal).isNotNull();

    runtimeService.trigger(executionWithSignal.getId());
    assertThat(listener.getEventsReceived()).hasSize(1);
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiSignalEvent.class);
    ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertThat(signalEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_SIGNALED);
    assertThat(signalEvent.getActivityId()).isEqualTo("receivePayment");
    assertThat(signalEvent.getExecutionId()).isEqualTo(executionWithSignal.getId());
    assertThat(signalEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(signalEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(signalEvent.getSignalName()).isNull();
    assertThat(signalEvent.getSignalData()).isNull();
    listener.clearEventsReceived();

    // Check signal using event, and pass in additional payload
    Execution executionWithSignalEvent = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();
    runtimeService.signalEventReceived("alert", executionWithSignalEvent.getId(), singletonMap("test", (Object) "test"));
    assertThat(listener.getEventsReceived()).hasSize(1);
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiSignalEvent.class);
    signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertThat(signalEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_SIGNALED);
    assertThat(signalEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(signalEvent.getExecutionId()).isEqualTo(executionWithSignalEvent.getId());
    assertThat(signalEvent.getProcessInstanceId()).isEqualTo(executionWithSignalEvent.getProcessInstanceId());
    assertThat(signalEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(signalEvent.getSignalName()).isEqualTo("alert");
    assertThat(signalEvent.getSignalData()).isNotNull();
    listener.clearEventsReceived();

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_SIGNALED);
  }

  /**
   * Test to verify if signals coming from an intermediate throw-event trigger the right events to be dispatched.
   */
  @Deployment
  public void testActivitySignalEventsWithinProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalProcess");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    Execution executionWithSignalEvent = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();

    taskService.complete(task.getId());
    assertThat(listener.getEventsReceived()).hasSize(1);

    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiSignalEvent.class);
    ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertThat(signalEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_SIGNALED);
    assertThat(signalEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(signalEvent.getExecutionId()).isEqualTo(executionWithSignalEvent.getId());
    assertThat(signalEvent.getProcessInstanceId()).isEqualTo(executionWithSignalEvent.getProcessInstanceId());
    assertThat(signalEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(signalEvent.getSignalName()).isEqualTo("alert");
    assertThat(signalEvent.getSignalData()).isNull();
  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageProcess");
    assertThat(processInstance).isNotNull();

    Execution executionWithMessage = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();
    assertThat(executionWithMessage).isNotNull();

    runtimeService.messageEventReceived("messageName", executionWithMessage.getId());
    assertThat(listener.getEventsReceived()).hasSize(2);

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiMessageEvent.class);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
    assertThat(messageEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("messageName");
    assertThat(messageEvent.getMessageData()).isNull();

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiMessageEvent.class);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    assertThat(messageEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("messageName");
    assertThat(messageEvent.getMessageData()).isNull();

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  /**
   * Test events related to message events, called from the API, targeting an event-subprocess.
   */
  @Deployment
  public void testActivityMessageEventsInEventSubprocess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageProcess");
    assertThat(processInstance).isNotNull();

    Execution executionWithMessage = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertThat(executionWithMessage).isNotNull();

    runtimeService.messageEventReceived("messageName", executionWithMessage.getId());

    // Only a message-event should be present, no signal-event, since the event-subprocess is
    // not signaled, but executed instead
    assertThat(listener.getEventsReceived()).hasSize(3);

    // An ACTIVITY_MESSAGE_WAITING event is expected
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiMessageEvent.class);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
    assertThat(messageEvent.getActivityId()).isEqualTo("catchMessage");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("messageName");
    assertThat(messageEvent.getMessageData()).isNull();

    // An ACTIVITY_MESSAGE_RECEIVED event is expected
    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiMessageEvent.class);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    assertThat(messageEvent.getActivityId()).isEqualTo("catchMessage");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("messageName");
    assertThat(messageEvent.getMessageData()).isNull();

    // An ACTIVITY_CANCELLED event is expected
    assertThat(listener.getEventsReceived().get(2)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(activityEvent.getActivityId()).isEqualTo("shipOrder");
    assertThat(activityEvent.getActivityType()).isEqualTo("userTask");
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
  }

  /**
   * Test events related to compensation events.
   */
  @Deployment
  public void testActivityCompensationEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");
    assertThat(processInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Complete task, next a compensation event will be thrown
    taskService.complete(task.getId());

    assertThat(listener.getEventsReceived()).hasSize(1);

    // A compensate-event is expected
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiActivityEvent.class);
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertThat(activityEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPENSATE);
    assertThat(activityEvent.getActivityId()).isEqualTo("compensate");
    // A new execution is created for the compensation-event, this should be
    // visible in the event
    assertThat(processInstance.getId().equals(activityEvent.getExecutionId())).isFalse();
    assertThat(activityEvent.getProcessInstanceId()).isEqualTo(processInstance.getProcessInstanceId());
    assertThat(activityEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    // Check if the process is still alive
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertThat(processInstance).isNotNull();

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_COMPENSATE);
  }

  /**
   * Test events related to error-events
   */
  @Deployment
  public void testActivityErrorEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
    assertThat(processInstance).isNotNull();

    // Error-handling should have ended the process
    ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterErrorInstance).isNull();

    List<ActivitiErrorEvent> errorEvents = listener.getEventsReceived().stream()
        .filter(ActivitiErrorEvent.class::isInstance)
        .map(ActivitiErrorEvent.class::cast)
        .collect(toList());

    assertThat(errorEvents).as("Only one ActivityErrorEvent expected").hasSize(1);

    ActivitiErrorEvent errorEvent = errorEvents.get(0);

    assertThat(errorEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_ERROR_RECEIVED);
    assertThat(errorEvent.getActivityId()).isEqualTo("catchError");
    assertThat(errorEvent.getErrorId()).isEqualTo("myError");
    assertThat(errorEvent.getErrorCode()).isEqualTo("123");
    assertThat(errorEvent.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(errorEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(errorEvent.getExecutionId()).isNotEqualTo(processInstance.getId());
  }

  /**
   * Test events related to error-events, thrown from within process-execution (eg. service-task).
   */
  @Deployment
  public void testActivityErrorEventsFromBPMNError() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
    assertThat(processInstance).isNotNull();

    // Error-handling should have ended the process
    ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(afterErrorInstance).isNull();

    List<ActivitiErrorEvent> errorEvents = listener.getEventsReceived().stream()
      .filter(ActivitiErrorEvent.class::isInstance)
      .map(ActivitiErrorEvent.class::cast)
      .collect(toList());

    assertThat(errorEvents).as("Only one ActivityErrorEvent expected").hasSize(1);

    ActivitiErrorEvent errorEvent = errorEvents.get(0);

    assertThat(errorEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_ERROR_RECEIVED);
    assertThat(errorEvent.getActivityId()).isEqualTo("catchError");
    assertThat(errorEvent.getErrorId()).isEqualTo("23");
    assertThat(errorEvent.getErrorCode()).isEqualTo("23");
    assertThat(errorEvent.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(errorEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(errorEvent.getExecutionId()).isNotEqualTo(processInstance.getId());
  }

  @Deployment(resources = "org/activiti/engine/test/api/event/JobEventsTest.testJobEntityEvents.bpmn20.xml")
  public void testActivityTimeOutEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(theJob).isNotNull();

    // Force timer to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    waitForJobExecutorToProcessAllJobs(2000);

    // Check timeout has been dispatched
    assertThat(listener.getEventsReceived()).hasSize(1);
    ActivitiEvent activitiEvent = listener.getEventsReceived().get(0);
    assertThat(activitiEvent.getType()).as("ACTIVITY_CANCELLED event expected").isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(((ActivitiActivityCancelledEvent) activitiEvent).getCause()).isEqualTo("boundary event (timer)");
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/timer/BoundaryTimerEventTest.testTimerOnNestingOfSubprocesses.bpmn20.xml")
  public void testActivityTimeOutEventInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnNestedSubprocesses");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(theJob).isNotNull();

    // Force timer to fire
    Calendar timeToFire = Calendar.getInstance();
    timeToFire.add(Calendar.HOUR, 2);
    timeToFire.add(Calendar.SECOND, 5);
    processEngineConfiguration.getClock().setCurrentTime(timeToFire.getTime());
    waitForJobExecutorToProcessAllJobs(2000);

    // Check timeout-events have been dispatched
    assertThat(listener.getEventsReceived()).hasSize(4);
    List<String> eventIdList = new ArrayList<String>();
    for (ActivitiEvent event : listener.getEventsReceived()) {
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
      assertThat(((ActivitiActivityCancelledEvent) event).getCause()).isEqualTo("boundary event (boundaryTimer)");
      eventIdList.add(((ActivitiActivityEventImpl) event).getActivityId());
    }
    assertThat(eventIdList.indexOf("innerTask1") >= 0).isTrue();
    assertThat(eventIdList.indexOf("innerTask2") >= 0).isTrue();
    assertThat(eventIdList.indexOf("subprocess") >= 0).isTrue();
    assertThat(eventIdList.indexOf("innerSubprocess") >= 0).isTrue();
  }

  @Deployment
  public void testActivityTimeOutEventInCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnCallActivity");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(theJob).isNotNull();

    // Force timer to fire
    Calendar timeToFire = Calendar.getInstance();
    timeToFire.add(Calendar.HOUR, 2);
    timeToFire.add(Calendar.MINUTE, 5);
    processEngineConfiguration.getClock().setCurrentTime(timeToFire.getTime());
    waitForJobExecutorToProcessAllJobs(5000);

    // Check timeout-events have been dispatched
    assertThat(listener.getEventsReceived())
            .extracting(ActivitiEvent::getType)
            .containsOnly(ActivitiEventType.ACTIVITY_CANCELLED)
            .hasSize(4);
    assertThat(listener.getEventsReceived())
            .extracting(event -> ((ActivitiActivityCancelledEvent) event).getActivityId(),
                        event -> ((ActivitiActivityCancelledEvent) event).getCause())
            .containsExactlyInAnyOrder(tuple("innerTask1",
                                   "boundary event (boundaryTimer)"),
                             tuple("innerTask2",
                                   "boundary event (boundaryTimer)"),
                             tuple("callActivity",
                                   "boundary event (boundaryTimer)"),
                             tuple("innerSubprocess",
                                   "boundary event (boundaryTimer)"));

  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageBoundaryEventsOnUserTask() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageOnUserTaskProcess");
    assertThat(processInstance).isNotNull();

    Execution executionWithMessage = runtimeService.createExecutionQuery().messageEventSubscriptionName("message_1").singleResult();
    assertThat(executionWithMessage).isNotNull();
    Execution taskExecution = runtimeService.createExecutionQuery().activityId("cloudformtask1").processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskExecution).isNotNull();

    runtimeService.messageEventReceived("message_1", executionWithMessage.getId());
    assertThat(listener.getEventsReceived()).hasSize(3);

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiMessageEvent.class);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
    assertThat(messageEvent.getActivityId()).isEqualTo("boundaryMessageEventCatching");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("message_1");
    assertThat(messageEvent.getMessageData()).isNull();

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiMessageEvent.class);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    assertThat(messageEvent.getActivityId()).isEqualTo("boundaryMessageEventCatching");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("message_1");
    assertThat(messageEvent.getMessageData()).isNull();

    // Next, an signal-event is expected, as a result of the message
    assertThat(listener.getEventsReceived().get(2)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    ActivitiActivityCancelledEvent cancelledEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertThat(cancelledEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelledEvent.getActivityId()).isEqualTo("cloudformtask1");
    assertThat(cancelledEvent.getExecutionId()).isEqualTo(taskExecution.getId());
    assertThat(cancelledEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(cancelledEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelledEvent.getCause()).isEqualTo("boundary event (boundaryMessageEventCatching)");

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageBoundaryEventsOnSubProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageOnSubProcess");
    assertThat(processInstance).isNotNull();

    Execution executionWithMessage = runtimeService.createExecutionQuery().activityId("boundaryMessageEventCatching").singleResult();
    assertThat(executionWithMessage).isNotNull();

    runtimeService.messageEventReceived("message_1", executionWithMessage.getId());
    assertThat(listener.getEventsReceived()).hasSize(4);

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiMessageEvent.class);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_WAITING);
    assertThat(messageEvent.getActivityId()).isEqualTo("boundaryMessageEventCatching");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("message_1");
    assertThat(messageEvent.getMessageData()).isNull();

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiMessageEvent.class);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertThat(messageEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
    assertThat(messageEvent.getActivityId()).isEqualTo("boundaryMessageEventCatching");
    assertThat(messageEvent.getExecutionId()).isEqualTo(executionWithMessage.getId());
    assertThat(messageEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(messageEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(messageEvent.getMessageName()).isEqualTo("message_1");
    assertThat(messageEvent.getMessageData()).isNull();

    // Next, a signal-event is expected, as a result of the message
    assertThat(listener.getEventsReceived().get(2)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    ActivitiActivityCancelledEvent cancelledEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertThat(cancelledEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelledEvent.getActivityId()).isEqualTo("cloudformtask1");
    assertThat(cancelledEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(cancelledEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelledEvent.getCause()).isEqualTo("boundary event (boundaryMessageEventCatching)");

    assertThat(listener.getEventsReceived().get(3)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    cancelledEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(3);
    assertThat(cancelledEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelledEvent.getActivityId()).isEqualTo("subProcess");
    assertThat(cancelledEvent.getProcessInstanceId()).isEqualTo(executionWithMessage.getProcessInstanceId());
    assertThat(cancelledEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelledEvent.getCause()).isEqualTo("boundary event (boundaryMessageEventCatching)");

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  @Deployment
  public void testActivitySignalBoundaryEventsOnSubProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalOnSubProcess");
    assertThat(processInstance).isNotNull();

    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("userTaskInsideProcess").singleResult();
    assertThat(executionWithSignal).isNotNull();

    runtimeService.signalEventReceived("signalName");
    assertThat(listener.getEventsReceived()).hasSize(3);

    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiSignalEventImpl.class);
    ActivitiSignalEventImpl signalEvent = (ActivitiSignalEventImpl) listener.getEventsReceived().get(0);
    assertThat(signalEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_SIGNALED);
    assertThat(signalEvent.getActivityId()).isEqualTo("boundarySignalEventCatching");
    assertThat(signalEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(signalEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    assertThat(listener.getEventsReceived().get(2)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    ActivitiActivityCancelledEvent cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertThat(cancelEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelEvent.getActivityId()).isEqualTo("subProcess");
    assertThat(cancelEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(cancelEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelEvent.getCause()).isEqualTo("boundary event (boundarySignalEventCatching)");

    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(1);
    assertThat(cancelEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelEvent.getActivityId()).isEqualTo("userTaskInsideProcess");
    assertThat(cancelEvent.getExecutionId()).isEqualTo(executionWithSignal.getId());
    assertThat(cancelEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(cancelEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelEvent.getCause()).isEqualTo("boundary event (boundarySignalEventCatching)");
  }

  @Deployment
  public void testActivitySignalBoundaryEventsOnUserTask() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalOnUserTask");
    assertThat(processInstance).isNotNull();

    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertThat(executionWithSignal).isNotNull();

    runtimeService.signalEventReceived("signalName");

    // Next, an signal-event is expected, as a result of the message
    assertThat(listener.getEventsReceived()).hasSize(2);

    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiSignalEventImpl.class);
    ActivitiSignalEventImpl signalEvent = (ActivitiSignalEventImpl) listener.getEventsReceived().get(0);
    assertThat(signalEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_SIGNALED);
    assertThat(signalEvent.getActivityId()).isEqualTo("boundarySignalEventCatching");
    assertThat(signalEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(signalEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());

    assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiActivityCancelledEvent.class);
    ActivitiActivityCancelledEvent cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(1);
    assertThat(cancelEvent.getType()).isEqualTo(ActivitiEventType.ACTIVITY_CANCELLED);
    assertThat(cancelEvent.getActivityId()).isEqualTo("userTask");
    assertThat(cancelEvent.getProcessInstanceId()).isEqualTo(executionWithSignal.getProcessInstanceId());
    assertThat(cancelEvent.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    assertThat(cancelEvent.getCause()).isEqualTo("boundary event (boundarySignalEventCatching)");
  }

  protected void assertDatabaseEventPresent(ActivitiEventType eventType) {
    String eventTypeString = eventType.name();
    List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(0L, 100000L);
    boolean found = false;
    for (EventLogEntry entry : eventLogEntries) {
      if (entry.getType().equals(eventTypeString)) {
        found = true;
      }
    }
    assertThat(found).isTrue();
  }

}
