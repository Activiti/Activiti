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


package org.activiti.engine.test.bpmn.callactivity;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class CallActivityAdvancedTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml", "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testCallSimpleSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the
    // process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertThat(taskBeforeSubProcess.getName()).isEqualTo("Task before subprocess");

    // Completing the task continues the process which leads to calling the
    // subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocess");

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(processInstance.getId());

    // Validate subprocess history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      // Subprocess should have initial activity set
      HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery().processInstanceId(taskInSubProcess.getProcessInstanceId()).singleResult();
      assertThat(historicProcess).isNotNull();
      assertThat(historicProcess.getStartActivityId()).isEqualTo("theStart");

      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(taskInSubProcess.getProcessInstanceId()).list();

      // Should contain a start-event, the task and an end-event
      assertThat(historicInstances).hasSize(3);
      Set<String> expectedActivities = new HashSet<String>(asList("theStart", "task", "theEnd" ));

      for (HistoricActivityInstance act : historicInstances) {
        expectedActivities.remove(act.getActivityId());
      }
      assertThat(expectedActivities).as("Not all expected activities were found in the history").isEmpty();
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testCallSimpleSubProcessWithExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the
    // process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertThat(taskBeforeSubProcess.getName()).isEqualTo("Task before subprocess");

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a
    // variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocess");

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(processInstance.getId());
  }

  /**
   * Test case for a possible tricky case: reaching the end event of the subprocess leads to an end event in the super process instance.
   */
  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testSubProcessEndsSuperProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testSubProcessEndsSuperProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEndsSuperProcess");

    // one task in the subprocess should be active after starting the
    // process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertThat(taskBeforeSubProcess.getName()).isEqualTo("Task in subprocess");

    // Completing this task ends the subprocess which leads to the end of
    // the whole process instance
    taskService.complete(taskBeforeSubProcess.getId());
    assertProcessEnded(processInstance.getId());
    assertThat(runtimeService.createExecutionQuery().list()).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallParallelSubProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/simpleParallelSubProcess.bpmn20.xml" })
  public void testCallParallelSubProcess() {
    runtimeService.startProcessInstanceByKey("callParallelSubProcess");

    // The two tasks in the parallel subprocess should be active
    TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskName().asc();
    List<Task> tasks = taskQuery.list();
    assertThat(tasks).hasSize(2);

    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertThat(taskA.getName()).isEqualTo("Task A");
    assertThat(taskB.getName()).isEqualTo("Task B");

    // Completing the first task should not end the subprocess
    taskService.complete(taskA.getId());
    assertThat(taskQuery.list()).hasSize(1);

    // Completing the second task should end the subprocess and end the
    // whole process instance
    taskService.complete(taskB.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSequentialSubProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml", "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess2.bpmn20.xml" })
  public void testCallSequentialSubProcessWithExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSequentialSubProcess");

    // FIRST sub process calls simpleSubProcess

    // one task in the subprocess should be active after starting the
    // process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertThat(taskBeforeSubProcess.getName()).isEqualTo("Task before subprocess");

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a
    // variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocess");

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());

    // SECOND sub process calls simpleSubProcess2

    // one task in the subprocess should be active after starting the
    // process
    // instance
    taskQuery = taskService.createTaskQuery();
    taskBeforeSubProcess = taskQuery.singleResult();
    assertThat(taskBeforeSubProcess.getName()).isEqualTo("Task before subprocess");

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a
    // variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess2");
    taskService.complete(taskBeforeSubProcess.getId());
    taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess 2");

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    taskAfterSubProcess = taskQuery.singleResult();
    assertThat(taskAfterSubProcess.getName()).isEqualTo("Task after subprocess");

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testTimerOnCallActivity.bpmn20.xml", "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testTimerOnCallActivity() {
    Date startTime = processEngineConfiguration.getClock().getCurrentTime();

    // After process start, the task in the subprocess should be active
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("timerOnCallActivity");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertThat(taskInSubProcess.getName()).isEqualTo("Task in subprocess");

    ProcessInstance pi2 = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi1.getId()).singleResult();

    // When the timer on the subprocess is fired, the complete subprocess is destroyed
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (6 * 60 * 1000))); // + 6 minutes, timer fires on 5 minutes
    waitForJobExecutorToProcessAllJobs(10000, 5000L);

    Task escalatedTask = taskQuery.singleResult();
    assertThat(escalatedTask.getName()).isEqualTo("Escalated Task");

    // Completing the task ends the complete process
    taskService.complete(escalatedTask.getId());
    assertThat(runtimeService.createExecutionQuery().list()).hasSize(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(pi2.getId()).singleResult()
          .getDeleteReason()).startsWith(DeleteReason.BOUNDARY_EVENT_INTERRUPTING);
      assertHistoricTasksDeleteReason(pi2, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "Task in subprocess");
      assertHistoricActivitiesDeleteReason(pi1, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "callSubProcess");
      assertHistoricActivitiesDeleteReason(pi2, DeleteReason.BOUNDARY_EVENT_INTERRUPTING, "task");
    }
  }

  /**
   * Test case for deleting a sub process
   */
  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcesses.bpmn20.xml", "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testTwoSubProcesses() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callTwoSubProcesses");

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertThat(instanceList).isNotNull();
    assertThat(instanceList).hasSize(3);

    List<Task> taskList = taskService.createTaskQuery().list();
    assertThat(taskList).isNotNull();
    assertThat(taskList).hasSize(2);

    runtimeService.deleteProcessInstance(processInstance.getId(), "Test cascading");

    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertThat(instanceList).isNotNull();
    assertThat(instanceList).hasSize(0);

    taskList = taskService.createTaskQuery().list();
    assertThat(taskList).isNotNull();
    assertThat(taskList).hasSize(0);
  }
}
