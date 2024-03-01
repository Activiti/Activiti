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


package org.activiti.engine.test.bpmn.subprocess;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The goal of these tests is to guarantee localVars defined on parentProcess aren't lost
 * when calling an embedded subProcess
 */
public class LocalVariablesWithSubProcessTest extends PluggableActivitiTestCase {

  @Deployment
  public void testLocalVariablesAreAvailableAfterSubProcess() {
      // GIVEN a process that creates a local variable, calls a subprocess and evaluates the initial local variable

      // WHEN process starts
      runtimeService.startProcessInstanceByKey("simplerProcess");

      // THEN after completing the subprocess, evaluation of the local variable is possible and flow reaches a user task
      TaskQuery taskQuery = taskService.createTaskQuery();
      Task taskBeforeSubProcess = taskQuery.singleResult();
      assertThat(taskBeforeSubProcess.getName()).isEqualTo("user task");
  }

    @Deployment
    public void testLocalVariablesAreAvailableAfterSubProcessParallelGateway() {
        // GIVEN a process with two parallel flows
        // GIVEN each flow sets local variables, calls a subprocess and ends with a user task

        // WHEN process starts
        runtimeService.startProcessInstanceByKey("simplerProcess");

        // THEN after completing subprocesses for each flow and reaching user tasks
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> tasks = taskQuery.list();
        assertThat(tasks).hasSize(2);

        // THEN when reaching userTaskA (flow A), we have 2 local variables
        Task taskA = tasks.stream().filter(task1 -> task1.getName().equals("user task A")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalA = runtimeService.getVariablesLocal(taskA.getExecutionId());
        assertThat(variablesLocalA).hasSize(2);
        assertThat(variablesLocalA.get("commonLocalVar")).isEqualTo("A1");
        assertThat(variablesLocalA.get("uniqueLocalVarA")).isEqualTo("A2");

        // THEN when reaching userTaskB (flow B), we have 2 local variables
        Task taskB = tasks.stream().filter(task1 -> task1.getName().equals("user task B")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalB = runtimeService.getVariablesLocal(taskB.getExecutionId());
        assertThat(variablesLocalB).hasSize(2);
        assertThat(variablesLocalB.get("commonLocalVar")).isEqualTo("B1");
        assertThat(variablesLocalB.get("uniqueLocalVarB")).isEqualTo("B2");
    }

    @Deployment
    public void testLocalVariablesAreAvailableAfterSubProcessWithUserTaskParallelGateway() {
        // GIVEN a process with two parallel flows
        // GIVEN each flow sets local variables, calls a subprocess and ends with a user task

        // WHEN process starts
        runtimeService.startProcessInstanceByKey("simplerProcess");

        // WHEN and we complete the user tasks inside each subprocess
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> subProcessUserTasks = taskQuery.list();
        assertThat(subProcessUserTasks).hasSize(2);

        Task subProcessAUserTask = subProcessUserTasks.stream().filter(task1 -> task1.getName().equals("subProcessA userTask")).findFirst().orElseThrow();
        taskService.complete(subProcessAUserTask.getId());

        Task subProcessBUserTask = subProcessUserTasks.stream().filter(task1 -> task1.getName().equals("subProcessB userTask")).findFirst().orElseThrow();
        taskService.complete(subProcessBUserTask.getId());

        // THEN we reach the user tasks on the main process
        taskQuery = taskService.createTaskQuery();
        List<Task> mainProcessUserTasks = taskQuery.list();
        assertThat(mainProcessUserTasks).hasSize(2);

        // THEN when reaching userTaskA (flow A), we have 2 local variables
        Task mainProcessAUserTask = mainProcessUserTasks.stream().filter(task1 -> task1.getName().equals("user task A")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalA = runtimeService.getVariablesLocal(mainProcessAUserTask.getExecutionId());
        assertThat(variablesLocalA).hasSize(2);
        assertThat(variablesLocalA.get("commonLocalVar")).isEqualTo("A1");
        assertThat(variablesLocalA.get("uniqueLocalVarA")).isEqualTo("A2");

        // THEN when reaching userTaskB (flow B), we have 2 local variables
        Task mainProcessBUserTask = subProcessUserTasks.stream().filter(task1 -> task1.getName().equals("user task B")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalB = runtimeService.getVariablesLocal(mainProcessBUserTask.getExecutionId());
        assertThat(variablesLocalB).hasSize(2);
        assertThat(variablesLocalB.get("commonLocalVar")).isEqualTo("B1");
        assertThat(variablesLocalB.get("uniqueLocalVarB")).isEqualTo("B2");
    }

}
