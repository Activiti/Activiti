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

package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class IntermediateTimerEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testCatchingTimerEvent() throws Exception {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample");
    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertThat(jobQuery.count()).isEqualTo(1);

    // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L);

    assertThat(jobQuery.count()).isEqualTo(0);
    assertProcessEnded(pi.getProcessInstanceId());

  }

  @Deployment
  public void testTimerEventWithStartAndDuration() throws Exception {

    Calendar testStartCal = new GregorianCalendar(2016, 0, 1, 10, 0, 0);
    Date testStartTime = testStartCal.getTime();
    processEngineConfiguration.getClock().setCurrentTime(testStartTime);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("timerEventWithStartAndDuration");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task A");

    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertThat(jobQuery.count()).isEqualTo(0);

    Date startDate = new Date();
    runtimeService.setVariable(pi.getId(), "StartDate", startDate);
    taskService.complete(task.getId());

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertThat(jobQuery.count()).isEqualTo(1);

    processEngineConfiguration.getClock().setCurrentTime(new Date(startDate.getTime() + 7000L));

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertThat(jobQuery.count()).isEqualTo(1);
    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId()).executable();
    assertThat(jobQuery.count()).isEqualTo(0);

    processEngineConfiguration.getClock().setCurrentTime(new Date(startDate.getTime() + 11000L));
    waitForJobExecutorToProcessAllJobs(15000L);

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertThat(jobQuery.count()).isEqualTo(0);

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Task B");
    taskService.complete(task.getId());

    assertProcessEnded(pi.getProcessInstanceId());

    processEngineConfiguration.getClock().reset();
  }

  @Deployment
  public void testExpression() {
    // Set the clock fixed
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("dueDate", new Date());

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("dueDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));

    // After process start, there should be timer created
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables1);
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables2);

    assertThat(managementService.createTimerJobQuery().processInstanceId(pi1.getId()).count()).isEqualTo(1);
    assertThat(managementService.createTimerJobQuery().processInstanceId(pi2.getId()).count()).isEqualTo(1);

    // After setting the clock to one second in the future the timers should fire
    List<Job> jobs = managementService.createTimerJobQuery().executable().list();
    assertThat(jobs).hasSize(2);
    for (Job job : jobs) {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
    }

    assertThat(managementService.createTimerJobQuery().processInstanceId(pi1.getId()).count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().processInstanceId(pi2.getId()).count()).isEqualTo(0);

    assertProcessEnded(pi1.getProcessInstanceId());
    assertProcessEnded(pi2.getProcessInstanceId());
  }

  @Deployment
  public void testLoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLoop");

    // After looping 3 times, the process should end
    for (int i = 0; i < 3; i++) {
      Job timer = managementService.createTimerJobQuery().singleResult();
      managementService.moveTimerToExecutableJob(timer.getId());
      managementService.executeJob(timer.getId());
    }

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testLoopWithCycle() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLoop");

    // After looping 3 times, the process should end. Cycle should NOT repeat itself
    for (int i = 0; i < 3; i++) {
      Job timer = managementService.createTimerJobQuery().singleResult();
      managementService.moveTimerToExecutableJob(timer.getId());
      managementService.executeJob(timer.getId());
    }

    assertProcessEnded(processInstance.getId());
  }

}
