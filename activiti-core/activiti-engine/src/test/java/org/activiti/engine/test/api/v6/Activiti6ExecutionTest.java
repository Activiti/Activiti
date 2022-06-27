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

package org.activiti.engine.test.api.v6;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiActivityCancelledEvent;
import org.activiti.engine.delegate.event.ActivitiActivityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.junit.Test;

public class Activiti6ExecutionTest extends PluggableActivitiTestCase {

  @Test
  @Deployment
  public void testOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(2);

    Execution rootProcessInstance = null;
    Execution childExecution = null;

    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;

        assertThat(execution.getActivityId()).isNull();

      } else {
        childExecution = execution;

        assertThat(execution.getId()).isNotEqualTo(execution.getProcessInstanceId());
        assertThat(execution.getActivityId()).isEqualTo("theTask");
      }
    }

    assertThat(rootProcessInstance).isNotNull();
    assertThat(childExecution).isNotNull();

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getExecutionId()).isEqualTo(childExecution.getId());

    taskService.complete(task.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertThat(historicActivities).hasSize(3);

      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask");
      activityIds.add("theEnd");

      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        activityIds.remove(historicActivityInstance.getActivityId());
        assertThat(historicActivityInstance.getExecutionId()).isEqualTo(childExecution.getId());
      }

      assertThat(activityIds).hasSize(0);
    }
  }

  @Test
  @Deployment
  public void testOneNestedTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneNestedTaskProcess");

    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(2);

    Execution rootProcessInstance = null;
    Execution childExecution = null;

    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;

        assertThat(execution.getActivityId()).isNull();

      } else {
        childExecution = execution;

        assertThat(execution.getId()).isNotEqualTo(execution.getProcessInstanceId());
        assertThat(execution.getActivityId()).isEqualTo("theTask1");
      }
    }

    assertThat(rootProcessInstance).isNotNull();
    assertThat(childExecution).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getExecutionId()).isEqualTo(childExecution.getId());

    taskService.complete(task.getId());

    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(3);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
    Execution subTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertThat(subTaskExecution.getActivityId()).isEqualTo("subTask");

    Execution subProcessExecution = runtimeService.createExecutionQuery().executionId(subTaskExecution.getParentId()).singleResult();
    assertThat(subProcessExecution.getActivityId()).isEqualTo("subProcess");
    assertThat(subProcessExecution.getParentId()).isEqualTo(rootProcessInstance.getId());

    taskService.complete(task.getId());

    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(2);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(childExecution.getId()).isNotEqualTo(task.getExecutionId());

    Execution finalTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertThat(finalTaskExecution.getActivityId()).isEqualTo("theTask2");

    assertThat(finalTaskExecution.getParentId()).isEqualTo(rootProcessInstance.getId());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertThat(historicActivities).hasSize(8);

      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask1");
      activityIds.add("subProcess");
      activityIds.add("subStart");
      activityIds.add("subTask");
      activityIds.add("subEnd");
      activityIds.add("theTask2");
      activityIds.add("theEnd");

      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        String activityId = historicActivityInstance.getActivityId();
        activityIds.remove(activityId);

        if ("theStart".equalsIgnoreCase(activityId) ||
          "theTask1".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(childExecution.getId());

        } else if ("theTask2".equalsIgnoreCase(activityId) ||
              "theEnd".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(finalTaskExecution.getId());

        } else if ("subStart".equalsIgnoreCase(activityId) ||
            "subTask".equalsIgnoreCase(activityId) ||
            "subEnd".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(subTaskExecution.getId());

        } else if ("subProcess".equalsIgnoreCase(activityId)) {
          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(subProcessExecution.getId());
        }
      }

      assertThat(activityIds).hasSize(0);
    }
  }

  @Test
  @Deployment
  public void testSubProcessWithTimer() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessWithTimer");

    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(2);

    Execution rootProcessInstance = null;
    Execution childExecution = null;

    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;

        assertThat(execution.getActivityId()).isNull();

      } else {
        childExecution = execution;

        assertThat(execution.getActivityId()).isEqualTo("theTask1");
      }
    }

    assertThat(rootProcessInstance).isNotNull();
    assertThat(childExecution).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getExecutionId()).isEqualTo(childExecution.getId());

    taskService.complete(task.getId());

    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(4);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("subTask");
    Execution subTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertThat(subTaskExecution.getActivityId()).isEqualTo("subTask");

    Execution subProcessExecution = runtimeService.createExecutionQuery().executionId(subTaskExecution.getParentId()).singleResult();
    assertThat(subProcessExecution.getActivityId()).isEqualTo("subProcess");
    assertThat(subProcessExecution.getParentId()).isEqualTo(rootProcessInstance.getId());

    taskService.complete(task.getId());

    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertThat(executionList).hasSize(2);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(childExecution.getId()).isNotEqualTo(task.getExecutionId());

    Execution finalTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertThat(finalTaskExecution.getActivityId()).isEqualTo("theTask2");

    assertThat(finalTaskExecution.getParentId()).isEqualTo(rootProcessInstance.getId());

    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertThat(historicActivities).hasSize(8);

      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask1");
      activityIds.add("subProcess");
      activityIds.add("subStart");
      activityIds.add("subTask");
      activityIds.add("subEnd");
      activityIds.add("theTask2");
      activityIds.add("theEnd");

      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        String activityId = historicActivityInstance.getActivityId();
        activityIds.remove(activityId);

        if ("theStart".equalsIgnoreCase(activityId) ||
          "theTask1".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(childExecution.getId());

        } else if ("theTask2".equalsIgnoreCase(activityId) ||
              "theEnd".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(finalTaskExecution.getId());

        } else if ("subStart".equalsIgnoreCase(activityId) ||
            "subTask".equalsIgnoreCase(activityId) ||
            "subEnd".equalsIgnoreCase(activityId)) {

          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(subTaskExecution.getId());

        } else if ("subProcess".equalsIgnoreCase(activityId)) {
          assertThat(historicActivityInstance.getExecutionId()).isEqualTo(subProcessExecution.getId());
        }
      }

      assertThat(activityIds).hasSize(0);
    }
  }

  @Test
  @Deployment
  public void testSubProcessEvents() {
    SubProcessEventListener listener = new SubProcessEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEvents");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess").singleResult();

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    // Verify Events
    List<ActivitiEvent> events = listener.getEventsReceived();
    assertThat(events).hasSize(2);

    ActivitiActivityEvent event = (ActivitiActivityEvent) events.get(0);
    assertThat(event.getActivityType()).isEqualTo("subProcess");
    assertThat(event.getExecutionId()).isEqualTo(subProcessExecution.getId());

    event = (ActivitiActivityEvent) events.get(1);
    assertThat(event.getActivityType()).isEqualTo("subProcess");
    assertThat(event.getExecutionId()).isEqualTo(subProcessExecution.getId());

    processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
  }

  public class SubProcessEventListener implements ActivitiEventListener {

    private List<ActivitiEvent> eventsReceived;

    public SubProcessEventListener() {
      eventsReceived = new ArrayList<ActivitiEvent>();
    }

    public List<ActivitiEvent> getEventsReceived() {
      return eventsReceived;
    }

    public void clearEventsReceived() {
      eventsReceived.clear();
    }

    @Override
    public void onEvent(ActivitiEvent activitiEvent) {
      if (activitiEvent instanceof ActivitiActivityEvent) {
        ActivitiActivityEvent event = (ActivitiActivityEvent) activitiEvent;
        if ("subProcess".equals(event.getActivityType())) {
          eventsReceived.add(event);
        }
      } else if (activitiEvent instanceof ActivitiActivityCancelledEvent) {
        ActivitiActivityCancelledEvent event = (ActivitiActivityCancelledEvent) activitiEvent;
        if ("subProcess".equals(event.getActivityType())) {
          eventsReceived.add(event);
        }
      }
    }

    @Override
    public boolean isFailOnException() {
      return true;
    }
  }
}
