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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class BoundaryTimerEventTest extends PluggableActivitiTestCase {

  private static boolean listenerExecutedStartEvent;
  private static boolean listenerExecutedEndEvent;

  public static class MyExecutionListener implements ExecutionListener {
    private static final long serialVersionUID = 1L;

    public void notify(DelegateExecution execution) {
      if ("end".equals(execution.getEventName())) {
        listenerExecutedEndEvent = true;
      } else if ("start".equals(execution.getEventName())) {
        listenerExecutedStartEvent = true;
      }
    }
  }

  /*
   * Test for when multiple boundary timer events are defined on the same user task
   *
   * Configuration: - timer 1 -> 2 hours -> secondTask - timer 2 -> 1 hour -> thirdTask - timer 3 -> 3 hours -> fourthTask
   *
   * See process image next to the process xml resource
   */
  @Deployment
  public void testMultipleTimersOnUserTask() {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multipleTimersOnUserTask");
    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertThat(jobs).hasSize(3);

    // After setting the clock to time '1 hour and 5 seconds', the second
    // timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    assertThat(jobQuery.count()).isEqualTo(0L);

    // which means that the third task is reached
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Third Task");
  }

  @Deployment
  public void testTimerOnNestingOfSubprocesses() {

    Date testStartTime = processEngineConfiguration.getClock().getCurrentTime();

    runtimeService.startProcessInstanceByKey("timerOnNestedSubprocesses");
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("Inner subprocess task 1");
    assertThat(tasks.get(1).getName()).isEqualTo("Inner subprocess task 2");

    // Timer will fire in 2 hours
    processEngineConfiguration.getClock().setCurrentTime(new Date(testStartTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task outside subprocess");
  }

  @Deployment
  public void testExpressionOnTimer() {
    // Set the clock fixed
    Date startTime = new Date();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duration", "PT1H");

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertThat(jobs).hasSize(1);

    // After setting the clock to time '1 hour and 5 seconds', the second
    // timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    assertThat(jobQuery.count()).isEqualTo(0L);

    // start execution listener is not executed
    assertThat(listenerExecutedStartEvent).isFalse();
    assertThat(listenerExecutedEndEvent).isTrue();

    // which means the process has ended
    assertProcessEnded(pi.getId());
  }


  @Deployment
  public void testNullExpressionOnTimer(){

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duration", null);

    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testNullExpressionOnTimer", variables);

    //NO job scheduled as null expression set
    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertThat(jobs).hasSize(0);

    // which means the process is still running waiting for human task input.
    ProcessInstance processInstance = processEngine
    	      .getRuntimeService()
    	      .createProcessInstanceQuery()
    	      .processInstanceId(pi.getId())
    	      .singleResult();
    assertThat(processInstance).isNotNull();
  }


  @Deployment
  public void testTimerInSingleTransactionProcess() {
    // make sure that if a PI completes in single transaction, JobEntities
    // associated with the execution are deleted.
    // broken before 5.10, see ACT-1133
    runtimeService.startProcessInstanceByKey("timerOnSubprocesses");
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testRepeatingTimerWithCancelActivity() {
    runtimeService.startProcessInstanceByKey("repeatingTimerAndCallActivity");
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);

    // Firing job should cancel the user task, destroy the scope,
    // re-enter the task and recreate the task. A new timer should also be
    // created.
    // This didn't happen before 5.11 (new jobs kept being created). See
    // ACT-1427
    Job job = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
  }

  @Deployment
	public void testInfiniteRepeatingTimer() throws Exception {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
		Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
		processEngineConfiguration.getClock().setCurrentTime(currentTime);

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("timerString", "R/2015-10-01T11:00:00/PT24H");
		runtimeService.startProcessInstanceByKey("testTimerErrors", vars);

		long twentyFourHours = 24L * 60L * 60L * 1000L;

		Date previousDueDate = null;

		// Move clock, job should fire
		for (int i=0; i<30; i++) {
			Job job = managementService.createTimerJobQuery().singleResult();

			// Verify due date
			if (previousDueDate != null) {
				assertThat(job.getDuedate().getTime() - previousDueDate.getTime() >= twentyFourHours).isTrue();
			}
			previousDueDate = job.getDuedate();

			currentTime = new Date(currentTime.getTime() + twentyFourHours + (60 * 1000));
			processEngineConfiguration.getClock().setCurrentTime(currentTime);
			String jobId = managementService.createTimerJobQuery().singleResult().getId();
			managementService.moveTimerToExecutableJob(jobId);
			managementService.executeJob(jobId);
		}

	}

  @Deployment
  public void testRepeatTimerDuration() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("repeattimertest");

    long twentyFourHours = 24L * 60L * 60L * 1000L;

    Date previousDueDate = null;

    // Move clock, job should fire
    for (int i = 0; i < 3; i++) {
      Job job = managementService.createTimerJobQuery().singleResult();

      // Verify due date
      if (previousDueDate != null) {
        assertThat(job.getDuedate().getTime() - previousDueDate.getTime() >= twentyFourHours).isTrue();
      }
      previousDueDate = job.getDuedate();

      currentTime = new Date(currentTime.getTime() + twentyFourHours + (60 * 1000));
      processEngineConfiguration.getClock().setCurrentTime(currentTime);
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
    }

  }

  @Deployment
  public void testBoundaryTimerEvent() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("patient","kermit");
    runtimeService.startProcessInstanceByKey("process1", vars);

    // just wait for 2 seconds to run any job if it's the case
    try {
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    // there should be a userTask waiting for user input
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("First Task");
    List<Job> jobList = managementService.createTimerJobQuery().list();
    assertThat(jobList).hasSize(1);

    // let's see what's happening after 2 minutes
    // nothing should change since the timer have to executed after 10 minutes
    long twoMinutes = 2L * 60L * 1000L;

    currentTime = new Date(currentTime.getTime() + twoMinutes +  1000L);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("First Task");
    jobList = managementService.createTimerJobQuery().list();
    assertThat(jobList).hasSize(1);

    // after another 8 minutes (the timer will have to execute because it wasa set to be executed @ 10 minutes after process start)
    long tenMinutes = 8L * 60L * 1000L;
    currentTime = new Date(currentTime.getTime() + tenMinutes);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    } catch (Exception ex) {
      ex.getCause();
      //expected exception because a new job is prepared
    }

    // there should be only one userTask and it should be the one triggered by the boundary timer event.
    // after the boundary event is triggered there should be no active job.
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Second Task");
    jobList = managementService.createJobQuery().list();
    assertThat(jobList).hasSize(0);
    jobList = managementService.createTimerJobQuery().list();
    assertThat(jobList).hasSize(0);
  }


  @Deployment
  public void testBoundaryTimerEvent2() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("timerprocess");

    // just wait for 2 seconds to run any job if it's the case
    try {
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    // there should be a userTask waiting for user input
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Start");
    List<Job> jobList = managementService.createTimerJobQuery().list();
    assertThat(jobList).hasSize(1);

    // after another 2 minutes
    long tenMinutes = 2L * 60L * 1000L;
    currentTime = new Date(currentTime.getTime() + tenMinutes);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(2000, 200);
    } catch (Exception ex) {
      ex.getCause();
      //expected exception because a new job is prepared
    }

    // there should be no userTask
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(0);
    jobList = managementService.createJobQuery().list();
    assertThat(jobList).hasSize(0);
    jobList = managementService.createTimerJobQuery().list();
    assertThat(jobList).hasSize(0);
  }

}
