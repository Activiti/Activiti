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
public class MakeTaskChangesUsingScripts extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/bpmn/usertask/MakeTaskChangesUsingScripts.bpmn20.xml"})
  public void testTaskChangesUsingTaskListeners() {
      // GIVEN: a process with 2 usertasks, each one with TaskListener to set values on the task using javascript
      // GIVEN: when the process start, the first usertask should have almost no information before executing the tasklistener
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicTaskChanges");
    String processInstanceId = processInstance.getId();

      // WHEN: fetching the information from the current task (both runtime and historical)
    Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).singleResult();

      // THEN: runtime-task should have the correct values and should be in-sync with historical-task
    assertThat(task.getName()).isEqualTo("UserTask1.name");
    assertThat(task.getName()).isEqualTo(historicTask.getName());

    assertThat(task.getDescription()).isEqualTo("UserTask1.description");
    assertThat(task.getDescription()).isEqualTo(historicTask.getDescription());

    // TODO: we need executionEntity when setting owner, which I couldn't find it on delegateTask
    /*
      assertThat(task.getOwner()).isEqualTo("UserTask1.owner");
      assertThat(task.getOwner()).isEqualTo(historicTask.getOwner());
     */

      assertThat(task.getCategory()).isEqualTo("UserTask1.category");
      assertThat(task.getCategory()).isEqualTo(historicTask.getCategory());

      assertThat(task.getFormKey()).isEqualTo("UserTask1.formKey");
      assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey());

      assertThat(task.getAssignee()).isEqualTo("UserTask1.assignee");
      assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee());

      assertThat(task.getPriority()).isEqualTo(1);
      assertThat(task.getPriority()).isEqualTo(historicTask.getPriority());

      assertThat(task.getDueDate()).isEqualTo(new Date(1));
      assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate());

      // WHEN: completing the usertask and go to the next usertask
      // GIVEN: the second usertask should have already most of the information before executing the tasklistener
      taskService.complete(task.getId());

      // WHEN: fetching the information from the current task (both runtime and historical)
      task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
      historicTask = historyService.createHistoricTaskInstanceQuery().unfinished().singleResult();

      // THEN: runtime-task should have the correct values and should be in-sync with historical-task
      assertThat(task.getName()).isEqualTo("UserTask2.name");
      assertThat(task.getName()).isEqualTo(historicTask.getName());

      assertThat(task.getDescription()).isEqualTo("UserTask2.description");
      assertThat(task.getDescription()).isEqualTo(historicTask.getDescription());

      // TODO: we need executionEntity when setting owner, which I couldn't find it on delegateTask
    /*
      assertThat(task.getOwner()).isEqualTo("UserTask2.owner");
      assertThat(task.getOwner()).isEqualTo(historicTask.getOwner());
     */

      assertThat(task.getCategory()).isEqualTo("UserTask2.category");
      assertThat(task.getCategory()).isEqualTo(historicTask.getCategory());

      assertThat(task.getFormKey()).isEqualTo("UserTask2.formKey");
      assertThat(task.getFormKey()).isEqualTo(historicTask.getFormKey());

      assertThat(task.getAssignee()).isEqualTo("UserTask2.assignee");
      assertThat(task.getAssignee()).isEqualTo(historicTask.getAssignee());

      assertThat(task.getPriority()).isEqualTo(2);
      assertThat(task.getPriority()).isEqualTo(historicTask.getPriority());

      assertThat(task.getDueDate()).isEqualTo(new Date(2));
      assertThat(task.getDueDate()).isEqualTo(historicTask.getDueDate());


  }

}
