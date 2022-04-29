/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

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


public class BoundaryTimerEventRepeatWithEndTest extends PluggableActivitiTestCase {

  @Deployment
  public void testRepeatWithEnd() throws Throwable {

    Calendar calendar = Calendar.getInstance();
    Date baseTime = calendar.getTime();

    calendar.add(Calendar.MINUTE, 20);
    // expect to stop boundary jobs after 20 minutes
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    DateTime dt = new DateTime(calendar.getTime());
    String dateStr = fmt.print(dt);

    // reset the timer
    Calendar nextTimeCal = Calendar.getInstance();
    nextTimeCal.setTime(baseTime);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("repeatWithEnd");

    runtimeService.setVariable(processInstance.getId(), "EndDateForBoundary", dateStr);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task A");

    // Test Boundary Events
    // complete will cause timer to be created
    taskService.complete(task.getId());

    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(1);

    // boundary events
    Job executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(executableJob.getId());

    assertThat(managementService.createJobQuery().list()).hasSize(0);
    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(1);

    nextTimeCal.add(Calendar.MINUTE, 15); // after 15 minutes
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(executableJob.getId());

    assertThat(managementService.createJobQuery().list()).hasSize(0);
    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(1);

    nextTimeCal.add(Calendar.MINUTE, 5); // after another 5 minutes (20 minutes and 1 second from the baseTime) the BoundaryEndTime is reached
    nextTimeCal.add(Calendar.SECOND, 1);
    processEngineConfiguration.getClock().setCurrentTime(nextTimeCal.getTime());

    executableJob = managementService.moveTimerToExecutableJob(jobs.get(0).getId());
    managementService.executeJob(executableJob.getId());

    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(0);
    jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    tasks = taskService.createTaskQuery().list();
    task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task B");
    assertThat(tasks).hasSize(1);
    taskService.complete(task.getId());

    jobs = managementService.createTimerJobQuery().list();
    assertThat(jobs).hasSize(0);
    jobs = managementService.createJobQuery().list();
    assertThat(jobs).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId())
          .singleResult();
      assertThat(historicInstance.getEndTime()).isNotNull();
    }

    // now all the process instances should be completed
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
