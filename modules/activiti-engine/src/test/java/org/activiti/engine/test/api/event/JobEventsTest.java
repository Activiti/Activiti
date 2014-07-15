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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
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
		Job theJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(theJob);
		
		// Check if create-event has been dispatched
		assertEquals(2, listener.getEventsReceived().size());
		ActivitiEvent event = listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
		checkEventContext(event, theJob, false);
		
		event = listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
		checkEventContext(event, theJob, false);
		
		listener.clearEventsReceived();

		// Update the job-entity. Check if update event is dispatched with update job entity
		managementService.setJobRetries(theJob.getId(), 5);
		assertEquals(1, listener.getEventsReceived().size());
		event = listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		Job updatedJob = (Job) ((ActivitiEntityEvent) event).getEntity();
		assertEquals(5, updatedJob.getRetries());
		checkEventContext(event, theJob, true);
		
		listener.clearEventsReceived();
		
		// Force timer to fire
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
		waitForJobExecutorToProcessAllJobs(2000, 100);
		
		// Check delete-event has been dispatched
		assertEquals(3, listener.getEventsReceived().size());
		
		// First, a timer fired event has been dispatched
		event = listener.getEventsReceived().get(0);
		assertEquals(ActivitiEventType.TIMER_FIRED, event.getType());
		checkEventContext(event, theJob, true);
		
	  // Next, a delete event has been dispatched
		event = listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		checkEventContext(event, theJob, true);
		
		// Finally, a complete event has been dispatched
		event = listener.getEventsReceived().get(2);
		assertEquals(ActivitiEventType.JOB_EXECUTION_SUCCESS, event.getType());
		checkEventContext(event, theJob, true);
	}
	
	/**
   * Timer repetition
   */
  @Deployment
  public void testRepetitionJobEntityEvents() throws Exception {
    Clock previousClock = processEngineConfiguration.getClock();

    Calendar testCal = new GregorianCalendar();
    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    testClock.setCurrentTime(testCal.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testRepetitionJobEvents");
    Job theJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(theJob);

    // Check if create-event has been dispatched
    assertEquals(2, listener.getEventsReceived().size());
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob, false);

    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob, false);

    listener.clearEventsReceived();

    // fire timer for the first time
    testCal.add(Calendar.SECOND, 20);
    testClock.setCurrentTime(testCal.getTime());
    waitForJobExecutorToProcessAllJobs(5000, 200);

    // fire timer for the second time
    testCal.add(Calendar.SECOND, 20);
    testClock.setCurrentTime(testCal.getTime());
    waitForJobExecutorToProcessAllJobs(5000, 200);

    // all jobs should have been done now
    assertEquals(0, managementService.createJobQuery().count());

    // count timer fired events
    int timerFiredCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.TIMER_FIRED.equals(eventReceived.getType())) {
        timerFiredCount++;
      }
    }
    listener.clearEventsReceived();
    processEngineConfiguration.setClock(previousClock);

    assertEquals(2, timerFiredCount);
  }

  /**
   * Test TIMER_FIRED event for timer start bpmn event.
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
    assertEquals(3, listener.getEventsReceived().size());
    assertEquals(ActivitiEventType.TIMER_FIRED, listener.getEventsReceived().get(0).getType());
  }

  /**
   * Test TIMER_FIRED event for intermediate timer bpmn event.
   */
  @Deployment
  public void testTimerFiredForIntermediateTimer() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testTimerFiredForIntermediateTimer");

    // Force timer to start the process
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    waitForJobExecutorToProcessAllJobs(2000, 100);

    // Check Timer fired event has been dispatched
    // there is an issue (ENTITY_DELETED for job is generated twice)
    boolean timerFired = false;
    for(ActivitiEvent event : listener.getEventsReceived()) {
      if (ActivitiEventType.TIMER_FIRED.equals(event.getType())) {
        timerFired = true;
      }
    }
    assertTrue(timerFired);
  }

    /**
	 * Test create, update and delete events of jobs entities.
	 */
	@Deployment
	public void testJobEntityEventsException() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
		Job theJob = managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(theJob);
		
		// Set retries to 1, to prevent multiple chains of events being thrown
		managementService.setJobRetries(theJob.getId(), 1);
		
		listener.clearEventsReceived();
		
		// Force timer to fire
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
		waitForJobExecutorToProcessAllJobs(2000, 100);
		
		// Check delete-event has been dispatched
		assertEquals(5, listener.getEventsReceived().size());

    // First, the timer was fired
    ActivitiEvent event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.TIMER_FIRED, event.getType());
    checkEventContext(event, theJob, true);

    // Second, the job-entity was deleted, as the job was executed
		event = listener.getEventsReceived().get(1);
		assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
		checkEventContext(event, theJob, true);
		
	  // Next, a job failed event is dispatched
		event = listener.getEventsReceived().get(2);
		assertEquals(ActivitiEventType.JOB_EXECUTION_FAILURE, event.getType());
		checkEventContext(event, theJob, true);
		
		// Finally, an update-event is received and the job count is decremented
		event = listener.getEventsReceived().get(3);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		checkEventContext(event, theJob, true);
		
		event = listener.getEventsReceived().get(4);
		assertEquals(ActivitiEventType.JOB_RETRIES_DECREMENTED, event.getType());
		assertEquals(0, ((Job) ((ActivitiEntityEvent) event).getEntity()).getRetries());
		checkEventContext(event, theJob, true);
	}
	
	protected void checkEventContext(ActivitiEvent event, Job entity, boolean scopeExecutionExpected) {
		assertEquals(entity.getProcessInstanceId(), event.getProcessInstanceId());
		assertEquals(entity.getProcessDefinitionId(), event.getProcessDefinitionId());
		if(scopeExecutionExpected) {
			assertEquals(entity.getExecutionId(), event.getExecutionId());
		} else {
			assertEquals(entity.getProcessInstanceId(), event.getExecutionId());
		}
		
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
	  
	  if(listener != null) {
	  	processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  }
	}
}
