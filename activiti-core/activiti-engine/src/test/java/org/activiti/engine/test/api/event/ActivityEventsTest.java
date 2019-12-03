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
import java.util.Calendar;
import java.util.Collections;
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
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
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
    assertNotNull(processInstance);

    assertEquals(3, listener.getEventsReceived().size());

    // Start-event activity started
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("theStart", activityEvent.getActivityId());
    assertTrue(!processInstance.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    // Start-event finished
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("theStart", activityEvent.getActivityId());
    assertTrue(!processInstance.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    // Usertask started
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("shipOrder", activityEvent.getActivityId());
    assertTrue(!processInstance.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    // Complete usertask
    listener.clearEventsReceived();
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());

    // Subprocess execution is created
    Execution execution = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
    assertNotNull(execution);
    assertEquals(5, listener.getEventsReceived().size());
    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("shipOrder", activityEvent.getActivityId());
    assertTrue(!processInstance.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("subProcess", activityEvent.getActivityId());
    assertEquals(execution.getId(), activityEvent.getExecutionId());
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("subProcessStart", activityEvent.getActivityId());
    assertTrue(!execution.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("subProcessStart", activityEvent.getActivityId());
    assertTrue(!execution.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("subTask", activityEvent.getActivityId());
    assertTrue(!execution.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());
    listener.clearEventsReceived();

    // Check gateway and intermediate throw event
    Task subTask = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult();
    assertNotNull(subTask);

    taskService.complete(subTask.getId());

    assertEquals(10, listener.getEventsReceived().size());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("subTask", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("gateway", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("gateway", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("throwMessageEvent", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("throwMessageEvent", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("endSubProcess", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(6);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("endSubProcess", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(7);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("subProcess", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(8);
    assertEquals(ActivitiEventType.ACTIVITY_STARTED, activityEvent.getType());
    assertEquals("theEnd", activityEvent.getActivityId());

    activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(9);
    assertEquals(ActivitiEventType.ACTIVITY_COMPLETED, activityEvent.getType());
    assertEquals("theEnd", activityEvent.getActivityId());
  }

  /**
   * Test events related to signalling
   */
  @Deployment
  public void testActivitySignalEvents() throws Exception {
    // Two paths are active in the process, one receive-task and one intermediate catching signal-event
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalProcess");
    assertNotNull(processInstance);

    // Check regular signal through API
    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("receivePayment").singleResult();
    assertNotNull(executionWithSignal);

    runtimeService.trigger(executionWithSignal.getId());
    assertEquals(1, listener.getEventsReceived().size());
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEvent);
    ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_SIGNALED, signalEvent.getType());
    assertEquals("receivePayment", signalEvent.getActivityId());
    assertEquals(executionWithSignal.getId(), signalEvent.getExecutionId());
    assertEquals(executionWithSignal.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertNull(signalEvent.getSignalName());
    assertNull(signalEvent.getSignalData());
    listener.clearEventsReceived();

    // Check signal using event, and pass in additional payload
    Execution executionWithSignalEvent = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();
    runtimeService.signalEventReceived("alert", executionWithSignalEvent.getId(), Collections.singletonMap("test", (Object) "test"));
    assertEquals(1, listener.getEventsReceived().size());
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEvent);
    signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_SIGNALED, signalEvent.getType());
    assertEquals("shipOrder", signalEvent.getActivityId());
    assertEquals(executionWithSignalEvent.getId(), signalEvent.getExecutionId());
    assertEquals(executionWithSignalEvent.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertEquals("alert", signalEvent.getSignalName());
    assertNotNull(signalEvent.getSignalData());
    listener.clearEventsReceived();

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_SIGNALED);
  }

  /**
   * Test to verify if signals coming from an intermediate throw-event trigger the right events to be dispatched.
   */
  @Deployment
  public void testActivitySignalEventsWithinProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalProcess");
    assertNotNull(processInstance);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    Execution executionWithSignalEvent = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();

    taskService.complete(task.getId());
    assertEquals(1L, listener.getEventsReceived().size());

    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEvent);
    ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_SIGNALED, signalEvent.getType());
    assertEquals("shipOrder", signalEvent.getActivityId());
    assertEquals(executionWithSignalEvent.getId(), signalEvent.getExecutionId());
    assertEquals(executionWithSignalEvent.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertEquals("alert", signalEvent.getSignalName());
    assertNull(signalEvent.getSignalData());
  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageProcess");
    assertNotNull(processInstance);

    Execution executionWithMessage = runtimeService.createExecutionQuery().activityId("shipOrder").singleResult();
    assertNotNull(executionWithMessage);

    runtimeService.messageEventReceived("messageName", executionWithMessage.getId());
    assertEquals(2, listener.getEventsReceived().size());

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiMessageEvent);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, messageEvent.getType());
    assertEquals("shipOrder", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("messageName", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiMessageEvent);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, messageEvent.getType());
    assertEquals("shipOrder", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("messageName", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  /**
   * Test events related to message events, called from the API, targeting an event-subprocess.
   */
  @Deployment
  public void testActivityMessageEventsInEventSubprocess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageProcess");
    assertNotNull(processInstance);

    Execution executionWithMessage = runtimeService.createExecutionQuery().messageEventSubscriptionName("messageName").singleResult();
    assertNotNull(executionWithMessage);

    runtimeService.messageEventReceived("messageName", executionWithMessage.getId());

    // Only a message-event should be present, no signal-event, since the event-subprocess is
    // not signaled, but executed instead
    assertEquals(3, listener.getEventsReceived().size());

    // An ACTIVITY_MESSAGE_WAITING event is expected
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiMessageEvent);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, messageEvent.getType());
    assertEquals("catchMessage", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("messageName", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // An ACTIVITY_MESSAGE_RECEIVED event is expected
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiMessageEvent);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, messageEvent.getType());
    assertEquals("catchMessage", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("messageName", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // An ACTIVITY_CANCELLED event is expected
    assertTrue(listener.getEventsReceived().get(2) instanceof ActivitiActivityCancelledEvent);
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, activityEvent.getType());
    assertEquals("shipOrder", activityEvent.getActivityId());
//    assertEquals("userTask", activityEvent.getActivityType());
    assertEquals(executionWithMessage.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());
  }

  /**
   * Test events related to compensation events.
   */
  @Deployment
  public void testActivityCompensationEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationProcess");
    assertNotNull(processInstance);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);

    // Complete task, next a compensation event will be thrown
    taskService.complete(task.getId());

    assertEquals(1, listener.getEventsReceived().size());

    // A compensate-event is expected
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiActivityEvent);
    ActivitiActivityEvent activityEvent = (ActivitiActivityEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_COMPENSATE, activityEvent.getType());
    assertEquals("compensate", activityEvent.getActivityId());
    // A new execution is created for the compensation-event, this should be
    // visible in the event
    assertFalse(processInstance.getId().equals(activityEvent.getExecutionId()));
    assertEquals(processInstance.getProcessInstanceId(), activityEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), activityEvent.getProcessDefinitionId());

    // Check if the process is still alive
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(processInstance);

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_COMPENSATE);
  }

  /**
   * Test events related to error-events
   */
  @Deployment
  public void testActivityErrorEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
    assertNotNull(processInstance);

    // Error-handling should have ended the process
    ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(afterErrorInstance);

    ActivitiErrorEvent errorEvent = null;

    for (ActivitiEvent event : listener.getEventsReceived()) {
      if (event instanceof ActivitiErrorEvent) {
        if (errorEvent == null) {
          errorEvent = (ActivitiErrorEvent) event;
        } else {
          fail("Only one ActivityErrorEvent expected");
        }
      }
    }

    assertNotNull(errorEvent);
    assertEquals(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, errorEvent.getType());
    assertEquals("catchError", errorEvent.getActivityId());
    assertEquals("myError", errorEvent.getErrorId());
    assertEquals("123", errorEvent.getErrorCode());
    assertEquals(processInstance.getId(), errorEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), errorEvent.getProcessDefinitionId());
    assertFalse(processInstance.getId().equals(errorEvent.getExecutionId()));
  }

  /**
   * Test events related to error-events, thrown from within process-execution (eg. service-task).
   */
  @Deployment
  public void testActivityErrorEventsFromBPMNError() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
    assertNotNull(processInstance);

    // Error-handling should have ended the process
    ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(afterErrorInstance);

    ActivitiErrorEvent errorEvent = null;

    for (ActivitiEvent event : listener.getEventsReceived()) {
      if (event instanceof ActivitiErrorEvent) {
        if (errorEvent == null) {
          errorEvent = (ActivitiErrorEvent) event;
        } else {
          fail("Only one ActivityErrorEvent expected");
        }
      }
    }

    assertNotNull(errorEvent);
    assertEquals(ActivitiEventType.ACTIVITY_ERROR_RECEIVED, errorEvent.getType());
    assertEquals("catchError", errorEvent.getActivityId());
    assertEquals("23", errorEvent.getErrorId());
    assertEquals("23", errorEvent.getErrorCode());
    assertEquals(processInstance.getId(), errorEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), errorEvent.getProcessDefinitionId());
    assertFalse(processInstance.getId().equals(errorEvent.getExecutionId()));
  }

  @Deployment(resources = "org/activiti/engine/test/api/event/JobEventsTest.testJobEntityEvents.bpmn20.xml")
  public void testActivityTimeOutEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Force timer to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 1000);

    // Check timeout has been dispatched
    assertEquals(1, listener.getEventsReceived().size());
    ActivitiEvent activitiEvent = listener.getEventsReceived().get(0);
    assertEquals("ACTIVITY_CANCELLED event expected", ActivitiEventType.ACTIVITY_CANCELLED, activitiEvent.getType());
    ActivitiActivityCancelledEvent cancelledEvent = (ActivitiActivityCancelledEvent) activitiEvent;
    assertTrue("TIMER is the cause of the cancellation", cancelledEvent.getCause() instanceof JobEntity);
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/event/timer/BoundaryTimerEventTest.testTimerOnNestingOfSubprocesses.bpmn20.xml")
  public void testActivityTimeOutEventInSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnNestedSubprocesses");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Force timer to fire
    Calendar timeToFire = Calendar.getInstance();
    timeToFire.add(Calendar.HOUR, 2);
    timeToFire.add(Calendar.SECOND, 5);
    processEngineConfiguration.getClock().setCurrentTime(timeToFire.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 200);

    // Check timeout-events have been dispatched
    assertEquals(4, listener.getEventsReceived().size());
    List<String> eventIdList = new ArrayList<String>();
    for (ActivitiEvent event : listener.getEventsReceived()) {
      assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, event.getType());
      assertTrue("TIMER is the cause of the cancellation", ((ActivitiActivityCancelledEvent) event).getCause() instanceof JobEntity);
      eventIdList.add(((ActivitiActivityEventImpl) event).getActivityId());
    }
    assertTrue(eventIdList.indexOf("innerTask1") >= 0);
    assertTrue(eventIdList.indexOf("innerTask2") >= 0);
    assertTrue(eventIdList.indexOf("subprocess") >= 0);
    assertTrue(eventIdList.indexOf("innerSubprocess") >= 0);
  }

  @Deployment
  public void testActivityTimeOutEventInCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerOnCallActivity");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Force timer to fire
    Calendar timeToFire = Calendar.getInstance();
    timeToFire.add(Calendar.HOUR, 2);
    timeToFire.add(Calendar.MINUTE, 5);
    processEngineConfiguration.getClock().setCurrentTime(timeToFire.getTime());
    waitForJobExecutorToProcessAllJobs(5000, 500);

    // Check timeout-events have been dispatched
    assertEquals(4, listener.getEventsReceived().size());
    List<String> eventIdList = new ArrayList<String>();
    for (ActivitiEvent event : listener.getEventsReceived()) {
      assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, event.getType());
      assertTrue("TIMER is the cause of the cancellation", ((ActivitiActivityCancelledEvent) event).getCause() instanceof JobEntity);
      eventIdList.add(((ActivitiActivityEventImpl) event).getActivityId());
    }
    assertTrue(eventIdList.contains("innerTask1"));
    assertTrue(eventIdList.contains("innerTask2"));
    assertTrue(eventIdList.contains("callActivity"));
    assertTrue(eventIdList.contains("innerSubprocess"));
  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageBoundaryEventsOnUserTask() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageOnUserTaskProcess");
    assertNotNull(processInstance);

    Execution executionWithMessage = runtimeService.createExecutionQuery().messageEventSubscriptionName("message_1").singleResult();
    assertNotNull(executionWithMessage);

    runtimeService.messageEventReceived("message_1", executionWithMessage.getId());
    assertEquals(3, listener.getEventsReceived().size());

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiMessageEvent);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, messageEvent.getType());
    assertEquals("boundaryMessageEventCatching", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("message_1", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiMessageEvent);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, messageEvent.getType());
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, messageEvent.getType());
    assertEquals("boundaryMessageEventCatching", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("message_1", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // Next, an signal-event is expected, as a result of the message
    assertTrue(listener.getEventsReceived().get(2) instanceof ActivitiActivityCancelledEvent);
    ActivitiActivityCancelledEvent signalEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, signalEvent.getType());
    assertEquals("cloudformtask1", signalEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), signalEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertNotNull(signalEvent.getCause());
    assertTrue(signalEvent.getCause() instanceof MessageEventSubscriptionEntity);
    MessageEventSubscriptionEntity cause = (MessageEventSubscriptionEntity) signalEvent.getCause();
    assertEquals("message_1", cause.getEventName());

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  /**
   * Test events related to message events, called from the API.
   */
  @Deployment
  public void testActivityMessageBoundaryEventsOnSubProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("messageOnSubProcess");
    assertNotNull(processInstance);

    Execution executionWithMessage = runtimeService.createExecutionQuery().activityId("boundaryMessageEventCatching").singleResult();
    assertNotNull(executionWithMessage);

    runtimeService.messageEventReceived("message_1", executionWithMessage.getId());
    assertEquals(4, listener.getEventsReceived().size());

    // First, an ACTIVITY_MESSAGE_WAITING event is expected
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiMessageEvent);
    ActivitiMessageEvent messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_WAITING, messageEvent.getType());
    assertEquals("boundaryMessageEventCatching", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("message_1", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // Second, an ACTIVITY_MESSAGE_RECEIVED event is expected
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiMessageEvent);
    messageEvent = (ActivitiMessageEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED, messageEvent.getType());
    assertEquals("boundaryMessageEventCatching", messageEvent.getActivityId());
    assertEquals(executionWithMessage.getId(), messageEvent.getExecutionId());
    assertEquals(executionWithMessage.getProcessInstanceId(), messageEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), messageEvent.getProcessDefinitionId());
    assertEquals("message_1", messageEvent.getMessageName());
    assertNull(messageEvent.getMessageData());

    // Next, a signal-event is expected, as a result of the message
    assertTrue(listener.getEventsReceived().get(2) instanceof ActivitiActivityCancelledEvent);
    ActivitiActivityCancelledEvent signalEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, signalEvent.getType());
    assertEquals("subProcess", signalEvent.getActivityId());
    assertEquals(executionWithMessage.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertNotNull(signalEvent.getCause());
    assertTrue(signalEvent.getCause() instanceof MessageEventSubscriptionEntity);
    MessageEventSubscriptionEntity cause = (MessageEventSubscriptionEntity) signalEvent.getCause();
    assertEquals("message_1", cause.getEventName());
    
    assertTrue(listener.getEventsReceived().get(3) instanceof ActivitiActivityCancelledEvent);
    signalEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, signalEvent.getType());
    assertEquals("cloudformtask1", signalEvent.getActivityId());
    assertEquals(executionWithMessage.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    assertNotNull(signalEvent.getCause());
    assertTrue(signalEvent.getCause() instanceof MessageEventSubscriptionEntity);
    cause = (MessageEventSubscriptionEntity) signalEvent.getCause();
    assertEquals("message_1", cause.getEventName());

    assertDatabaseEventPresent(ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED);
  }

  @Deployment
  public void testActivitySignalBoundaryEventsOnSubProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalOnSubProcess");
    assertNotNull(processInstance);

    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("userTaskInsideProcess").singleResult();
    assertNotNull(executionWithSignal);

    runtimeService.signalEventReceived("signalName");
    assertEquals(3, listener.getEventsReceived().size());
    
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEventImpl);
    ActivitiSignalEventImpl signalEvent = (ActivitiSignalEventImpl) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_SIGNALED, signalEvent.getType());
    assertEquals("boundarySignalEventCatching", signalEvent.getActivityId());
    assertEquals(executionWithSignal.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiActivityCancelledEvent);
    ActivitiActivityCancelledEvent cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, cancelEvent.getType());
    assertEquals("subProcess", cancelEvent.getActivityId());
    assertEquals(executionWithSignal.getProcessInstanceId(), cancelEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), cancelEvent.getProcessDefinitionId());
    assertNotNull(cancelEvent.getCause());
    assertTrue(cancelEvent.getCause() instanceof SignalEventSubscriptionEntity);
    SignalEventSubscriptionEntity cause = (SignalEventSubscriptionEntity) cancelEvent.getCause();
    assertEquals("signalName", cause.getEventName());
    
    assertTrue(listener.getEventsReceived().get(2) instanceof ActivitiActivityCancelledEvent);
    cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, cancelEvent.getType());
    assertEquals("userTaskInsideProcess", cancelEvent.getActivityId());
    assertEquals(executionWithSignal.getId(), cancelEvent.getExecutionId());
    assertEquals(executionWithSignal.getProcessInstanceId(), cancelEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), cancelEvent.getProcessDefinitionId());
    assertNotNull(cancelEvent.getCause());
    assertTrue(cancelEvent.getCause() instanceof SignalEventSubscriptionEntity);
    cause = (SignalEventSubscriptionEntity) cancelEvent.getCause();
    assertEquals("signalName", cause.getEventName());
  }

  @Deployment
  public void testActivitySignalBoundaryEventsOnUserTask() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalOnUserTask");
    assertNotNull(processInstance);

    Execution executionWithSignal = runtimeService.createExecutionQuery().activityId("userTask").singleResult();
    assertNotNull(executionWithSignal);

    runtimeService.signalEventReceived("signalName");

    // Next, an signal-event is expected, as a result of the message
    assertEquals(2, listener.getEventsReceived().size());
    
    assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEventImpl);
    ActivitiSignalEventImpl signalEvent = (ActivitiSignalEventImpl) listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ACTIVITY_SIGNALED, signalEvent.getType());
    assertEquals("boundarySignalEventCatching", signalEvent.getActivityId());
    assertEquals(executionWithSignal.getProcessInstanceId(), signalEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
    
    assertTrue(listener.getEventsReceived().get(1) instanceof ActivitiActivityCancelledEvent);
    ActivitiActivityCancelledEvent cancelEvent = (ActivitiActivityCancelledEvent) listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ACTIVITY_CANCELLED, cancelEvent.getType());
    assertEquals("userTask", cancelEvent.getActivityId());
    assertEquals(executionWithSignal.getProcessInstanceId(), cancelEvent.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), cancelEvent.getProcessDefinitionId());
    assertNotNull(cancelEvent.getCause());
    assertTrue(cancelEvent.getCause() instanceof SignalEventSubscriptionEntity);
    SignalEventSubscriptionEntity cause = (SignalEventSubscriptionEntity) cancelEvent.getCause();
    assertEquals("signalName", cause.getEventName());
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
    assertTrue(found);
  }

}
