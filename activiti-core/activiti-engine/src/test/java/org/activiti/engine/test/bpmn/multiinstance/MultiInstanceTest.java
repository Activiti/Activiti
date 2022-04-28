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


package org.activiti.engine.test.bpmn.multiinstance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class MultiInstanceTest extends PluggableActivitiTestCase {

  public static final String NR_OF_INSTANCES_KEY = "nrOfInstances";
  public static final String NR_OF_ACTIVE_INSTANCES_KEY = "nrOfActiveInstances";
  public static final String NR_OF_COMPLETED_INSTANCES_KEY = "nrOfCompletedInstances";
  public static final String NR_OF_LOOPS_KEY = "nrOfLoops";
  public static final String LOOP_COUNTER_KEY = "loopCounter";

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml" })
  public void testSequentialUserTasks() {
    checkSequentialUserTasks("miSequentialUserTasks", LOOP_COUNTER_KEY);
  }

  @Deployment
  public void testSequentialUserTasksCustomExtensions() {
    checkSequentialUserTasks("miSequentialUserTasksCustomExtensions", "loopValueIndex");
  }

  private void checkSequentialUserTasks(String processDefinitionKey, String elementIndexVariable) {
    int nrOfLoops = 3;
    String procId = runtimeService.startProcessInstanceByKey(processDefinitionKey, singletonMap(NR_OF_LOOPS_KEY, nrOfLoops)).getId();

    Execution outerInstance = retrieveOuterExecution(procId);

    checkAndCompleteTask("kermit_0", 0, nrOfLoops, elementIndexVariable, outerInstance);
    checkAndCompleteTask("kermit_1", 1, nrOfLoops, elementIndexVariable, outerInstance);
    checkAndCompleteTask("kermit_2", 2, nrOfLoops, elementIndexVariable, outerInstance);
    assertThat(taskService.createTaskQuery().singleResult()).isNull();
    assertProcessEnded(procId);
  }

  private Execution retrieveOuterExecution(String procId) {
    List<Execution> executions = runtimeService.createExecutionQuery().parentId(procId).list();
    assertThat(executions).hasSize(1);
    Execution outerInstance = executions.get(0);
    assertThat(outerInstance.getActivityId()).isEqualTo("miTasks");
    return outerInstance;
  }

  private void checkAndCompleteTask(String expectedAssignee, int expectedLoopCounter, int nrOfLoops, String elementIndexVariable, Execution outerInstance) {
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("My Task");
    assertThat(task.getAssignee()).isEqualTo(expectedAssignee);

    checkInnerInstanceVariables(task, expectedLoopCounter, elementIndexVariable);
    checkOuterInstanceVariables(outerInstance, expectedLoopCounter, nrOfLoops, elementIndexVariable);

    taskService.complete(task.getId());
  }

  private void checkOuterInstanceVariables(Execution outerInstance, int loopCounter, int nrOfLoops, String elementIndexVariable) {
    Map<String, Object> localVariables = runtimeService.getVariablesLocal(outerInstance.getId());
    // this variable should be available only in the inner instance: see BPMN specification table 10.30, page 194
    assertThat(localVariables).doesNotContainKey(elementIndexVariable);

    assertThat(localVariables).containsKeys(NR_OF_INSTANCES_KEY, NR_OF_ACTIVE_INSTANCES_KEY, NR_OF_COMPLETED_INSTANCES_KEY);
    assertThat(localVariables.get(NR_OF_INSTANCES_KEY)).isEqualTo(nrOfLoops);
    assertThat(localVariables.get(NR_OF_ACTIVE_INSTANCES_KEY)).isEqualTo(1);
    assertThat(localVariables.get(NR_OF_COMPLETED_INSTANCES_KEY)).isEqualTo(loopCounter);
  }

  private void checkInnerInstanceVariables(Task task, int loopCounter, String elementIndexVariable) {
    Map<String, Object> localVariables = runtimeService.getVariablesLocal(task.getExecutionId());
    // these variables should be available only in the outer instance: see BPMN specification table 10.30, page 194
    assertThat(localVariables).doesNotContainKeys(NR_OF_INSTANCES_KEY, NR_OF_ACTIVE_INSTANCES_KEY, NR_OF_COMPLETED_INSTANCES_KEY);

    assertThat(localVariables).containsKey(elementIndexVariable);
    assertThat(localVariables.get(elementIndexVariable)).isEqualTo(loopCounter);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml" })
  public void testSequentialUserTasksHistory() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks", singletonMap(NR_OF_LOOPS_KEY, 4)).getId();
    for (int i = 0; i < 4; i++) {
      taskService.complete(taskService.createTaskQuery().singleResult().getId());
    }
    assertProcessEnded(procId);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertThat(historicTaskInstances).hasSize(4);
      for (HistoricTaskInstance ht : historicTaskInstances) {
        assertThat(ht.getAssignee()).isNotNull();
        assertThat(ht.getStartTime()).isNotNull();
        assertThat(ht.getEndTime()).isNotNull();
      }

      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertThat(historicActivityInstances).hasSize(4);
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertThat(hai.getActivityId()).isNotNull();
        assertThat(hai.getActivityName()).isNotNull();
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
        assertThat(hai.getAssignee()).isNotNull();
      }

    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml" })
  public void testSequentialUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks", singletonMap(NR_OF_LOOPS_KEY, 3)).getId();

    // Complete 1 tasks
    taskService.complete(taskService.createTaskQuery().singleResult().getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.sequentialUserTasks.bpmn20.xml" })
  public void testSequentialUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks", singletonMap(NR_OF_LOOPS_KEY, 10)).getId();

    // 10 tasks are to be created, but completionCondition stops them at 5
    for (int i = 0; i < 5; i++) {
      Task task = taskService.createTaskQuery().singleResult();
      taskService.complete(task.getId());
    }
    assertThat(taskService.createTaskQuery().singleResult()).isNull();
    assertProcessEnded(procId);
  }

  @Deployment
  public void testNestedSequentialUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialUserTasks").getId();

    for (int i = 0; i < 3; i++) {
      Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
      assertThat(task.getName()).isEqualTo("My Task");
      taskService.complete(task.getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasks").getId();

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(3);
    assertThat(tasks.get(0).getName()).isEqualTo("My Task 0");
    assertThat(tasks.get(1).getName()).isEqualTo("My Task 1");
    assertThat(tasks.get(2).getName()).isEqualTo("My Task 2");

    checkInnerInstanceVariables(tasks.get(0), 0, LOOP_COUNTER_KEY);
    checkInnerInstanceVariables(tasks.get(1), 1, LOOP_COUNTER_KEY);
    checkInnerInstanceVariables(tasks.get(2), 2, LOOP_COUNTER_KEY);

    Execution outerExecution = retrieveOuterExecution(procId);

    checkBuiltInOuterVariables(outerExecution, 3, 0);
    taskService.complete(tasks.get(0).getId());

    checkBuiltInOuterVariables(outerExecution, 2, 1);
    taskService.complete(tasks.get(1).getId());

    checkBuiltInOuterVariables(outerExecution, 1, 2);
    taskService.complete(tasks.get(2).getId());

    assertProcessEnded(procId);
  }

  private void checkBuiltInOuterVariables(Execution outerExecution, int expetedActiveNumber, int expectedCompletedNumber) {
    Map<String, Object> variables = runtimeService.getVariablesLocal(outerExecution.getId());
    assertThat(variables).containsEntry(NR_OF_INSTANCES_KEY, 3);
    assertThat(variables).containsEntry(NR_OF_ACTIVE_INSTANCES_KEY, expetedActiveNumber);
    assertThat(variables).containsEntry(NR_OF_COMPLETED_INSTANCES_KEY, expectedCompletedNumber);
    assertThat(variables).doesNotContainKey(LOOP_COUNTER_KEY);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelUserTasks.bpmn20.xml" })
  public void testParallelUserTasksHistory() {
    runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
      for (int i = 0; i < historicTaskInstances.size(); i++) {
        HistoricTaskInstance hi = historicTaskInstances.get(i);
        assertThat(hi.getStartTime()).isNotNull();
        assertThat(hi.getEndTime()).isNotNull();
        assertThat(hi.getName()).isEqualTo("My Task " + i);
        assertThat(hi.getAssignee()).isEqualTo("kermit_" + i);
      }

      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertThat(historicActivityInstances).hasSize(3);
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
        assertThat(hai.getAssignee()).isNotNull();
        assertThat(hai.getActivityType()).isEqualTo("userTask");
      }
    }
  }

  @Deployment
  public void testParallelUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksWithTimer").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelUserTasksCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksCompletionCondition").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(5);

    // Completing 3 tasks gives 50% of tasks completed, which triggers
    // completionCondition
    for (int i = 0; i < 3; i++) {
      assertThat(taskService.createTaskQuery().count()).isEqualTo(5 - i);
      taskService.complete(tasks.get(i).getId());
    }
    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelUserTasksBasedOnCollection() {
    List<String> assigneeList = asList("kermit", "gonzo", "mispiggy", "fozzie", "bubba");
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksBasedOnCollection", singletonMap("assigneeList", assigneeList)).getId();

    List<Task> tasks = taskService.createTaskQuery().orderByTaskAssignee().asc().list();
    assertThat(tasks).hasSize(5);
    assertThat(tasks.get(0).getAssignee()).isEqualTo("bubba");
    assertThat(tasks.get(1).getAssignee()).isEqualTo("fozzie");
    assertThat(tasks.get(2).getAssignee()).isEqualTo("gonzo");
    assertThat(tasks.get(3).getAssignee()).isEqualTo("kermit");
    assertThat(tasks.get(4).getAssignee()).isEqualTo("mispiggy");

    // Completing 3 tasks will trigger completioncondition
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelUserTasksCustomExtensions() {
    checkParallelUserTasksCustomExtensions("miParallelUserTasks");
  }

  @Deployment
  public void testParallelUserTasksCustomExtensionsLoopIndexVariable() {
    checkParallelUserTasksCustomExtensions("miParallelUserTasksLoopVariable");
  }

  private void checkParallelUserTasksCustomExtensions(String processDefinitionKey) {
    Map<String, Object> vars = new HashMap<String, Object>();
    List<String> assigneeList = asList("kermit", "gonzo", "fozzie");
    vars.put("assigneeList", assigneeList);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, vars);

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(3);
    assertThat(tasks.get(0).getName()).isEqualTo("My Task 0");
    assertThat(tasks.get(1).getName()).isEqualTo("My Task 1");
    assertThat(tasks.get(2).getName()).isEqualTo("My Task 2");

    tasks = taskService.createTaskQuery().orderByTaskAssignee().asc().list();
    assertThat(tasks.get(0).getAssignee()).isEqualTo("fozzie");
    assertThat(tasks.get(1).getAssignee()).isEqualTo("gonzo");
    assertThat(tasks.get(2).getAssignee()).isEqualTo("kermit");

    // Completing 3 tasks will trigger completion condition
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    assertProcessEnded(processInstance.getProcessInstanceId());
  }

  @Deployment
  public void testParallelUserTasksExecutionAndTaskListeners() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelUserTasks");
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    Execution waitState = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertThat(waitState).isNotNull();

    assertThat(runtimeService.getVariable(processInstance.getId(), "taskListenerCounter")).isEqualTo(3);
    assertThat(runtimeService.getVariable(processInstance.getId(), "executionListenerCounter")).isEqualTo(3);

    runtimeService.trigger(waitState.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testNestedParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelUserTasks").getId();

    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    for (Task task : tasks) {
      assertThat(task.getName()).isEqualTo("My Task");
      taskService.complete(task.getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment
  public void testSequentialScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put(NR_OF_LOOPS_KEY, 5);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);
    int sum = (Integer) runtimeService.getVariable(processInstance.getId(), "sum");
    assertThat(sum).isEqualTo(10);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialScriptTasks.bpmn20.xml" })
  public void testSequentialScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put(NR_OF_LOOPS_KEY, 7);
    runtimeService.startProcessInstanceByKey("miSequentialScriptTask", vars);

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").orderByActivityId().asc().list();
      assertThat(historicInstances).hasSize(7);
      for (int i = 0; i < 7; i++) {
        HistoricActivityInstance hai = historicInstances.get(i);
        assertThat(hai.getActivityType()).isEqualTo("scriptTask");
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }
    }
  }

  @Deployment
  public void testSequentialScriptTasksCompletionCondition() {
    runtimeService.startProcessInstanceByKey("miSequentialScriptTaskCompletionCondition").getId();
    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions).hasSize(2);
    Execution processInstanceExecution = null;
    Execution waitStateExecution = null;
    for (Execution execution : executions) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        processInstanceExecution = execution;
      } else {
        waitStateExecution = execution;
      }
    }
    assertThat(processInstanceExecution).isNotNull();
    assertThat(waitStateExecution).isNotNull();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertThat(sum).isEqualTo(5);
  }

  @Deployment
  public void testParallelScriptTasks() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put(NR_OF_LOOPS_KEY, 10);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);
    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions).hasSize(2);
    Execution processInstanceExecution = null;
    Execution waitStateExecution = null;
    for (Execution execution : executions) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        processInstanceExecution = execution;
      } else {
        waitStateExecution = execution;
      }
    }
    assertThat(processInstanceExecution).isNotNull();
    assertThat(waitStateExecution).isNotNull();
    int sum = (Integer) runtimeService.getVariable(waitStateExecution.getId(), "sum");
    assertThat(sum).isEqualTo(45);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasks.bpmn20.xml" })
  public void testParallelScriptTasksHistory() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("sum", 0);
    vars.put(NR_OF_LOOPS_KEY, 4);
    runtimeService.startProcessInstanceByKey("miParallelScriptTask", vars);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertThat(historicActivityInstances).hasSize(4);
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }
    }
  }

  @Deployment
  public void testParallelScriptTasksCompletionCondition() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    Execution waitStateExecution = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertThat(waitStateExecution).isNotNull();
    int sum = (Integer) runtimeService.getVariable(processInstance.getId(), "sum");
    assertThat(sum).isEqualTo(2);
    runtimeService.trigger(waitStateExecution.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelScriptTasksCompletionCondition.bpmn20.xml" })
  public void testParallelScriptTasksCompletionConditionHistory() {
    runtimeService.startProcessInstanceByKey("miParallelScriptTaskCompletionCondition");
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("scriptTask").list();
      assertThat(historicActivityInstances).hasSize(2);
    }
  }

  @Deployment
  public void testSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocess").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i = 0; i < 4; i++) {
      List<Task> tasks = query.list();
      assertThat(tasks).hasSize(2);

      assertThat(tasks.get(0).getName()).isEqualTo("task one");
      assertThat(tasks.get(1).getName()).isEqualTo("task two");

      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());

      if (i != 3) {
        List<String> activities = runtimeService.getActiveActivityIds(procId);
        assertThat(activities).isNotNull();
        assertThat(activities).hasSize(3);
      }
    }

    assertProcessEnded(procId);
  }

  @Deployment
  public void testSequentialSubProcessEndEvent() {
    // ACT-1185: end-event in subprocess causes inactivated execution
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocess").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i = 0; i < 4; i++) {
      List<Task> tasks = query.list();
      assertThat(tasks).hasSize(1);

      assertThat(tasks.get(0).getName()).isEqualTo("task one");

      taskService.complete(tasks.get(0).getId());

      // Last run, the execution no longer exists
      if (i != 3) {
        List<String> activities = runtimeService.getActiveActivityIds(procId);
        assertThat(activities).isNotNull();
        assertThat(activities).hasSize(2);
      }
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialSubProcess.bpmn20.xml" })
  public void testSequentialSubProcessHistory() {
    runtimeService.startProcessInstanceByKey("miSequentialSubprocess");
    for (int i = 0; i < 4; i++) {
      List<Task> tasks = taskService.createTaskQuery().list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> onlySubProcessInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertThat(onlySubProcessInstances).hasSize(4);

      List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("subProcess").list();
      assertThat(historicInstances).hasSize(4);
      for (HistoricActivityInstance hai : historicInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }

      historicInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask").list();
      assertThat(historicInstances).hasSize(8);
      for (HistoricActivityInstance hai : historicInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }
    }
  }

  @Deployment
  public void testSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessWithTimer").getId();

    // Complete one subprocess
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment
  public void testSequentialSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialSubprocessCompletionCondition").getId();

    TaskQuery query = taskService.createTaskQuery().orderByTaskName().asc();
    for (int i = 0; i < 3; i++) {
      List<Task> tasks = query.list();
      assertThat(tasks).hasSize(2);

      assertThat(tasks.get(0).getName()).isEqualTo("task one");
      assertThat(tasks.get(1).getName()).isEqualTo("task two");

      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment
  public void testNestedSequentialSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcess").getId();

    for (int i = 0; i < 3; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment
  public void testNestedSequentialSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialSubProcessWithTimer").getId();

    for (int i = 0; i < 2; i++) {
      List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    // Complete one task, to make it a bit more trickier
    List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocess").getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(4);

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcess.bpmn20.xml" })
  public void testParallelSubProcessHistory() {
    runtimeService.startProcessInstanceByKey("miParallelSubprocess");
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // Validate history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("miSubProcess").list();
      assertThat(historicActivityInstances).hasSize(2);
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }
    }
  }

  @Deployment
  public void testParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessWithTimer").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(6);

    // Complete two tasks
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelSubProcessCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessCompletionCondition").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(4);

    List<Task> subProcessTasks1 = taskService.createTaskQuery().taskDefinitionKey("subProcessTask1").list();
    assertThat(subProcessTasks1).hasSize(2);

    List<Task> subProcessTasks2 = taskService.createTaskQuery().taskDefinitionKey("subProcessTask2").list();
    assertThat(subProcessTasks2).hasSize(2);

    Execution taskExecution = runtimeService.createExecutionQuery().executionId(subProcessTasks1.get(0).getExecutionId()).singleResult();
    String parentExecutionId = taskExecution.getParentId();

    Task subProcessTask2 = null;
    for (Task task : subProcessTasks2) {
      Execution toFindExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
      if (toFindExecution.getParentId().equals(parentExecutionId)) {
        subProcessTask2 = task;
        break;
      }
    }

    assertThat(subProcessTask2).isNotNull();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(subProcessTask2.getId());

    assertProcessEnded(procId);
  }

  @Deployment
  public void testParallelSubProcessAllAutomatic() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics", singletonMap(NR_OF_LOOPS_KEY, 5)).getId();

    for (int i = 0; i < 5; i++) {
      List<Execution> waitSubExecutions = runtimeService.createExecutionQuery().activityId("subProcessWait").list();
      assertThat(waitSubExecutions.size() > 0).isTrue();
      runtimeService.trigger(waitSubExecutions.get(0).getId());
    }

    List<Execution> waitSubExecutions = runtimeService.createExecutionQuery().activityId("subProcessWait").list();
    assertThat(waitSubExecutions).hasSize(0);

    Execution waitState = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertThat(runtimeService.getVariable(waitState.getId(), "sum")).isEqualTo(10);

    runtimeService.trigger(waitState.getId());
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelSubProcessAllAutomatic.bpmn20.xml" })
  public void testParallelSubProcessAllAutomaticCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelSubprocessAllAutomatics", singletonMap(NR_OF_LOOPS_KEY, 10)).getId();

    for (int i = 0; i < 6; i++) {
      List<Execution> waitSubExecutions = runtimeService.createExecutionQuery().activityId("subProcessWait").list();
      assertThat(waitSubExecutions.size() > 0).isTrue();
      runtimeService.trigger(waitSubExecutions.get(0).getId());
    }

    List<Execution> waitSubExecutions = runtimeService.createExecutionQuery().activityId("subProcessWait").list();
    assertThat(waitSubExecutions).hasSize(0);

    Execution waitState = runtimeService.createExecutionQuery().activityId("waitState").singleResult();
    assertThat(runtimeService.getVariable(procId, "sum")).isEqualTo(12);

    runtimeService.trigger(waitState.getId());
    assertProcessEnded(procId);
  }

  @Deployment
  public void testNestedParallelSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(8);

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }
    assertProcessEnded(procId);
  }

  @Deployment
  public void testNestedParallelSubProcessWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelSubProcess").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(12);

    for (int i = 0; i < 3; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivity").getId();

    for (int i = 0; i < 3; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertThat(tasks).hasSize(2);
      assertThat(tasks.get(0).getName()).isEqualTo("task one");
      assertThat(tasks.get(1).getName()).isEqualTo("task two");
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivityWithList.bpmn20.xml")
  public void testSequentialCallActivityWithList() {
      ArrayList<String> list = new ArrayList<String>();
      list.add("one");
      list.add("two");

      HashMap<String, Object> variables = new HashMap<String, Object>();
      variables.put("list", list);

      String procId = runtimeService.startProcessInstanceByKey("parentProcess", variables).getId();

      Task task1 = taskService.createTaskQuery().processVariableValueEquals("element", "one").singleResult();
      Task task2 = taskService.createTaskQuery().processVariableValueEquals("element", "two").singleResult();

      assertThat(task1).isNotNull();
      assertThat(task2).isNotNull();

      HashMap<String, Object> subVariables = new HashMap<String, Object>();
      subVariables.put("x", "y");

      taskService.complete(task1.getId(), subVariables);
      taskService.complete(task2.getId(), subVariables);

      Task task3 = taskService.createTaskQuery().processDefinitionKey("midProcess").singleResult();
      assertThat(task3).isNotNull();
      taskService.complete(task3.getId(), null);

      Task task4 = taskService.createTaskQuery().processDefinitionKey("parentProcess").singleResult();
      assertThat(task4).isNotNull();
      taskService.complete(task4.getId(), null);

      assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialCallActivityWithTimer").getId();

    // Complete first subprocess
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("task one");
    assertThat(tasks.get(1).getName()).isEqualTo("task two");
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(12);
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivityHistory() {
    runtimeService.startProcessInstanceByKey("miParallelCallActivity");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(12);
    for (int i = 0; i < tasks.size(); i++) {
      taskService.complete(tasks.get(i).getId());
    }

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      // Validate historic processes
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
      assertThat(historicProcessInstances).hasSize(7); // 6 subprocesses
                                                        // + main process
      for (HistoricProcessInstance hpi : historicProcessInstances) {
        assertThat(hpi.getStartTime()).isNotNull();
        assertThat(hpi.getEndTime()).isNotNull();
      }

      // Validate historic tasks
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().list();
      assertThat(historicTaskInstances).hasSize(12);
      for (HistoricTaskInstance hti : historicTaskInstances) {
        assertThat(hti.getStartTime()).isNotNull();
        assertThat(hti.getEndTime()).isNotNull();
        assertThat(hti.getAssignee()).isNotNull();
        assertThat(hti.getDeleteReason()).isNull();
      }

      // Validate historic activities
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("callActivity").list();
      assertThat(historicActivityInstances).hasSize(6);
      for (HistoricActivityInstance hai : historicActivityInstances) {
        assertThat(hai.getStartTime()).isNotNull();
        assertThat(hai.getEndTime()).isNotNull();
      }
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelCallActivity").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(6);
    for (int i = 0; i < 2; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedSequentialCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivity").getId();

    for (int i = 0; i < 4; i++) {
      List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
      assertThat(tasks).hasSize(2);
      assertThat(tasks.get(0).getName()).isEqualTo("task one");
      assertThat(tasks.get(1).getName()).isEqualTo("task two");
      taskService.complete(tasks.get(0).getId());
      taskService.complete(tasks.get(1).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedSequentialCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedSequentialCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedSequentialCallActivityWithTimer").getId();

    // first instance
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("task one");
    assertThat(tasks.get(1).getName()).isEqualTo("task two");
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // one task of second instance
    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);
    taskService.complete(tasks.get(0).getId());

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivity").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(14);
    for (int i = 0; i < 14; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityWithTimer.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivityWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityWithTimer").getId();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(4);
    for (int i = 0; i < 3; i++) {
      taskService.complete(tasks.get(i).getId());
    }

    // Fire timer
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterTimer.getTaskDefinitionKey()).isEqualTo("taskAfterTimer");
    taskService.complete(taskAfterTimer.getId());

    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedParallelCallActivityCompletionCondition.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.externalSubProcess.bpmn20.xml" })
  public void testNestedParallelCallActivityCompletionCondition() {
    String procId = runtimeService.startProcessInstanceByKey("miNestedParallelCallActivityCompletionCondition").getId();

    assertThat(taskService.createTaskQuery().count()).isEqualTo(8);

    for (int i = 0; i < 2; i++) {
      ProcessInstance nextSubProcessInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("externalSubProcess").listPage(0, 1).get(0);
      List<Task> tasks = taskService.createTaskQuery().processInstanceId(nextSubProcessInstance.getId()).list();
      assertThat(tasks).hasSize(2);
      for (Task task : tasks) {
        taskService.complete(task.getId());
      }
    }

    assertProcessEnded(procId);
  }

  // ACT-764
  @Deployment
  public void testSequentialServiceTaskWithClass() {
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", singletonMap("result", 5));
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertThat(result.intValue()).isEqualTo(160);

    Execution waitExecution = runtimeService.createExecutionQuery().processInstanceId(procInst.getId()).activityId("wait").singleResult();
    runtimeService.trigger(waitExecution.getId());
    assertProcessEnded(procInst.getId());
  }

  @Deployment
  public void testSequentialServiceTaskWithClassAndCollection() {
    Collection<Integer> items = asList(1, 2, 3, 4, 5, 6);
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("result", 1);
    vars.put("items", items);

    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("multiInstanceServiceTask", vars);
    Integer result = (Integer) runtimeService.getVariable(procInst.getId(), "result");
    assertThat(result.intValue()).isEqualTo(720);

    Execution waitExecution = runtimeService.createExecutionQuery().processInstanceId(procInst.getId()).activityId("wait").singleResult();
    runtimeService.trigger(waitExecution.getId());
    assertProcessEnded(procInst.getId());
  }

  // ACT-901
  @Deployment
  public void testAct901() {

    Date startTime = processEngineConfiguration.getClock().getCurrentTime();

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multiInstanceSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();

    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + 61000L)); // timer is set to one minute
    List<Job> timers = managementService.createTimerJobQuery().list();
    assertThat(timers).hasSize(5);

    // Execute all timers one by one (single thread vs thread pool of job
    // executor, which leads to optimisticlockingexceptions!)
    for (Job timer : timers) {
      managementService.moveTimerToExecutableJob(timer.getId());
      managementService.executeJob(timer.getId());
    }

    // All tasks should be canceled
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();
    assertThat(tasks).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.callActivityWithBoundaryErrorEvent.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.throwingErrorEventSubProcess.bpmn20.xml" })
  public void testMultiInstanceCallActivityWithErrorBoundaryEvent() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("assignees", asList("kermit", "gonzo"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(2);

    // finish first call activity with error
    variableMap = new HashMap<String, Object>();
    variableMap.put("done", false);
    taskService.complete(tasks.get(0).getId(), variableMap);

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    taskService.complete(tasks.get(0).getId());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").list();
    assertThat(processInstances).hasSize(0);
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.callActivityWithBoundaryErrorEventSequential.bpmn20.xml",
      "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.throwingErrorEventSubProcess.bpmn20.xml" })
  public void testSequentialMultiInstanceCallActivityWithErrorBoundaryEvent() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("assignees", asList("kermit", "gonzo"));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    // finish first call activity with error
    variableMap = new HashMap<String, Object>();
    variableMap.put("done", false);
    taskService.complete(tasks.get(0).getId(), variableMap);

    tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(1);

    taskService.complete(tasks.get(0).getId());

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").list();
    assertThat(processInstances).hasSize(0);
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testMultiInstanceParallelReceiveTask() {
    runtimeService.startProcessInstanceByKey("multi-instance-receive");
    List<Execution> executions = runtimeService.createExecutionQuery().activityId("theReceiveTask").list();
    assertThat(executions).hasSize(4);

    // Complete all four of the executions
    for (Execution execution : executions) {
      runtimeService.trigger(execution.getId());
    }

    // There is one task after the task
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testMultiInstanceParalelReceiveTaskWithTimer() {
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    runtimeService.startProcessInstanceByKey("multiInstanceReceiveWithTimer");
    List<Execution> executions = runtimeService.createExecutionQuery().activityId("theReceiveTask").list();
    assertThat(executions).hasSize(3);

    // Signal only one execution. Then the timer will fire
    runtimeService.trigger(executions.get(1).getId());
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + 60000L));
    waitForJobExecutorToProcessAllJobs(10000L);

    // The process should now be in the task after the timer
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task after timer");

    // Completing it should end the process
    taskService.complete(task.getId());
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testMultiInstanceSequentialReceiveTask() {
    runtimeService.startProcessInstanceByKey("multi-instance-receive");
    Execution execution = runtimeService.createExecutionQuery().activityId("theReceiveTask").singleResult();
    assertThat(execution).isNotNull();

    // Complete all four of the executions
    while (execution != null) {
      runtimeService.trigger(execution.getId());
      execution = runtimeService.createExecutionQuery().activityId("theReceiveTask").singleResult();
    }

    // There is one task after the task
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testNestedMultiInstanceTasks.bpmn20.xml" })
  public void testNestedMultiInstanceTasks() {
    List<String> processes = asList("process A", "process B");
    List<String> assignees = asList("kermit", "gonzo");
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("subProcesses", processes);
    variableMap.put("assignees", assignees);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miNestedMultiInstanceTasks", variableMap);

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(processes.size() * assignees.size());

    for (Task t : tasks) {
      taskService.complete(t.getId());
    }

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("miNestedMultiInstanceTasks").list();
    assertThat(processInstances).hasSize(0);
    assertProcessEnded(processInstance.getId());
  }


  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialSubprocessEmptyCollection.bpmn20.xml" })
  public void testSequentialSubprocessEmptyCollection() {
	Collection<String> collection = emptyList();
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("collection", collection);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSequentialSubProcessEmptyCollection", variableMap);
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNull();
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialEmptyCollection.bpmn20.xml" })
  public void testSequentialEmptyCollection() {
    Collection<String> collection = emptyList();
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("collection", collection);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSequentialEmptyCollection", variableMap);
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNull();
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testSequentialEmptyCollection.bpmn20.xml" })
  public void testSequentialEmptyCollectionWithNonEmptyCollection() {
    Collection<String> collection = singleton("Test");
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("collection", collection);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSequentialEmptyCollection", variableMap);
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelEmptyCollection.bpmn20.xml" })
  public void testParalellEmptyCollection() throws Exception {
    Collection<String> collection = emptyList();
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("collection", collection);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testParalellEmptyCollection", variableMap);
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNull();
    assertProcessEnded(processInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/multiinstance/MultiInstanceTest.testParallelEmptyCollection.bpmn20.xml" })
  public void testParalellEmptyCollectionWithNonEmptyCollection() {
    Collection<String> collection = singleton("Test");
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("collection", collection);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testParalellEmptyCollection", variableMap);
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());
    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testInfiniteLoopWithDelegateExpressionFix() {

    // Add bean temporary to process engine

    Map<Object, Object> originalBeans = processEngineConfiguration.getExpressionManager().getBeans();

    try {

      Map<Object, Object> newBeans = new HashMap<Object, Object>();
      newBeans.put("SampleTask", new TestSampleServiceTask());
      processEngineConfiguration.getExpressionManager().setBeans(newBeans);

      Map<String, Object> params = new HashMap<String, Object>();
      params.put("sampleValues", asList("eins", "zwei", "drei"));
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("infiniteLoopTest", params);
      assertThat(processInstance).isNotNull();

    } finally {

      // Put beans back
      processEngineConfiguration.getExpressionManager().setBeans(originalBeans);

    }
  }

  @Deployment
  public void testEmptyCollectionOnParallelUserTask() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      Map<String, Object> vars = new HashMap<>();
      vars.put("messages", emptyList());
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelUserTaskMi", vars);

      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).finished().count()).isEqualTo(1L);
    }
  }

  @Deployment
  public void testZeroLoopCardinalityOnParallelUserTask() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("parallelUserTaskMi");
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).finished().count()).isEqualTo(1L);
    }
  }

  @Deployment
  public void testEmptyCollectionOnSequentialEmbeddedSubprocess() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      Map<String, Object> vars = new HashMap<>();
      vars.put("messages", emptyList());
      runtimeService.startProcessInstanceByKey("sequentialMiSubprocess", vars);

      assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(1L);
    }
  }

  @Deployment
  public void testEmptyCollectionOnParallelEmbeddedSubprocess() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      Map<String, Object> vars = new HashMap<>();
      vars.put("messages", emptyList());
      runtimeService.startProcessInstanceByKey("parallelMiSubprocess", vars);

      assertThat(historyService.createHistoricProcessInstanceQuery().finished().count()).isEqualTo(1L);
    }
  }

  @Deployment
  public void testExecutionListenersOnMultiInstanceSubprocess() {
  	resetTestCounts();
		Map<String, Object> variableMap = new HashMap<String, Object>();
		List<String> assignees = new ArrayList<String>();
		assignees.add("john");
		assignees.add("jane");
		assignees.add("matt");
		variableMap.put("assignees", assignees );
		runtimeService.startProcessInstanceByKey("MultiInstanceTest", variableMap);

		assertThat(TestStartExecutionListener.countWithLoopCounter.get()).isEqualTo(3);
		assertThat(TestEndExecutionListener.countWithLoopCounter.get()).isEqualTo(3);

		assertThat(TestStartExecutionListener.countWithoutLoopCounter.get()).isEqualTo(1);
		assertThat(TestEndExecutionListener.countWithoutLoopCounter.get()).isEqualTo(1);
  }


  @Deployment
  public void testExecutionListenersOnMultiInstanceUserTask() {
  	resetTestCounts();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionListenersOnMultiInstanceUserTask");

		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		for (Task task : tasks) {
			taskService.complete(task.getId());
		}

		assertThat(TestTaskCompletionListener.count.get()).isEqualTo(4);

		assertThat(TestStartExecutionListener.countWithLoopCounter.get()).isEqualTo(4);
		assertThat(TestEndExecutionListener.countWithLoopCounter.get()).isEqualTo(4);

		assertThat(TestStartExecutionListener.countWithoutLoopCounter.get()).isEqualTo(1);
		assertThat(TestEndExecutionListener.countWithoutLoopCounter.get()).isEqualTo(1);
  }

  @Deployment
  public void testParallelAfterSequentialMultiInstance() {

    // Used to throw a nullpointer exception

    runtimeService.startProcessInstanceByKey("multiInstance");
    assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testEndTimeOnMiSubprocess() {

    if (!processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      return;
    }

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multiInstanceSubProcessParallelTasks");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("User Task 1");
    assertThat(tasks.get(1).getName()).isEqualTo("User Task 1");

    // End time should not be set for the subprocess
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("subprocess1").list();
    assertThat(historicActivityInstances).hasSize(2);
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      assertThat(historicActivityInstance.getStartTime()).isNotNull();
      assertThat(historicActivityInstance.getEndTime()).isNull();
    }

    // Complete one of the user tasks. This should not trigger setting of end time of the subprocess, but due to a bug it did exactly that
    taskService.complete(tasks.get(0).getId());
    historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("subprocess1").list();
    assertThat(historicActivityInstances).hasSize(2);
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      assertThat(historicActivityInstance.getEndTime()).isNull();
    }

    taskService.complete(tasks.get(1).getId());
    historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("subprocess1").list();
    assertThat(historicActivityInstances).hasSize(2);
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      assertThat(historicActivityInstance.getEndTime()).isNull();
    }

    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("User Task 3").list();
    assertThat(tasks).hasSize(2);
    for (Task task : tasks) {
      taskService.complete(task.getId());
      historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("subprocess1").list();
      assertThat(historicActivityInstances).hasSize(2);
      for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
        assertThat(historicActivityInstance.getEndTime()).isNull();
      }
    }

    // Finishing the tasks should also set the end time
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertThat(tasks).hasSize(2);
    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityId("subprocess1").list();
    assertThat(historicActivityInstances).hasSize(2);
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      assertThat(historicActivityInstance.getEndTime()).isNotNull();
    }
  }

  @Deployment
  public void testChangingCollection() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("multi_users", asList("testuser"));
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("test_multi", vars);
    assertThat(instance).isNotNull();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("multi");
    vars.put("multi_users", new ArrayList<String>()); // <-- Problem here.
    taskService.complete(task.getId(), vars);
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().list();
    assertThat(instances).hasSize(0);
  }

  protected void resetTestCounts() {
  	TestStartExecutionListener.countWithLoopCounter.set(0);
  	TestStartExecutionListener.countWithoutLoopCounter.set(0);
  	TestEndExecutionListener.countWithLoopCounter.set(0);
  	TestEndExecutionListener.countWithoutLoopCounter.set(0);
  	TestTaskCompletionListener.count.set(0);
  }

  public static class TestStartExecutionListener implements ExecutionListener {

  	public static AtomicInteger countWithLoopCounter = new AtomicInteger(0);
  	public static AtomicInteger countWithoutLoopCounter = new AtomicInteger(0);

  	@Override
  	public void notify(DelegateExecution execution) {
  		Integer loopCounter = (Integer) execution.getVariable(LOOP_COUNTER_KEY);
  		if (loopCounter != null) {
  			countWithLoopCounter.incrementAndGet();
  		} else {
  			countWithoutLoopCounter.incrementAndGet();
  		}
  	}

  }

  public static class TestEndExecutionListener implements ExecutionListener {

  	public static AtomicInteger countWithLoopCounter = new AtomicInteger(0);
  	public static AtomicInteger countWithoutLoopCounter = new AtomicInteger(0);

  	@Override
  	public void notify(DelegateExecution execution) {
  		Integer loopCounter = (Integer) execution.getVariable(LOOP_COUNTER_KEY);
  		if (loopCounter != null) {
  			countWithLoopCounter.incrementAndGet();
  		} else{
  			countWithoutLoopCounter.incrementAndGet();
  		}
  	}

  }

  public static class TestTaskCompletionListener implements TaskListener {

  	public static AtomicInteger count = new AtomicInteger(0);

  	@Override
  	public void notify(DelegateTask delegateTask) {
  		count.incrementAndGet();
  	}

  }


}
