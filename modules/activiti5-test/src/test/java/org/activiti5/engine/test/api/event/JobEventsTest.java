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
package org.activiti5.engine.test.api.event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

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
	  Clock clock = processEngineConfiguration.getClock();
	  
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
		Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
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
		managementService.setTimerJobRetries(theJob.getId(), 5);
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
		clock.setCurrentCalendar(tomorrow);
		processEngineConfiguration.setClock(clock);
		waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
		
		// Check delete-event has been dispatched
		assertEquals(6, listener.getEventsReceived().size());
		
		// First, a timer fired event has been dispatched
		event = listener.getEventsReceived().get(0);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob, true);
    
    event = listener.getEventsReceived().get(1);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob, true);

    // First, a timer fired event has been dispatched
    event = listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.TIMER_FIRED, event.getType());
    checkEventContext(event, theJob, true);

    // Next, a delete event has been dispatched
    event = listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
    checkEventContext(event, theJob, true);

    // Finally, a complete event has been dispatched
    event = listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.JOB_EXECUTION_SUCCESS, event.getType());
    checkEventContext(event, theJob, true);
	
		processEngineConfiguration.resetClock();
	}

  /**
   * Timer repetition
   */
  @Deployment
  public void testRepetitionJobEntityEvents() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    
    clock.reset();
    Date now = clock.getCurrentTime();
    processEngineConfiguration.setClock(clock);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testRepetitionJobEvents");
    Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
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

    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    assertEquals(0, listener.getEventsReceived().size());
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    
    // a new job must be prepared because there are 2 repeats")
    
    clock.setCurrentTime(new Date(now.getTime() + 10000L));
    processEngineConfiguration.setClock(clock);
    
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    
    // a new job must be prepared because there are 2 repeats
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    
    clock.setCurrentTime(new Date(now.getTime() + 20000L));
    processEngineConfiguration.setClock(clock);
    
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    
    // There must be no jobs remaining
    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());

    clock.setCurrentTime(new Date(now.getTime() + 30000L));
    processEngineConfiguration.setClock(clock);
    
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    
    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).count());
    
    // count timer fired events
    int timerFiredCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.TIMER_FIRED.equals(eventReceived.getType())) {
        timerFiredCount++;
      }
    }
    listener.clearEventsReceived();

    assertEquals(2, timerFiredCount);
    
    processEngineConfiguration.resetClock();
  }

  @Deployment
  public void testJobCanceledEventOnBoundaryEvent() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    clock.reset();
    processEngineConfiguration.setClock(clock);
    runtimeService.startProcessInstanceByKey("testTimerCancelledEvent");
    listener.clearEventsReceived();

    Task task = taskService.createTaskQuery().singleResult();

    taskService.complete(task.getId());

    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
    
    processEngineConfiguration.resetClock();
  }

  @Deployment(resources = "org/activiti5/engine/test/api/event/JobEventsTest.testJobCanceledEventOnBoundaryEvent.bpmn20.xml")
  public void testJobCanceledEventByManagementService() throws Exception {
    // GIVEN
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testTimerCancelledEvent");
    listener.clearEventsReceived();

    Job job = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    // WHEN
    managementService.deleteTimerJob(job.getId());

    // THEN
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);
  }

  public void testJobCanceledEventOnProcessRedeploy() throws Exception {
    // GIVEN
    // deploy process definition
    String deployment1 = repositoryService.createDeployment()
        .addClasspathResource("org/activiti5/engine/test/api/event/JobEventsTest.testTimerFiredForTimerStart.bpmn20.xml")
        .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
        .deploy()
        .getId();
    listener.clearEventsReceived();

    // WHEN
    String deployment2 = repositoryService.createDeployment()
        .addClasspathResource("org/activiti5/engine/test/api/event/JobEventsTest.testTimerFiredForTimerStart.bpmn20.xml")
        .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
        .deploy()
        .getId();

    // THEN
    checkEventCount(1, ActivitiEventType.JOB_CANCELED);

    repositoryService.deleteDeployment(deployment2);
    repositoryService.deleteDeployment(deployment1);
  }

  private void checkEventCount(int expectedCount, ActivitiEventType eventType) {// count timer cancelled events
    int timerCancelledCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (eventType.equals(eventReceived.getType())) {
        timerCancelledCount++;
      }
    }
    assertEquals(eventType.name() + " event was expected "+ expectedCount+" times.", expectedCount, timerCancelledCount);
  }

  /**
   * Test TIMER_FIRED event for timer start bpmn event.
   */
  @Deployment
  public void testTimerFiredForTimerStart() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    
    // there should be one job after process definition deployment
    
    // Force timer to start the process
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    clock.setCurrentCalendar(tomorrow);
    processEngineConfiguration.setClock(clock);
    waitForJobExecutorToProcessAllJobs(2000, 200);

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
    
    processEngineConfiguration.resetClock();
  }

  /**
   * Test TIMER_FIRED event for intermediate timer bpmn event.
   */
  @Deployment
  public void testTimerFiredForIntermediateTimer() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    
    runtimeService.startProcessInstanceByKey("testTimerFiredForIntermediateTimer");

    // Force timer to start the process
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    clock.setCurrentCalendar(tomorrow);
    processEngineConfiguration.setClock(clock);
    waitForJobExecutorToProcessAllJobs(2000, 200);

    checkEventCount(0, ActivitiEventType.JOB_CANCELED);
    checkEventCount(1, ActivitiEventType.TIMER_FIRED);
    
    processEngineConfiguration.resetClock();
  }

  /**
	 * Test create, update and delete events of jobs entities.
	 */
	@Deployment
	public void testJobEntityEventsException() throws Exception {
	  Clock clock = processEngineConfiguration.getClock();
    
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testJobEvents");
		Job theJob = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();
		assertNotNull(theJob);
		
		// Set retries to 1, to prevent multiple chains of events being thrown
		managementService.setTimerJobRetries(theJob.getId(), 1);
		
		listener.clearEventsReceived();
		
		// Force timer to fire
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		clock.setCurrentCalendar(tomorrow);
		processEngineConfiguration.setClock(clock);
		
		managementService.moveTimerToExecutableJob(theJob.getId());
		
		listener.clearEventsReceived();
		
		try {
		  managementService.executeJob(theJob.getId());
		  fail("Expected exception");
		} catch (Exception e) {
		  // exception expected
		}
		
		// Check delete-event has been dispatched
		assertEquals(8, listener.getEventsReceived().size());

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
		
		// Finally, an timer create event is received and the job count is decremented
    event = listener.getEventsReceived().get(3);
    assertEquals(ActivitiEventType.ENTITY_CREATED, event.getType());
    checkEventContext(event, theJob, true);
    
    event = listener.getEventsReceived().get(4);
    assertEquals(ActivitiEventType.ENTITY_INITIALIZED, event.getType());
    checkEventContext(event, theJob, true);
    
    // original job is deleted
    event = listener.getEventsReceived().get(5);
    assertEquals(ActivitiEventType.ENTITY_DELETED, event.getType());
    checkEventContext(event, theJob, true);
		
		// Finally, an update-event is received and the job count is decremented
		event = listener.getEventsReceived().get(6);
		assertEquals(ActivitiEventType.ENTITY_UPDATED, event.getType());
		checkEventContext(event, theJob, true);
		
		event = listener.getEventsReceived().get(7);
		assertEquals(ActivitiEventType.JOB_RETRIES_DECREMENTED, event.getType());
		assertEquals(0, ((Job) ((ActivitiEntityEvent) event).getEntity()).getRetries());
		checkEventContext(event, theJob, true);
	
		processEngineConfiguration.resetClock();
	}
	
	protected void checkEventContext(ActivitiEvent event, Job entity, boolean scopeExecutionExpected) {
		assertEquals(entity.getProcessInstanceId(), event.getProcessInstanceId());
		assertEquals(entity.getProcessDefinitionId(), event.getProcessDefinitionId());
		if (scopeExecutionExpected) {
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
	  
	  if (listener != null) {
	    processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
	  }
	}
}
