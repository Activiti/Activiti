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


package org.activiti.engine.test.bpmn.subprocess;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class SubProcessTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSimpleSubProcess() {

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  /**
   * Same test case as before, but now with all automatic steps
   */
  @Deployment
  public void testSimpleAutomaticSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessAutomatic");
    assertThat(pi.isEnded()).isTrue();
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testSimpleSubProcessWithTimer() {

    Date startTime = new Date();

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // Setting the clock forward 2 hours 1 second (timer fires in 2 hours) and fire up the job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000) + 1000));
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
    waitForJobExecutorToProcessAllJobs(5000L, 500L);

    // The subprocess should be left, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(escalationTask.getName()).isEqualTo("Fix escalated problem");

    // Verify history for task that was killed
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskName("Task in subprocess").singleResult();
      assertThat(historicTaskInstance.getEndTime()).isNotNull();

      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityId("subProcessTask").singleResult();
      assertThat(historicActivityInstance.getEndTime()).isNotNull();
    }
  }

  /**
   * A test case that has a timer attached to the subprocess, where 2 concurrent paths are defined when the timer fires.
   */
  @Deployment
  public void IGNORE_testSimpleSubProcessWithConcurrentTimer() {

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessWithConcurrentTimer");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc();

    Task subProcessTask = taskQuery.singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // When the timer is fired (after 2 hours), two concurrent paths should be created
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    List<Task> tasksAfterTimer = taskQuery.list();
    assertThat(tasksAfterTimer).hasSize(2);
    Task taskAfterTimer1 = tasksAfterTimer.get(0);
    Task taskAfterTimer2 = tasksAfterTimer.get(1);
    assertThat(taskAfterTimer1.getName()).isEqualTo("Task after timer 1");
    assertThat(taskAfterTimer2.getName()).isEqualTo("Task after timer 2");

    // Completing the two tasks should end the process instance
    taskService.complete(taskAfterTimer1.getId());
    taskService.complete(taskAfterTimer2.getId());
    assertProcessEnded(pi.getId());
  }

  /**
   * Test case where the simple sub process of previous test cases is nested within another subprocess.
   */
  @Deployment
  public void testNestedSimpleSubProcess() {

    // Start and delete a process with a nested subprocess when it is not yet ended
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess", singletonMap("someVar", "abc"));
    runtimeService.deleteProcessInstance(pi.getId(), "deleted");

    // After staring the process, the task in the inner subprocess must be active
    pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // After completing the task in the subprocess,
    // both subprocesses are destroyed and the task after the subprocess should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(taskAfterSubProcesses).isNotNull();
    assertThat(taskAfterSubProcesses.getName()).isEqualTo("Task after subprocesses");
    taskService.complete(taskAfterSubProcesses.getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testNestedSimpleSubprocessWithTimerOnInnerSubProcess() {
    Date startTime = new Date();

    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSubProcessWithTimer");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and
    // fire up the job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (60 * 60 * 1000) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);

    // The inner subprocess should be destroyed, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(escalationTask.getName()).isEqualTo("Escalated task");

    // Completing the escalated task, destroys the outer scope and activates
    // the task after the subprocess
    taskService.complete(escalationTask.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocesses");
  }

  /**
   * Test case where the simple sub process of previous test cases is nested within two other sub processes
   */
  @Deployment
  public void testDoubleNestedSimpleSubProcess() {
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // After completing the task in the subprocess,
    // both subprocesses are destroyed and the task after the subprocess
    // should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(taskAfterSubProcesses.getName()).isEqualTo("Task after subprocesses");
  }

  @Deployment
  public void testSimpleParallelSubProcess() {

    // After starting the process, the two task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleParallelSubProcess");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();

    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertThat(taskA.getName()).isEqualTo("Task A");
    assertThat(taskB.getName()).isEqualTo("Task B");

    // Completing both tasks, should destroy the subprocess and activate the
    // task after the subprocess
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after sub process");
  }

  @Deployment
  public void testSimpleParallelSubProcessWithTimer() {

    // After staring the process, the tasks in the subprocess should be active
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("simpleParallelSubProcessWithTimer");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();

    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertThat(taskA.getName()).isEqualTo("Task A");
    assertThat(taskB.getName()).isEqualTo("Task B");

    Job job = managementService.createTimerJobQuery().processInstanceId(processInstance.getId()).singleResult();

    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());

    // The inner subprocess should be destroyed, and the task after the timer  should be active
    Task taskAfterTimer = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(taskAfterTimer.getName()).isEqualTo("Task after timer");

    // Completing the task after the timer ends the process instance
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testTwoSubProcessInParallel() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallel");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    assertThat(tasks.get(0).getName()).isEqualTo("Task in subprocess A");
    assertThat(tasks.get(1).getName()).isEqualTo("Task in subprocess B");

    // Completing both tasks should active the tasks outside the
    // subprocesses
    taskService.complete(tasks.get(0).getId());

    tasks = taskQuery.list();
    assertThat(tasks.get(0).getName()).isEqualTo("Task after subprocess A");
    assertThat(tasks.get(1).getName()).isEqualTo("Task in subprocess B");

    taskService.complete(tasks.get(1).getId());

    tasks = taskQuery.list();

    assertThat(tasks.get(0).getName()).isEqualTo("Task after subprocess A");
    assertThat(tasks.get(1).getName()).isEqualTo("Task after subprocess B");

    // Completing these tasks should end the process
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertThat(taskA.getName()).isEqualTo("Task in subprocess A");
    assertThat(taskB.getName()).isEqualTo("Task in subprocess B");

    // Completing both tasks should active the tasks outside the subprocesses
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());

    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocess");

    // Completing this task should end the process
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testTwoNestedSubProcessesInParallelWithTimer() {

    // Date startTime = new Date();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedParallelSubProcessesWithTimer");
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc();
    List<Task> tasks = taskQuery.list();

    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertThat(taskA.getName()).isEqualTo("Task in subprocess A");
    assertThat(taskB.getName()).isEqualTo("Task in subprocess B");

    // Firing the timer should destroy all three subprocesses and activate the task after the timer
    // processEngineConfiguration.getClock().setCurrentTime(new
    // Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
    // waitForJobExecutorToProcessAllJobs(5000L, 50L);
    Job job = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(job.getId());
    managementService.executeJob(job.getId());

    Task taskAfterTimer = taskQuery.singleResult();
    assertThat(taskAfterTimer.getName()).isEqualTo("Task after timer");

    // Completing the task should end the process instance
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(pi.getId());
  }

  /**
   * @see <a href="https://activiti.atlassian.net/browse/ACT-1072">https://activiti.atlassian.net/browse/ACT-1072</a>
   */
  @Deployment
  public void testNestedSimpleSubProcessWithoutEndEvent() {
    testNestedSimpleSubProcess();
  }

  /**
   * @see <a href="https://activiti.atlassian.net/browse/ACT-1072">https://activiti.atlassian.net/browse/ACT-1072</a>
   */
  @Deployment
  public void testSimpleSubProcessWithoutEndEvent() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testSimpleSubProcessWithoutEndEvent");
    assertProcessEnded(pi.getId());
  }

  /**
   * @see <a href="https://activiti.atlassian.net/browse/ACT-1072">https://activiti.atlassian.net/browse/ACT-1072</a>
   */
  @Deployment
  public void testNestedSubProcessesWithoutEndEvents() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testNestedSubProcessesWithoutEndEvents");
    assertProcessEnded(pi.getId());
  }

  /**
   * @see <a href="https://activiti.atlassian.net/browse/ACT-1847">https://activiti.atlassian.net/browse/ACT-1847</a>
   */
  @Deployment
  public void testDataObjectScope() {

    // After staring the process, the task in the subprocess should be
    // active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("dataObjectScope");

    // get main process task
    Task currentTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    assertThat(currentTask.getName()).isEqualTo("Complete Task A");

    // verify main process scoped variables
    Map<String, Object> variables = runtimeService.getVariables(pi.getId());
    assertThat(variables).hasSize(2);
    Iterator<String> varNameIt = variables.keySet().iterator();
    while (varNameIt.hasNext()) {
      String varName = varNameIt.next();
      if ("StringTest123".equals(varName)) {
        assertThat(variables.get(varName)).isEqualTo("Testing123");
      } else if ("NoData123".equals(varName)) {
        assertThat(variables.get(varName)).isNull();
      } else {
        fail("Variable not expected " + varName);
      }
    }

    // After completing the task in the main process, the subprocess scope
    // initiates
    taskService.complete(currentTask.getId());

    // get subprocess task
    currentTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();

    assertThat(currentTask.getName()).isEqualTo("Complete SubTask");

    // verify current scoped variables - includes subprocess variables
    variables = runtimeService.getVariables(currentTask.getExecutionId());
    assertThat(variables).hasSize(3);

    varNameIt = variables.keySet().iterator();
    while (varNameIt.hasNext()) {
      String varName = varNameIt.next();
      if ("StringTest123".equals(varName)) {
        assertThat(variables.get(varName)).isEqualTo("Testing123");

      } else if ("StringTest456".equals(varName)) {
        assertThat(variables.get(varName)).isEqualTo("Testing456");

      } else if ("NoData123".equals(varName)) {
        assertThat(variables.get(varName)).isNull();
      } else {
        fail("Variable not expected " + varName);
      }
    }

    // After completing the task in the subprocess, the subprocess scope is destroyed and the main process continues
    taskService.complete(currentTask.getId());

    // verify main process scoped variables
    variables = runtimeService.getVariables(pi.getId());
    assertThat(variables).hasSize(2);
    varNameIt = variables.keySet().iterator();
    while (varNameIt.hasNext()) {
      String varName = varNameIt.next();
      if ("StringTest123".equals(varName)) {
        assertThat(variables.get(varName)).isEqualTo("Testing123");
      } else if ("NoData123".equals(varName)) {
        assertThat(variables.get(varName)).isNull();
      } else {
        fail("Variable not expected " + varName);
      }
    }

    currentTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    // Verify there are no local variables assigned to the current task. (subprocess variables are gone).
    variables = runtimeService.getVariablesLocal(currentTask.getExecutionId());
    assertThat(variables).hasSize(0);

    // After completing the final task in the main process,
    // the process scope is destroyed and the process ends
    currentTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(currentTask.getName()).isEqualTo("Complete Task B");

    taskService.complete(currentTask.getId());
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }
}
