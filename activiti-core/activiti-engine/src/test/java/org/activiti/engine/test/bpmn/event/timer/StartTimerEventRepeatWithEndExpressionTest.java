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
package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.api.event.TestActivitiEntityEventListener;

/**

 */
public class StartTimerEventRepeatWithEndExpressionTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

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

  /**
   * Timer repetition
   */
  public void testCycleDateStartTimerEvent() throws Exception {
    Clock previousClock = processEngineConfiguration.getClock();

    Clock testClock = new DefaultClockImpl();

    processEngineConfiguration.setClock(testClock);

    Calendar calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.DECEMBER, 10, 0, 0, 0);
    testClock.setCurrentTime(calendar.getTime());

    // deploy the process
    repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventRepeatWithEndExpressionTest.testCycleDateStartTimerEvent.bpmn20.xml").deploy();
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);

    // AFTER DEPLOYMENT
    // when the process is deployed there will be created a timerStartEvent
    // job which will wait to be executed.
    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(1);

    // dueDate should be after 24 hours from the process deployment
    Calendar dueDateCalendar = Calendar.getInstance();
    dueDateCalendar.set(2025, Calendar.DECEMBER, 11, 0, 0, 0);

    // check the due date is inside the 2 seconds range
    assertThat(Math.abs(dueDateCalendar.getTime().getTime() - jobs.get(0).getDuedate().getTime()) < 2000).isEqualTo(true);

    // No process instances
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(0);

    // No tasks
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);

    // ADVANCE THE CLOCK
    // advance the clock to 11 dec -> the system will execute the pending
    // job and will create a new one
    moveByMinutes(60 * 24);
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000L, 200);

    // there must be a pending job because the endDate is not reached yet");
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

    // After the first startEvent Execution should be one process instance started
    processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(1);

    // one task to be executed (the userTask "Task A")
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    // one new job will be created (and the old one will be deleted after execution)
    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(1);

    dueDateCalendar = Calendar.getInstance();
    dueDateCalendar.set(2025, Calendar.DECEMBER, 12, 0, 0, 0);

    assertThat(Math.abs(dueDateCalendar.getTime().getTime() - jobs.get(0).getDuedate().getTime()) < 2000).isEqualTo(true);

    // ADVANCE THE CLOCK SO THE END DATE WILL BE REACHED
    // 12 dec (last execution)
    moveByMinutes(60 * 24);
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);

    // After the second startEvent Execution should have 2 process instances started
    // (since the first one was not completed)
    processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(2);

    // Because the endDate 12.dec.2025 is reached
    // the current job will be deleted after execution and a new one will
    // not be created.
    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(0);
    jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    // 2 tasks to be executed (the userTask "Task A")
    // one task for each process instance
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    // count "timer fired" events
    int timerFiredCount = 0;
    List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.TIMER_FIRED.equals(eventReceived.getType())) {
        timerFiredCount++;
      }
    }

    // count "entity created" events
    int eventCreatedCount = 0;
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.ENTITY_CREATED.equals(eventReceived.getType())) {
        eventCreatedCount++;
      }
    }

    // count "entity deleted" events
    int eventDeletedCount = 0;
    for (ActivitiEvent eventReceived : eventsReceived) {
      if (ActivitiEventType.ENTITY_DELETED.equals(eventReceived.getType())) {
        eventDeletedCount++;
      }
    }
    assertThat(timerFiredCount).isEqualTo(2); // 2 timers fired
    assertThat(eventCreatedCount).isEqualTo(4); // 4 jobs created, each timer creates one timer and one executable job
    assertThat(eventDeletedCount).isEqualTo(4); // 4 jobs deleted, each timer results in deleting one timer and one executable job

    // for each processInstance
    // let's complete the userTasks where the process is hanging in order to
    // complete the processes.
    for (ProcessInstance processInstance : processInstances) {
      tasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).list();
      Task task = tasks.get(0);
      assertThat(task.getName()).isEqualTo("Task A");
      assertThat(tasks).hasSize(1);
      taskService.complete(task.getId());
    }

    // now All the process instances should be completed
    processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(0);

    // no jobs
    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(0);
    jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);

    listener.clearEventsReceived();
    processEngineConfiguration.setClock(previousClock);

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);

  }

  private void moveByMinutes(int minutes) throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + ((minutes * 60 * 1000))));
  }

}
