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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
    assertThat(theJob).isNotNull();

    // Check if create-event has been dispatched
    assertThat(listener.getEventsReceived()).hasSize(3);
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
    checkEventContext(event, theJob);

    listener.clearEventsReceived();

    // Update the job-entity. Check if update event is dispatched with update job entity
    managementService.setTimerJobRetries(theJob.getId(), 5);
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    Job updatedJob = (Job) ((ActivitiEntityEvent) event).getEntity();
    assertThat(updatedJob.getRetries()).isEqualTo(5);
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
    assertThat(listener.getEventsReceived()).hasSize(6);

    event = listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    checkEventContext(event, theJob);

    // First, a timer fired event has been dispatched
    event = listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_FIRED);
    checkEventContext(event, theJob);

    // Next, a delete event has been dispatched
    event = listener.getEventsReceived().get(4);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    checkEventContext(event, theJob);

    // Finally, a complete event has been dispatched
    event = listener.getEventsReceived().get(5);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_EXECUTION_SUCCESS);
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
    assertThat(theJob).isNotNull();

    // Check if create-event has been dispatched
    assertThat(listener.getEventsReceived()).hasSize(3);
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_SCHEDULED);
    checkEventContext(event, theJob);

    listener.clearEventsReceived();

    // no timer jobs will be fired
    waitForJobExecutorToProcessAllJobs(2000, 200);
    assertThat(listener.getEventsReceived()).hasSize(0);
    assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
    Job firstTimerInstance = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    nowCalendar.add(Calendar.HOUR, 1);
    nowCalendar.add(Calendar.MINUTE, 5);
    testClock.setCurrentTime(nowCalendar.getTime());

    // the timer job will be fired for the first time now
    waitForJobExecutorToProcessAllJobs(2000, 200);

    // a new timer should be created with the repeat
    assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
    Job secondTimerInstance = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(firstTimerInstance.getId() != secondTimerInstance.getId()).isTrue();

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
    assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

    nowCalendar.add(Calendar.HOUR, 1);
    nowCalendar.add(Calendar.MINUTE, 5);
    testClock.setCurrentTime(nowCalendar.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 200);

    assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

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
    assertThat(timerCancelledCount).as(eventType.name() + " event was expected " + expectedCount + " times.").isEqualTo(expectedCount);
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
    assertThat(listener.getEventsReceived()).hasSize(6);

    // timer entity created first
    assertThat(listener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    // timer entity initialized
    assertThat(listener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    // timer entity deleted
    assertThat(listener.getEventsReceived().get(2).getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    // job fired
    assertThat(listener.getEventsReceived().get(3).getType()).isEqualTo(ActivitiEventType.TIMER_FIRED);
    // job executed successfully
    assertThat(listener.getEventsReceived().get(5).getType()).isEqualTo(ActivitiEventType.JOB_EXECUTION_SUCCESS);

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
    assertThat(theJob).isNotNull();

    // Set retries to 1, to prevent multiple chains of events being thrown
    managementService.setTimerJobRetries(theJob.getId(), 1);

    // Force timer to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());

    Job executableJob = managementService.moveTimerToExecutableJob(theJob.getId());

    listener.clearEventsReceived();

    assertThatExceptionOfType(Exception.class)
      .isThrownBy(() -> managementService.executeJob(executableJob.getId()));

    theJob = managementService.createDeadLetterJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(theJob).isNotNull();

    // Check delete-event has been dispatched
    assertThat(listener.getEventsReceived()).hasSize(8);

    // First, the timer was fired
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.TIMER_FIRED);
    checkEventContext(event, theJob);

    // Second, the job-entity was deleted, as the job was executed
    event = listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    checkEventContext(event, theJob);

    // Next, a job failed event is dispatched
    event = listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_EXECUTION_FAILURE);
    checkEventContext(event, theJob);

    // Finally, an timer create event is received and the job count is decremented
    event = listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(4);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    checkEventContext(event, theJob);

    // original job is deleted
    event = listener.getEventsReceived().get(5);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    checkEventContext(event, theJob);

    // timer job updated
    event = listener.getEventsReceived().get(6);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    checkEventContext(event, theJob);

    event = listener.getEventsReceived().get(7);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.JOB_RETRIES_DECREMENTED);
    assertThat(((Job) ((ActivitiEntityEvent) event).getEntity()).getRetries()).isEqualTo(0);
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
    assertThat(task.getName()).isEqualTo("Inside Task");

    // Force timer to trigger so that subprocess will flow to terminate end event
    Calendar later = Calendar.getInstance();
    later.add(Calendar.YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(later.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 100);

    // Process Cancelled event should not be sent for the subprocess
    List<ActivitiEvent> eventsReceived = activitiEventListener.getEventsReceived();
    assertThat(eventsReceived)
        .filteredOn(eventReceived -> ActivitiEventType.PROCESS_CANCELLED.equals(eventReceived.getType()))
        .as("Should not have received PROCESS_CANCELLED event")
        .isEmpty();

    // validate the activityType string
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.ACTIVITY_CANCELLED.equals(eventReceived.getType())) {
        ActivitiActivityEvent event = (ActivitiActivityEvent) eventReceived;
        String activityType = event.getActivityType();
        assertThat(activityType)
            .as("Unexpected activity type: " + activityType)
            .isIn("userTask", "subProcess", "endEvent");
      }
    }

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Outside Task");

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    processEngineConfiguration.setClock(previousClock);
  }

  protected void checkEventContext(ActivitiEvent event, Job entity) {
    assertThat(event.getProcessInstanceId()).isEqualTo(entity.getProcessInstanceId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(entity.getProcessDefinitionId());
    assertThat(event.getExecutionId()).isNotNull();

    assertThat(event).isInstanceOf(ActivitiEntityEvent.class);
    ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
    assertThat(entityEvent.getEntity()).isInstanceOf(Job.class);
    assertThat(((Job) entityEvent.getEntity()).getId()).isEqualTo(entity.getId());
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
