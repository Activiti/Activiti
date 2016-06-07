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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Vasile Dirla
 */
public class IntermediateTimerEventRepeatWithEndTest extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithEnd() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    calendar.setTime(baseTime);
    calendar.add(Calendar.MINUTE, 10);
    // after 10 minutes the end Date will be reached but the intermediate timers will ignore it
    // since the end date is validated only when a new timer is going to be created
    DateTime dt = new DateTime(calendar.getTime());
    String dateStr1 = fmt.print(dt);

    calendar.setTime(baseTime);
    calendar.add(Calendar.HOUR, 1);
    calendar.add(Calendar.MINUTE, 30);

    dt = new DateTime(calendar.getTime());
    String dateStr2 = fmt.print(dt);

    // reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch1", dateStr1);
    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch2", dateStr2);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());

    // Test Timer Catch Intermediate Events after completing Task A (endDate
    // not reached but it will be executed according to the expression)
    taskService.complete(task.getId());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
      fail("Expected that job isn't executed because the timer is in t0");
    } catch (Exception e) {
      // expected
    }

    nextTimeCal.add(Calendar.MINUTE, 30); // after 30 minutes the 10 minutes for expire date are ended.
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
      fail("The intermediate timers should not trigger when the endDate is reached.");
    } catch (Exception ex) {
     // expected: The job should not trigger on END Date
    }

    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());

    nextTimeCal.add(Calendar.MINUTE, 30); // after 30 minutes the 10 minutes for expire date are ended.
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
     } catch (Exception ex) {
      fail("The time for intermediate timer is reached and that's why it should trigger and execute it.");
    }

    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Task C", task.getName());

    // Test Timer Catch Intermediate Events after completing Task C
    taskService.complete(task.getId());
    nextTimeCal.add(Calendar.MINUTE, 60);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      fail("Should not have any other jobs because the endDate is reached");
    }

    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

      assertNotNull(historicInstance.getEndTime());
    }

    // now all the process instances should be completed
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertEquals(0, processInstances.size());

    // no jobs
    jobs = managementService.createJobQuery().list();
    assertEquals(0, jobs.size());

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertEquals(0, tasks.size());
  }

}
