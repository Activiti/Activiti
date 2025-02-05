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


public class ScriptTaskListenerChangesTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/bpmn/usertask/ScriptTaskListenerChangesTest.bpmn20.xml"})
  public void testTaskChangesUsingTaskListeners() {
    // GIVEN: a process with an usertask, with a TaskListener to set values on the task using javascript
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicTaskChanges");
    String processInstanceId = processInstance.getId();

    // WHEN: fetching the information from the current task (both runtime and historical)
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).singleResult();

    // THEN: runtime-task should have the correct values and should be in-sync with historical-task
    assertThat(task.getName()).isEqualTo(historicTask.getName()).isEqualTo("name from script");
    assertThat(task.getDescription()).isEqualTo(historicTask.getDescription()).isEqualTo("description from script");
    assertThat(task.getCategory()).isEqualTo(historicTask.getCategory()).isEqualTo("category from script");
    assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey()).isEqualTo("formKey from script");
    assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee()).isEqualTo("assignee from script");
    assertThat(task.getOwner()).isEqualTo(historicTask.getOwner()).isEqualTo("owner from script");
    assertThat(task.getPriority()).isEqualTo(historicTask.getPriority()).isEqualTo(1);
    assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate()).isEqualTo(new Date(1));
  }

    @Deployment(resources={"org/activiti/engine/test/bpmn/usertask/ScriptTaskListenerChangesTest.bpmn20.xml"})
    public void testTaskChangesUsingTaskListenersOnSave() {
      // GIVEN: a process with an usertask, with a TaskListener to set values on the task using javascript
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicTaskChanges");
      String processInstanceId = processInstance.getId();

      // WHEN: fetching the information from the current task (both runtime and historical)
      Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
      HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).singleResult();

      // WHEN: changing the values of the task and saving the task
      task.setName("name from save");
      task.setOwner("owner from save");
      task.setDescription("description from save");
      task.setCategory("category from save");
      task.setFormKey("formKey from save");
      task.setAssignee("assignee from save");
      task.setPriority(2);
      task.setDueDate(new Date(2));
      taskService.saveTask(task);

      // WHEN: fetching the information from the current task (both runtime and historical)
      task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
      historicTask = historyService.createHistoricTaskInstanceQuery().singleResult();

      // THEN: runtime-task should have the values from script and should be in-sync with historical-task
      // THEN: saveTask will execute the assignment taskListener, which will revert the values to the script ones
      assertThat(task.getName()).isEqualTo(historicTask.getName()).isEqualTo("name from script");
      assertThat(task.getDescription()).isEqualTo(historicTask.getDescription()).isEqualTo("description from script");
      assertThat(task.getCategory()).isEqualTo(historicTask.getCategory()).isEqualTo("category from script");
      assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey()).isEqualTo("formKey from script");
      assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee()).isEqualTo("assignee from script");
      assertThat(task.getOwner()).isEqualTo(historicTask.getOwner()).isEqualTo("owner from script");
      assertThat(task.getPriority()).isEqualTo(historicTask.getPriority()).isEqualTo(1);
      assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate()).isEqualTo(new Date(1));
    }

}
