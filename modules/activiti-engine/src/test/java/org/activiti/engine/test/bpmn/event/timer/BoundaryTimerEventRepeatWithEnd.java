package org.activiti.engine.test.bpmn.event.timer;

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

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

public class BoundaryTimerEventRepeatWithEnd extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithEnd() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    calendar.add(Calendar.MINUTE, 20);
     //expect to stop boundary jobs after 20 minutes
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    DateTime dt = new DateTime(calendar.getTime());
    String dateStr = fmt.print(dt);

    calendar.setTime(baseTime);
    calendar.add(Calendar.HOUR, 2);
    //expect to wait after competing task B for 1 hour even I set the end date for 2 hours (the expression will trigger the execution)
    dt = new DateTime(calendar.getTime());
    String dateStr1 = fmt.print(dt);

    calendar.setTime(baseTime);
    calendar.add(Calendar.HOUR, 1);
    calendar.add(Calendar.MINUTE, 30);
    //expect to wait after competing task B for 1 hour and 30 minutes (the end date will be reached, the expression will not be considered)
    dt = new DateTime(calendar.getTime());
    String dateStr2 = fmt.print(dt);


    //reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForBoundary", dateStr);
    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch1", dateStr1);
    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch2", dateStr2);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());


    //Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
    //boundary events
    managementService.executeJob(jobs.get(0).getId());
    jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());

    nextTimeCal.add(Calendar.MINUTE, 15); //after 15 minutes
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());
    managementService.executeJob(jobs.get(0).getId());
    jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());


    nextTimeCal.add(Calendar.MINUTE, 5); //after another 5 minutes (20 minutes and 1 second from the baseTime) the BoundaryEndTime is reached
    nextTimeCal.add(Calendar.SECOND, 1);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());
    managementService.executeJob(jobs.get(0).getId());
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size()); //no others jobs are created

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Task B", task.getName());



    //Test Timer Catch Intermediate Events after completing Task B (endDate not reached but it will be executed accrding to the expression)
    taskService.complete(task.getId());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 500);
      fail("Expected that job isn't executed because the timer is in t0");
    } catch (Exception e) {
      // expected
    }

    nextTimeCal.add(Calendar.HOUR, 1); //after 1 hour the event must be triggered and the flow will go to the next step
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    waitForJobExecutorToProcessAllJobs(2000, 500);
    //expect to execute because the time is reached.

    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());


    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Task C", task.getName());


    //Test Timer Catch Intermediate Events after completing Task C
    taskService.complete(task.getId());
    nextTimeCal.add(Calendar.MINUTE, 10); //after 10 minutes will be 1 hour and 30 minutes after the process start time (the endTime for Intermediate Timer 2)
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    waitForJobExecutorToProcessAllJobs(2000, 500);
    //expect to execute because the end time is reached.

    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstance.getId())
            .singleResult();

    assertNotNull(historicInstance.getEndTime());
  }

}
