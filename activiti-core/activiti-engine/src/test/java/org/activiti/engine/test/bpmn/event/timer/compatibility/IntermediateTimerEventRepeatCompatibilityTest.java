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


package org.activiti.engine.test.bpmn.event.timer.compatibility;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class IntermediateTimerEventRepeatCompatibilityTest extends TimerEventCompatibilityTest {

  @Deployment
  public void testRepeatWithEnd() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    // expect to stop boundary jobs after 20 minutes
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    calendar.setTime(baseTime);
    calendar.add(Calendar.HOUR, 2);
    // expect to wait after completing task A for 1 hour even I set the end
    // date for 2 hours (the expression will trigger the execution)
    DateTime dt = new DateTime(calendar.getTime());
    String endDateForIntermediate1 = fmt.print(dt);

    calendar.setTime(baseTime);
    calendar.add(Calendar.HOUR, 1);
    calendar.add(Calendar.MINUTE, 30);
    // expect to wait after completing task B for 1 hour and 30 minutes (the
    // end date will be reached, the expression will not be considered)
    dt = new DateTime(calendar.getTime());
    String endDateForIntermediate2 = fmt.print(dt);

    // reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch1", endDateForIntermediate1);
    runtimeService.setVariable(processInstance.getId(), "EndDateForCatch2", endDateForIntermediate2);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task A");

    // Test Timer Catch Intermediate Events after completing Task B (endDate
    // not reached but it will be executed according to the expression)
    taskService.complete(task.getId());

    waitForJobExecutorToProcessAllJobs(2000, 500);
    // Expected that job isn't executed because the timer is in t0
    assertThat(managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult()).isNotNull();

    nextTimeCal.add(Calendar.HOUR, 1); // after 1 hour the event must be triggered and the flow will go to the next step
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    waitForJobExecutorToProcessAllJobs(2000, 500);
    // expect to execute because the time is reached.

    List<Job> jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task C");

    // Test Timer Catch Intermediate Events after completing Task C
    taskService.complete(task.getId());
    nextTimeCal.add(Calendar.HOUR, 1); // after 1H 40 minutes from process start, the timer will trigger because of the endDate
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    waitForJobExecutorToProcessAllJobs(2000, 500);
    // expect to execute because the end time is reached.

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(historicInstance.getEndTime()).isNotNull();
    }

    // now All the process instances should be completed
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
    assertThat(processInstances).hasSize(0);

    // no jobs
    jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(0);

    // no tasks
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);

  }

}
