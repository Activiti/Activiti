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


package org.activiti.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class UserTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testTaskPropertiesNotNull() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getId()).isNotNull();
    assertThat(task.getName()).isEqualTo("my task");
    assertThat(task.getDescription()).isEqualTo("Very important");
    assertThat(task.getPriority() == 0).isTrue();
    assertThat(task.getAssignee()).isEqualTo("kermit");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getProcessDefinitionId()).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isNotNull();
    assertThat(task.getCreateTime()).isNotNull();

    // the next test verifies that if an execution creates a task, that no
    // events are created during creation of the task.
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertThat(taskService.getTaskEvents(task.getId())).hasSize(0);
    }
  }

  @Deployment
  public void testQuerySortingWithParameter() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);
  }

  @Deployment
  public void testCompleteAfterParallelGateway() throws InterruptedException {
    // related to https://activiti.atlassian.net/browse/ACT-1054

    // start the process
    runtimeService.startProcessInstanceByKey("ForkProcess");
    List<Task> taskList = taskService.createTaskQuery().list();
    assertThat(taskList).isNotNull();
    assertThat(taskList).hasSize(2);

    // make sure user task exists
    Task task = taskService.createTaskQuery().taskDefinitionKey("SimpleUser").singleResult();
    assertThat(task).isNotNull();

    // attempt to complete the task and get PersistenceException pointing to
    // "referential integrity constraint violation"
    taskService.complete(task.getId());
  }

  @Deployment
  public void testTaskCategory() {
    runtimeService.startProcessInstanceByKey("testTaskCategory");
    Task task = taskService.createTaskQuery().singleResult();

    // Test if the property set in the model is shown in the task
    String testCategory = "My Category";
    assertThat(task.getCategory()).isEqualTo(testCategory);

    // Test if can be queried by query API
    assertThat(taskService.createTaskQuery().taskCategory(testCategory).singleResult().getName()).isEqualTo("Task with category");
    assertThat(taskService.createTaskQuery().taskCategory("Does not exist").count() == 0).isTrue();

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // Check historic task
      HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
      assertThat(historicTaskInstance.getCategory()).isEqualTo(testCategory);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskCategory(testCategory).singleResult().getName()).isEqualTo("Task with category");
      assertThat(historyService.createHistoricTaskInstanceQuery().taskCategory("Does not exist").count() == 0).isTrue();

      // Update category
      String newCategory = "New Test Category";
      task.setCategory(newCategory);
      taskService.saveTask(task);

      task = taskService.createTaskQuery().singleResult();
      assertThat(task.getCategory()).isEqualTo(newCategory);
      assertThat(taskService.createTaskQuery().taskCategory(newCategory).singleResult().getName()).isEqualTo("Task with category");
      assertThat(taskService.createTaskQuery().taskCategory(testCategory).count() == 0).isTrue();

      // Complete task and verify history
      taskService.complete(task.getId());
      historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
      assertThat(historicTaskInstance.getCategory()).isEqualTo(newCategory);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskCategory(newCategory).singleResult().getName()).isEqualTo("Task with category");
      assertThat(historyService.createHistoricTaskInstanceQuery().taskCategory(testCategory).count() == 0).isTrue();
    }
  }

  // See https://activiti.atlassian.net/browse/ACT-4041
  public void testTaskFormKeyWhenUsingIncludeVariables() {
    deployOneTaskTestProcess();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // Set variables
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    Map<String, Object> vars = new HashMap<String, Object>();
    for (int i=0; i<20; i++) {
      vars.put("var" + i, i*2);
    }
    taskService.setVariables(task.getId(), vars);

    // Set form key
    task = taskService.createTaskQuery().singleResult();
    task.setFormKey("test123");
    taskService.saveTask(task);

    // Verify query and check form key
    task = taskService.createTaskQuery().includeProcessVariables().singleResult();
    assertThat(task.getProcessVariables()).hasSize(vars.size());

    assertThat(task.getFormKey()).isEqualTo("test123");
  }

}
