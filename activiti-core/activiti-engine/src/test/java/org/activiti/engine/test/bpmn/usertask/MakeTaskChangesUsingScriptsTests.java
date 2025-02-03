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

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


/**

 */
public class MakeTaskChangesUsingScriptsTests extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/bpmn/usertask/MakeTaskChangesUsingScripts.bpmn20.xml"})
  public void testTaskChangesUsingTaskListeners() {
    // GIVEN: a process with an usertask, with a TaskListener to set values on the task using javascript
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicTaskChanges");
    String processInstanceId = processInstance.getId();

    // WHEN: fetching the information from the current task (both runtime and historical)
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).singleResult();

    // THEN: runtime-task should have the correct values and should be in-sync with historical-task
    assertThat(task.getName()).isEqualTo(historicTask.getName()).isEqualTo("UserTask.name1");
    assertThat(task.getDescription()).isEqualTo(historicTask.getDescription()).isEqualTo("UserTask.description1");
    assertThat(task.getCategory()).isEqualTo(historicTask.getCategory()).isEqualTo("UserTask.category1");
    assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey()).isEqualTo("UserTask.formKey1");
    assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee()).isEqualTo("UserTask.assignee1");
    assertThat(task.getOwner()).isEqualTo(historicTask.getOwner()).isEqualTo("UserTask.owner1");
    assertThat(task.getPriority()).isEqualTo(historicTask.getPriority()).isEqualTo(1);
    assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate()).isEqualTo(new Date(1));

    task.setName("UserTask.name2");
    task.setOwner("UserTask.owner2");
    task.setDescription("UserTask.description2");
    task.setCategory("UserTask.category2");
    task.setFormKey("UserTask.formKey2");
    task.setAssignee("UserTask.assignee2");
    task.setPriority(2);
    task.setDueDate(new Date(2));

    // WHEN: saving the task
    taskService.saveTask(task);

    // WHEN: fetching the information from the current task (both runtime and historical)
    task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
    historicTask = historyService.createHistoricTaskInstanceQuery().unfinished().singleResult();

    // THEN: runtime-task should have the correct values and should be in-sync with historical-task
      assertThat(task.getName()).isEqualTo(historicTask.getName()).isEqualTo("UserTask.name2");
      assertThat(task.getDescription()).isEqualTo(historicTask.getDescription()).isEqualTo("UserTask.description2");
      assertThat(task.getCategory()).isEqualTo(historicTask.getCategory()).isEqualTo("UserTask.category2");
      assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey()).isEqualTo("UserTask.formKey2");
      assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee()).isEqualTo("UserTask.assignee2");
      assertThat(task.getOwner()).isEqualTo(historicTask.getOwner()).isEqualTo("UserTask.owner2");
      assertThat(task.getPriority()).isEqualTo(historicTask.getPriority()).isEqualTo(2);
      assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate()).isEqualTo(new Date(2));
  }

}
