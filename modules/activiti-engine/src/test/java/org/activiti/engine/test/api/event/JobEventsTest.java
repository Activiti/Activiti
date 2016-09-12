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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to jobs.
 * 
 * @author Frederik Heremans
 */
public class JobEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Test create, update and delete events of jobs entities.
   */
  @Deployment
  public void testJobEntityEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Check if create-event has been dispatched
    assertEquals(3, listener.getEventsReceived().size());
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.TIMER_SCHEDULED, event.getType());
    checkEventContext(event, theJob);

    listener.clearEventsReceived();

    // Update the job-entity. Check if update event is dispatched with update job entity
    managementService.setTimerJobRetries(theJob.getId(), 5);
    assertEquals(1, listener.getEventsReceived().size());
    event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
    Job updatedJob = (Job) ((ActivitiEntityEvent) event).getEntity();
    assertEquals(5, updatedJob.getRetries());
    checkEventContext(event, theJob);

    checkEventCount(0, ActivitiEventType.TIMER_SCHEDULED);
    listener.clearEventsReceived();

    // Force timer to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    String jobId = managementService.createTimerJobQuery().singleResult().getId();
    managementService.moveTimerToExecutableJob(jobId);
    managementService.executeJob(jobId);

    // Check delete-event has been dispatched
    assertEquals(6, listener.getEventsReceived().size());
    
    event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob);
    
    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob);

    // First, a timer fired event has been dispatched
    event = listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.TIMER_FIRED, event.getType());
    checkEventContext(event, theJob);

    // Next, a delete event has been dispatched
    event = listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
    checkEventContext(event, theJob);

    // Finally, a complete event has been dispatched
    event = listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.JOB_EXECUTION_SUCCESS, event.getType());
    checkEventContext(event, theJob);

    checkEventCount(0, ActivitiEventType.TIMER_SCHEDULED);
  }

  /**
   * Timer repetition
   */
  @Deployment
  public void testRepetitionJobEntityEvents() throws Exception {
    Clock previousClock = processEngineConfiguration.getClock();

    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    Date now = new Date();
    testClock.setCurrentTime(now);
    
    Calendar nowCalendar = new GregorianCalendar();
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testRepetitionJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Check if create-event has been dispatched
    assertEquals(3, listener.getEventsReceived().size());
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.TIMER_SCHEDULED, event.getType());
    checkEventContext(event, theJob);

    listener.clearEventsReceived();

    // no timer jobs will be fired
    waitForJobExecutorToProcessAllJobs(2000, 200);
    assertEquals(0, listener.getEventsReceived().size());
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    Job firstTimerInstance = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    
    nowCalendar.add(Calendar.HOUR, 1);
    nowCalendar.add(Calendar.MINUTE, 5);
    testClock.setCurrentTime(nowCalendar.getTime());
    
    // the timer job will be fired for the first time now
    waitForJobExecutorToProcessAllJobs(2000, 200);
    
    // a new timer should be created with the repeat
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    Job secondTimerInstance = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTrue(firstTimerInstance.getId() != secondTimerInstance.getId());

    checkEventCount(1, ActivitiEventType.TIMER_FIRED);
    checkEventContext(filterEvents(ActivitiEventType.TIMER_FIRED).get(0), firstTimerInstance);
    checkEventCount(1, ActivitiEventType.TIMER_SCHEDULED);
    checkEventContext(filterEvents(ActivitiEventType.TIMER_SCHEDULED).get(0), secondTimerInstance);

    listener.clearEventsReceived();

    nowCalendar.add(Calendar.HOUR, 1);
    nowCalendar.add(Calendar.MINUTE, 5);
    testClock.setCurrentTime(nowCalendar.getTime());

    // the second timer job will be fired and no jobs should be remaining
    waitForJobExecutorToProcessAllJobs(2000, 200);
    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());

    nowCalendar.add(Calendar.HOUR, 1);
    nowCalendar.add(Calendar.MINUTE, 5);
    testClock.setCurrentTime(nowCalendar.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 200);
    
    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    
    checkEventCount(1, ActivitiEventType.TIMER_FIRED);
    checkEventContext(filterEvents(ActivitiEventType.TIMER_FIRED).get(0), secondTimerInstance);
    checkEventCount(0, ActivitiEventType.TIMER_SCHEDULED);

    listener.clearEventsReceived();
    processEngineConfiguration.setClock(previousClock);
  }

  @Deployment
  public void testJobCanceledEventOnBoundaryEvent() throws Exception {
    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    testClock.setCurrentTime(new Date());
    runtimeService.startProcessInstanceByKey("testTimerCancelledEvent");
    listener.clearEventsReceived();

    Task task = taskService.createTaskQuery().singleResult();

    taskService.complete(task.getId());

    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
  }

  @Deployment(resources = "org/activiti/engine/test/api/event/JobEventsTest.testJobCanceledEventOnBoundaryEvent.bpmn20.xml")
  public void testJobCanceledEventByManagementService() throws Exception {
    // GIVEN
    processEngineConfiguration.getClock().setCurrentTime(new Date());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testTimerCancelledEvent");
    listener.clearEventsReceived();

    Job job = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    // WHEN
    managementService.deleteTimerJob(job.getId());

    // THEN
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
  }

  public void testJobCanceledAndTimerStartEventOnProcessRedeploy() throws Exception {
    // GIVEN deploy process definition
    String deployment1 = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/event/JobEventsTest.testTimerFiredForTimerStart.bpmn20.xml").deploy().getId();
    checkEventCount(1, ActivitiEventType.TIMER_SCHEDULED);
    listener.clearEventsReceived();

    // WHEN
    String deployment2 = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/event/JobEventsTest.testTimerFiredForTimerStart.bpmn20.xml").deploy().getId();

    // THEN
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
    checkEventCount(1, ActivitiEventType.TIMER_SCHEDULED);

    listener.clearEventsReceived();

    repositoryService.deleteDeployment(deployment2);
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
    checkEventCount(1, ActivitiEventType.TIMER_SCHEDULED);

    listener.clearEventsReceived();

    repositoryService.deleteDeployment(deployment1);
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
  }

  private void checkEventCount(int expectedCount, ActivitiEventType eventType) {// count
                                                                                // timer
                                                                                // cancelled
                                                                                // events
    int timerCancelledCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (eventType.equals(eventReceived.getType())) {
        timerCancelledCount++;
      }
    }
    assertEquals(eventType.name() + " event was expected " + expectedCount + " times.", expectedCount, timerCancelledCount);
  }

  private List<ActivitiEvent> filterEvents(ActivitiEventType eventType) {
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    List<ActivitiEvent> filteredEvents = new ArrayList<>();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (eventType.equals(eventReceived.getType())) {
        filteredEvents.add(eventReceived);
      }
    }
    return filteredEvents;
  }
  /**
   * /** Test TIMER_FIRED event for timer start bpmn event.
   */
  @Deployment
  public void testTimerFiredForTimerStart() throws Exception {
    // there should be one job after process definition deployment

    // Force timer to start the process
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 100);

    // Check Timer fired event has been dispatched
    assertEquals(6, listener.getEventsReceived().size());
    
    // timer entity created first
    assertEquals(ActivitiEventType.ENTITY_CREATED, listener.getEventsReceived().get(0).getType());
    // timer entity initialized
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, listener.getEventsReceived().get(1).getType());
    // timer entity deleted
    assertEquals(ActivitiEventType.ENTITY_DELETED, listener.getEventsReceived().get(2).getType());
    // job fired
    assertEquals(ActivitiEventType.TIMER_FIRED, listener.getEventsReceived().get(3).getType());
    // job executed successfully
    assertEquals(ActivitiEventType.JOB_EXECUTION_SUCCESS, listener.getEventsReceived().get(5).getType());
    
    checkEventCount(0, ActivitiEventType.JOB_CANCELED);
  }

  /**
   * Test TIMER_FIRED event for intermediate timer bpmn event.
   */
  @Deployment
  public void testTimerFiredForIntermediateTimer() throws Exception {
    runtimeService.startProcessInstanceByKey("testTimerFiredForIntermediateTimer");

    // Force timer to start the process
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 100);

    checkEventCount(1, ActivitiEventType.TIMER_SCHEDULED);
    checkEventCount(0, ActivitiEventType.JOB_CANCELED);
    checkEventCount(1, ActivitiEventType.TIMER_FIRED);
  }

  /**
   * Test create, update and delete events of jobs entities.
   */
  @Deployment
  public void testJobEntityEventsException() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Set retries to 1, to prevent multiple chains of events being thrown
    managementService.setTimerJobRetries(theJob.getId(), 1);

    // Force timer to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    
    Job executableJob = managementService.moveTimerToExecutableJob(theJob.getId());
    
    listener.clearEventsReceived();
    
    try {
      managementService.executeJob(executableJob.getId());
      fail("Expected exception");
    } catch (Exception e) {
      // exception expected
    }
    
    theJob = managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Check delete-event has been dispatched
    assertEquals(8, listener.getEventsReceived().size());

    // First, the timer was fired
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.TIMER_FIRED, event.getType());
    checkEventContext(event, theJob);

    // Second, the job-entity was deleted, as the job was executed
    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
    checkEventContext(event, theJob);

    // Next, a job failed event is dispatched
    event = listener.getEventsReceived().get(2);
    assertEquals(ActivitiEventType.JOB_EXECUTION_FAILURE, event.getType());
    checkEventContext(event, theJob);

    // Finally, an timer create event is received and the job count is decremented
    event = listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob);
    
    event = listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob);
    
    // original job is deleted
    event = listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
    checkEventContext(event, theJob);
    
    // timer job updated
    event = listener.getEventsReceived().get(6);
    assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(7);
    assertEquals(ActivitiEventType.JOB_RETRIES_DECREMENTED, event.getType());
    assertEquals(0, ((Job) ((ActivitiEntityEvent) event).getEntity()).getRetries());
    checkEventContext(event, theJob);
  }
  
  @Deployment
  public void testTerminateEndEvent() throws Exception {
    Clock previousClock = processEngineConfiguration.getClock();

    TestActivitiEventListener activitiEventListener = new TestActivitiEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(activitiEventListener);
    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    testClock.setCurrentTime(new Date());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testTerminateEndEvent");
    listener.clearEventsReceived();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Inside Task", task.getName());

    // Force timer to trigger so that subprocess will flow to terminate end event
    Calendar later = Calendar.getInstance();
    later.add(Calendar.YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(later.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 100);

    // Process Cancelled event should not be sent for the subprocess
    List<ActivitiEvent> eventsReceived = activitiEventListener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.PROCESS_CANCELLED.equals(eventReceived.getType())) {
        fail("Should not have received PROCESS_CANCELLED event");
      }
    }

    // validate the activityType string
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.ACTIVITY_CANCELLED.equals(eventReceived.getType())) {
        ActivitiActivityEvent event = (ActivitiActivityEvent) eventReceived;
        String activityType = event.getActivityType();
        if (!"userTask".equals(activityType) && (!"subProcess".equals(activityType)) && (!"endEvent".equals(activityType))) {
          fail("Unexpected activity type: " + activityType);
        }
      }
    }

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("Outside Task", task.getName());

    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());

    processEngineConfiguration.setClock(previousClock);
  }

  protected void checkEventContext(ActivitiEvent event, Job entity) {
    assertEquals(entity.getProcessInstanceId(), event.getProcessInstanceId());
    assertEquals(entity.getProcessDefinitionId(), event.getProcessDefinitionId());
    assertNotNull(event.getExecutionId());

    assertTrue(event instanceof ActivitiEntityEvent);
    ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
    assertTrue(entityEvent.getEntity() instanceof Job);
    assertEquals(entity.getId(), ((Job) entityEvent.getEntity()).getId());
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Job.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}
