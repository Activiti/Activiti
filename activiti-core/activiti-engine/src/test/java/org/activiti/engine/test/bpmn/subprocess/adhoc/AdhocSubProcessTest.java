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


package org.activiti.engine.test.bpmn.subprocess.adhoc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class AdhocSubProcessTest extends PluggableActivitiTestCase {

  @Deployment
  public void testSimpleAdhocSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    Execution newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    assertThat(newTaskExecution).isNotNull();
    assertThat(newTaskExecution.getId()).isNotNull();

    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask").singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    taskService.complete(subProcessTask.getId());

    enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.completeAdhocSubProcess(execution.getId());

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testSimpleCompletionCondition() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    Execution newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    assertThat(newTaskExecution).isNotNull();
    assertThat(newTaskExecution.getId()).isNotNull();

    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask").singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    taskService.complete(subProcessTask.getId());

    enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    newTaskExecution = runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");

    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task2 in subprocess");

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

      List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(pi.getId())
          .orderByHistoricTaskInstanceEndTime()
          .asc()
          .list();

      assertThat(historicTasks).hasSize(3);
      List<String> taskDefinitionKeys = new ArrayList<String>(3);
      taskDefinitionKeys.add(historicTasks.get(0).getTaskDefinitionKey());
      taskDefinitionKeys.add(historicTasks.get(1).getTaskDefinitionKey());
      taskDefinitionKeys.add(historicTasks.get(2).getTaskDefinitionKey());
      assertThat(taskDefinitionKeys.contains("subProcessTask")).isTrue();
      assertThat(taskDefinitionKeys.contains("subProcessTask2")).isTrue();
      assertThat(taskDefinitionKeys.contains("afterTask")).isTrue();

    }

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testParallelAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testSequentialAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    assertThatExceptionOfType(ActivitiException.class)
      .as("exception expected because can only enable one activity in a sequential ad-hoc sub process")
      .isThrownBy(() -> runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2"));

    taskService.complete(subProcessTask.getId());

    // now we can enable the activity
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");

    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task2 in subprocess");

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testFlowsInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    taskService.complete(subProcessTask.getId());

    assertThatExceptionOfType(ActivitiException.class)
      .as("exception expected because can only enable one activity in a sequential ad-hoc sub process")
      .isThrownBy(() -> runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2"));

    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("The next task");

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment(resources="org/activiti/engine/test/bpmn/subprocess/adhoc/AdhocSubProcessTest.testFlowsInAdhocSubProcess.bpmn20.xml")
  public void testCompleteFlowBeforeEndInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testParallelFlowsInAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(3);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask3");

    Task subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask2").singleResult();
    assertThat(subProcessTask2.getName()).isEqualTo("Task2 in subprocess");
    taskService.complete(subProcessTask2.getId());

    subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask2").singleResult();
    assertThat(subProcessTask2.getName()).isEqualTo("The next task2");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(3);

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testKeepRemainingInstancesAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(2);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task2 in subprocess");

    taskService.complete(subProcessTask.getId());

    // with no remaining executions the ad-hoc sub process will be completed
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }

  @Deployment
  public void testParallelFlowsWithKeepRemainingInstancesAdhocSubProcess() {
    Map<String, Object> variableMap = new HashMap<String, Object>();
    variableMap.put("completed", false);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", variableMap);
    Execution execution = runtimeService.createExecutionQuery().activityId("adhocSubProcess").singleResult();
    assertThat(execution).isNotNull();

    List<FlowNode> enabledActivities = runtimeService.getEnabledActivitiesFromAdhocSubProcess(execution.getId());
    assertThat(enabledActivities).hasSize(3);

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask2");
    runtimeService.executeActivityInAdhocSubProcess(execution.getId(), "subProcessTask3");

    Task subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("subProcessTask2").singleResult();
    assertThat(subProcessTask2.getName()).isEqualTo("Task2 in subprocess");
    taskService.complete(subProcessTask2.getId());

    subProcessTask2 = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask2").singleResult();
    assertThat(subProcessTask2.getName()).isEqualTo("The next task2");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(3);

    variableMap = new HashMap<String, Object>();
    variableMap.put("completed", true);
    taskService.complete(subProcessTask.getId(), variableMap);

    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(3);

    subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("sequentialTask").singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("The next task");

    taskService.complete(subProcessTask.getId(), variableMap);

    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);

    taskService.complete(subProcessTask2.getId(), variableMap);

    // ad-hoc sub process is not completed because of cancelRemainingInstances is set to false
    Task subProcessTask3 = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask3.getName()).isEqualTo("Task3 in subprocess");

    taskService.complete(subProcessTask3.getId(), variableMap);

    // with no remaining executions the ad-hoc sub process will be completed
    Task afterTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(afterTask.getName()).isEqualTo("After task");

    taskService.complete(afterTask.getId());

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult()).isNull();
  }
}
