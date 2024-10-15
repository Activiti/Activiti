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
import org.activiti.engine.runtime.ProcessInstance;
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
      Task taskAfterSubprocess = taskQuery.singleResult();
      assertThat(taskAfterSubprocess.getName()).isEqualTo("user task");

      // THEN local variable has value set by scriptTask before calling subprocess
      final Map<String, Object> variablesLocal = runtimeService.getVariablesLocal(taskAfterSubprocess.getExecutionId());
      assertThat(variablesLocal).hasSize(1);
      assertThat(variablesLocal.get("httpStatus")).isEqualTo(500);
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
        // GIVEN each flow sets local variables, calls a subprocess (with an user task) and ends with a user task

        // WHEN process starts
        runtimeService.startProcessInstanceByKey("simplerProcess");

        // WHEN and we complete the user tasks inside each subprocess
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> subProcessUserTasks = taskQuery.list();
        assertThat(subProcessUserTasks).hasSize(2);

        Task subProcessAUserTask = subProcessUserTasks.stream().filter(task1 -> task1.getName().equals("subProcessA userTask")).findFirst().orElseThrow();
        final Map<String, Object> variablesLocal_subprocessAUsertask = runtimeService.getVariablesLocal(subProcessAUserTask.getExecutionId());
        assertThat(variablesLocal_subprocessAUsertask).hasSize(0);
        taskService.complete(subProcessAUserTask.getId());

        Task subProcessBUserTask = subProcessUserTasks.stream().filter(task1 -> task1.getName().equals("subProcessB userTask")).findFirst().orElseThrow();
        final Map<String, Object> variablesLocal_subprocessBUsertask = runtimeService.getVariablesLocal(subProcessBUserTask.getExecutionId());
        assertThat(variablesLocal_subprocessBUsertask).hasSize(0);
        taskService.complete(subProcessBUserTask.getId());

        // THEN we reach the user tasks on the main process
        taskQuery = taskService.createTaskQuery();
        List<Task> mainProcessUserTasks = taskQuery.list();
        assertThat(mainProcessUserTasks).hasSize(2);

        // THEN when reaching userTaskA (flow A), we have 2 local variables
        Task mainProcessAUserTask = mainProcessUserTasks.stream().filter(task -> task.getName().equals("user task A")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalA = runtimeService.getVariablesLocal(mainProcessAUserTask.getExecutionId());
        assertThat(variablesLocalA).hasSize(2);
        assertThat(variablesLocalA.get("commonLocalVar")).isEqualTo("A1");
        assertThat(variablesLocalA.get("uniqueLocalVarA")).isEqualTo("A2");

        // THEN when reaching userTaskB (flow B), we have 2 local variables
        Task mainProcessBUserTask = mainProcessUserTasks.stream().filter(task -> task.getName().equals("user task B")).findFirst().orElseThrow();
        Map<String, Object> variablesLocalB = runtimeService.getVariablesLocal(mainProcessBUserTask.getExecutionId());
        assertThat(variablesLocalB).hasSize(2);
        assertThat(variablesLocalB.get("commonLocalVar")).isEqualTo("B1");
        assertThat(variablesLocalB.get("uniqueLocalVarB")).isEqualTo("B2");
    }

    @Deployment(resources = {
        "org/activiti/engine/test/bpmn/subprocess/LocalVariablesWithSubProcessTest.testLocalVariablesAreAvailableAfterSubProcessWithUserTaskParallelGateway.bpmn20.xml",
        "org/activiti/engine/test/bpmn/subprocess/LocalVariablesWithSubProcessTest.testLocalVariablesAreAvailableAfterSubProcessWithUserTaskParallelGatewayProcess2.bpmn20.xml"
    })
    public void testLocalVariablesAreAvailableAfterSubProcessWithUserTaskParallelGateway_twoProcesses() {
        final String SUBPROCESS_A_USER_TASK = "subProcessA userTask";
        final String SUBPROCESS_B_USER_TASK = "subProcessB userTask";
        final String BRANCH_A_USER_TASK = "user task A";
        final String BRANCH_B_USER_TASK = "user task B";

        // WHEN processes starts
        final ProcessInstance simplerProcess = runtimeService.startProcessInstanceByKey("simplerProcess");
        final ProcessInstance simplerProcess2 = runtimeService.startProcessInstanceByKey("simplerProcess2");

        // THEN "simplerProcess": we have two usertasks, one for each subprocess
        List<Task> simplerProcess_userTasks = getUserTasks(simplerProcess);
        assertThat(simplerProcess_userTasks).hasSize(2);

        // THEN "simplerProcess": userTask on each subprocess doesn't have any local variables
        final Task simplerProcess_subProcessA_userTask = getUserTask(simplerProcess_userTasks, SUBPROCESS_A_USER_TASK);
        final Map<String, Object> simplerProcess_subProcessA_variablesLocal = getVariablesLocal(simplerProcess_subProcessA_userTask);
        assertThat(simplerProcess_subProcessA_variablesLocal).hasSize(0);

        final Task simplerProcess_subProcessB_userTask = getUserTask(simplerProcess_userTasks, SUBPROCESS_B_USER_TASK);
        final Map<String, Object> simplerProcess_subProcessB_variablesLocal = getVariablesLocal(simplerProcess_subProcessB_userTask);
        assertThat(simplerProcess_subProcessB_variablesLocal).hasSize(0);

        // THEN "simplerProcess2": we have two usertasks, one for each subprocess
        List<Task> simplerProcess2_userTasks = getUserTasks(simplerProcess2);
        assertThat(simplerProcess2_userTasks).hasSize(2);

        // THEN "simplerProcess2": userTask on each subprocess doesn't have any local variables
        final Task simplerProcess2_subProcessA_userTask = getUserTask(simplerProcess2_userTasks, SUBPROCESS_A_USER_TASK);
        final Map<String, Object> simplerProcess2_subProcessA_variablesLocal = getVariablesLocal(simplerProcess2_subProcessA_userTask);
        assertThat(simplerProcess2_subProcessA_variablesLocal).hasSize(0);

        final Task simplerProcess2_subProcessB_userTask = getUserTask(simplerProcess2_userTasks, SUBPROCESS_B_USER_TASK);
        final Map<String, Object> simplerProcess2_subProcessB_variablesLocal = getVariablesLocal(simplerProcess2_subProcessB_userTask);
        assertThat(simplerProcess2_subProcessB_variablesLocal).hasSize(0);

        // WHEN "simplerProcess": we complete the usertask on each subprocess
        taskService.complete(simplerProcess_subProcessA_userTask.getId());
        taskService.complete(simplerProcess_subProcessB_userTask.getId());

        // WHEN "simplerProcess2": we complete the usertask on each subprocess
        taskService.complete(simplerProcess2_subProcessA_userTask.getId());
        taskService.complete(simplerProcess2_subProcessB_userTask.getId());

        // THEN "simplerProcess": the usertask after the subprocess should have the local variables set before calling the subprocess
        simplerProcess_userTasks = getUserTasks(simplerProcess);
        assertThat(simplerProcess_userTasks).hasSize(2);

        final Task simplerProcess_branchA_userTask = getUserTask(simplerProcess_userTasks, BRANCH_A_USER_TASK);
        final Map<String, Object> simplerProcess_branchA_variablesLocal = getVariablesLocal(simplerProcess_branchA_userTask);
        assertThat(simplerProcess_branchA_variablesLocal.get("commonLocalVar")).isEqualTo("A1");
        assertThat(simplerProcess_branchA_variablesLocal.get("uniqueLocalVarB")).isEqualTo("A2");

        final Task simplerProcess_branchB_userTask = getUserTask(simplerProcess_userTasks, BRANCH_B_USER_TASK);
        final Map<String, Object> simplerProcess_branchB_variablesLocal = getVariablesLocal(simplerProcess_branchB_userTask);
        assertThat(simplerProcess_branchB_variablesLocal.get("commonLocalVar")).isEqualTo("B1");
        assertThat(simplerProcess_branchB_variablesLocal.get("uniqueLocalVarB")).isEqualTo("B2");

        // THEN "simplerProcess2": the usertask after the subprocess should have the local variables set before calling the subprocess
        simplerProcess2_userTasks = getUserTasks(simplerProcess2);
        assertThat(simplerProcess2_userTasks).hasSize(2);

        final Task simplerProcess2_branchA_userTask = getUserTask(simplerProcess2_userTasks, BRANCH_A_USER_TASK);
        final Map<String, Object> simplerProcess2_branchA_variablesLocal = getVariablesLocal(simplerProcess2_branchA_userTask);
        assertThat(simplerProcess2_branchA_variablesLocal.get("commonLocalVar")).isEqualTo("C1");
        assertThat(simplerProcess2_branchA_variablesLocal.get("uniqueLocalVarB")).isEqualTo("C2");

        final Task simplerProcess2_branchB_userTask = getUserTask(simplerProcess2_userTasks, BRANCH_B_USER_TASK);
        final Map<String, Object> simplerProcess2_branchB_variablesLocal = getVariablesLocal(simplerProcess2_branchB_userTask);
        assertThat(simplerProcess2_branchB_variablesLocal.get("commonLocalVar")).isEqualTo("D1");
        assertThat(simplerProcess2_branchB_variablesLocal.get("uniqueLocalVarB")).isEqualTo("D2");

    }

    private Task getUserTask(List<Task> userTasks, String userTaskName) {
        return userTasks.stream().filter(task -> task.getName().equals(userTaskName)).findFirst().orElseThrow();
    }

    private List<Task> getUserTasks(ProcessInstance processInstance) {
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId());
        return taskQuery.list();
    }

    private Map<String, Object> getVariablesLocal(List<Task> userTasks, String userTaskName) {
        Task subProcessAUserTask = userTasks.stream().filter(task -> task.getName().equals(userTaskName)).findFirst().orElseThrow();
        return runtimeService.getVariablesLocal(subProcessAUserTask.getExecutionId());
    }
    private Map<String, Object> getVariablesLocal(Task userTask) {
        return runtimeService.getVariablesLocal(userTask.getExecutionId());
    }

    private void validateProcess(ProcessInstance processInstance) {
        final String SUBPROCESS_A_USER_TASK = "subProcessA userTask";
        final String SUBPROCESS_B_USER_TASK = "subProcessB userTask";
        final String BRANCH_A_USER_TASK = "user task A";
        final String BRANCH_B_USER_TASK = "user task B";

        // WHEN before completing the user task inside the subprocess
        // THEN we shouldn't have any local variables
        final List<Task> userTasks = getUserTasks(processInstance);
        assertThat(userTasks).hasSize(2);

        Task subprocessA_userTask = userTasks.stream().filter(task -> task.getName().equals(SUBPROCESS_A_USER_TASK)).findFirst().orElseThrow();
        getVariablesLocal(subprocessA_userTask, 0);

        Task subprocessB_userTask = userTasks.stream().filter(task -> task.getName().equals(SUBPROCESS_B_USER_TASK)).findFirst().orElseThrow();
        getVariablesLocal(subprocessB_userTask, 0);

        // WHEN we complete the user task inside the subprocess
        // THEN the user task outside the subprocess should have the local variables before the subprocess
        Task branchA_userTask = userTasks.stream().filter(task -> task.getName().equals(BRANCH_A_USER_TASK)).findFirst().orElseThrow();
        Map<String, Object> branchA_variablesLocal = getVariablesLocal(branchA_userTask, 2);
        assertThat(branchA_variablesLocal.get("commonLocalVar")).isEqualTo("A1");
        assertThat(branchA_variablesLocal.get("uniqueLocalVarA")).isEqualTo("A2");

        // THEN when reaching userTaskB (flow B), we have 2 local variables
        Task branchB_userTask = userTasks.stream().filter(task -> task.getName().equals(BRANCH_B_USER_TASK)).findFirst().orElseThrow();
        Map<String, Object> branchB_variablesLocal = getVariablesLocal(branchB_userTask, 2);
        assertThat(branchB_variablesLocal.get("commonLocalVar")).isEqualTo("B1");
        assertThat(branchB_variablesLocal.get("uniqueLocalVarB")).isEqualTo("B2");
    }

    private Map<String, Object> getVariablesLocal(Task task, int expectedSize) {
        final Map<String, Object> variablesLocal = runtimeService.getVariablesLocal(task.getExecutionId());
    //    assertThat(variablesLocal).hasSize(expectedSize);
        return variablesLocal;
    }
}
