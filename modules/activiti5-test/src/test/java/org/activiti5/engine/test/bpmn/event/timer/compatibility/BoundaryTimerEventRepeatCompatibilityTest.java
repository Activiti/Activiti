package org.activiti5.engine.test.bpmn.event.timer.compatibility;

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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class BoundaryTimerEventRepeatCompatibilityTest extends TimerEventCompatibilityTest {

  @Deployment
  public void testRepeatWithoutEnd() throws Throwable {
    Clock clock = processEngineConfiguration.getClock();
    
    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    calendar.add(Calendar.MINUTE, 20);
    //expect to stop boundary jobs after 20 minutes
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    DateTime dt = new DateTime(calendar.getTime());
    String dateStr = fmt.print(dt);

    //reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    clock.setCurrentCalendar(nextTimeCal);
    processEngineConfiguration.setClock(clock);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForBoundary", dateStr);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());

    // Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertEquals(1, jobs.size());

    //boundary events

    waitForJobExecutorToProcessAllJobs(2000, 200);
    // a new job must be prepared because there are 10 repeats 2 seconds interval

    for (int i = 0; i < 9; i++) {
      nextTimeCal.add(Calendar.SECOND, 2);
      clock.setCurrentCalendar(nextTimeCal);
      processEngineConfiguration.setClock(clock);
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    }

    nextTimeCal.add(Calendar.SECOND, 2);
    clock.setCurrentCalendar(nextTimeCal);
    processEngineConfiguration.setClock(clock);

    waitForJobExecutorToProcessAllJobs(2000, 200);
    
    // Should not have any other jobs because the endDate is reached
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());
    
    tasks = taskService.createTaskQuery().list();
    task = tasks.get(0);
    assertEquals("Task B", task.getName());
    assertEquals(1, tasks.size());
    taskService.complete(task.getId());

    waitForJobExecutorToProcessAllJobs(2000, 200);

    // now All the process instances should be completed
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, processInstances.size());

    // no jobs
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());
    jobs = managementService.createTimerJobQuery().list();
    assertEquals(0, jobs.size());

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertEquals(0, tasks.size());

    processEngineConfiguration.resetClock();
  }

}
