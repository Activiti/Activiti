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


package org.activiti.engine.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class VariableScopeTest extends PluggableActivitiTestCase {

  @Deployment
  public void testVariableScope() {

    // After starting the process, the task in the subprocess should be
    // active
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "test");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    // get variables for execution id user task, should return the new value
    // of variable test --> test2
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("test2");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("test2");

    // get variables for process instance id, should return the initial
    // value of variable test --> test
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("test");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("test");

    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "test", "test3");

    // get variables for execution id user task, should return the new value
    // of variable test --> test3
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("test3");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("test3");

    // get variables for process instance id, should still return the
    // initial value of variable test --> test
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("test");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("test");

    runtimeService.setVariable(pi.getId(), "test", "test4");

    // get variables for execution id user task, should return the old value
    // of variable test --> test3
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("test3");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("test3");

    // get variables for process instance id, should also return the initial
    // value of variable test --> test4
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("test4");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("test4");

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
  }

  /**
   * A testcase to produce and fix issue ACT-862.
   *


   */
  @Deployment
  public void testVariableNamesScope() {

    // After starting the process, the task in the subprocess should be
    // active
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "test");
    varMap.put("helloWorld", "helloWorld");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    runtimeService.setVariableLocal(pi.getProcessInstanceId(), "mainProcessLocalVariable", "Hello World");

    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess");

    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "subProcessLocalVariable", "Hello SubProcess");

    // Returns a set of local variablenames of pi
    List<String> result = processEngineConfiguration.getCommandExecutor().execute(new GetVariableNamesCommand(pi.getProcessInstanceId(), true));

    // pi contains local the variablenames "test", "helloWorld" and
    // "mainProcessLocalVariable" but not "subProcessLocalVariable"
    assertThat(result.contains("test")).isTrue();
    assertThat(result.contains("helloWorld")).isTrue();
    assertThat(result.contains("mainProcessLocalVariable")).isTrue();
    assertThat(result.contains("subProcessLocalVariable")).isFalse();

    // Returns a set of global variablenames of pi
    result = processEngineConfiguration.getCommandExecutor().execute(new GetVariableNamesCommand(pi.getProcessInstanceId(), false));

    // pi contains global the variablenames "test", "helloWorld" and
    // "mainProcessLocalVariable" but not "subProcessLocalVariable"
    assertThat(result.contains("test")).isTrue();
    assertThat(result.contains("mainProcessLocalVariable")).isTrue();
    assertThat(result.contains("helloWorld")).isTrue();
    assertThat(result.contains("subProcessLocalVariable")).isFalse();

    // Returns a set of local variablenames of subProcessTask execution
    result = processEngineConfiguration.getCommandExecutor().execute(new GetVariableNamesCommand(subProcessTask.getExecutionId(), true));

    // subProcessTask execution contains local the variablenames "test",
    // "subProcessLocalVariable" but not "helloWorld" and
    // "mainProcessLocalVariable"
    assertThat(result.contains("test")).isTrue(); // the variable "test" was set
                                         // locally by SetLocalVariableTask
    assertThat(result.contains("subProcessLocalVariable")).isTrue();
    assertThat(result.contains("helloWorld")).isFalse();
    assertThat(result.contains("mainProcessLocalVariable")).isFalse();

    // Returns a set of global variablenames of subProcessTask execution
    result = processEngineConfiguration.getCommandExecutor().execute(new GetVariableNamesCommand(subProcessTask.getExecutionId(), false));

    // subProcessTask execution contains global all defined variablenames
    assertThat(result.contains("test")).isTrue(); // the variable "test" was set
                                         // locally by SetLocalVariableTask
    assertThat(result.contains("subProcessLocalVariable")).isTrue();
    assertThat(result.contains("helloWorld")).isTrue();
    assertThat(result.contains("mainProcessLocalVariable")).isTrue();

    taskService.complete(subProcessTask.getId());
  }

  @Deployment
  public void testModeledVariableScope() {

    // After starting the process, the task in the subprocess should be
    // active
    Map<String, Object> varMap = new HashMap<String, Object>();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(subProcessTask.getName()).isEqualTo("Task in subprocess1");

    // get variables for execution id user task, should return the new value
    // of
    // variable test --> test2
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("test2");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("test2");

    // get variables for process instance id, should return the initial
    // value of
    // variable test --> test
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("test");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("test");

    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "test", "testX");

    // get variables for execution id user task, should return the new value
    // of
    // variable test --> test3
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("testX");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("testX");

    // get variables for process instance id, should still return the
    // initial
    // value of variable test --> test
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("test");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("test");

    runtimeService.setVariable(pi.getId(), "test", "testY");

    // get variables for execution id user task, should return the old value
    // of
    // variable test --> test3
    assertThat(runtimeService.getVariable(subProcessTask.getExecutionId(), "test")).isEqualTo("testX");
    assertThat(runtimeService.getVariables(subProcessTask.getExecutionId()).get("test")).isEqualTo("testX");

    // get variables for process instance id, should also return the initial
    // value of variable test --> test4
    assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("testY");
    assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("testY");

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());

    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();

    for (Task subProcTask : subProcessTasks) {
      if (subProcTask.getName().equals("Task in subprocess2")) {
        // get variables for execution id user task, should return the
        // old value
        // of variable test --> test3
        assertThat(runtimeService.getVariable(subProcTask.getExecutionId(), "test")).isEqualTo("test3");
        assertThat(runtimeService.getVariables(subProcTask.getExecutionId()).get("test")).isEqualTo("test3");

        // get variables for process instance id, should also return the
        // initial
        // value of variable test --> testY
        assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("testY");
        assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("testY");
      } else if (subProcTask.getName().equals("Task in subprocess3")) {
        // get variables for execution id user task, should return the
        // old value
        // of variable test --> test4
        assertThat(runtimeService.getVariable(subProcTask.getExecutionId(), "test")).isEqualTo("test4");
        assertThat(runtimeService.getVariables(subProcTask.getExecutionId()).get("test")).isEqualTo("test4");

        // get variables for process instance id, should also return the
        // initial
        // value of variable test --> testY
        assertThat(runtimeService.getVariable(pi.getId(), "test")).isEqualTo("testY");
        assertThat(runtimeService.getVariables(pi.getId()).get("test")).isEqualTo("testY");
      } else {
        fail("Unexpected subProcessTask: " + subProcTask);
      }
    }

    // finish process
    for (Task subProcTask : subProcessTasks) {
      taskService.complete(subProcTask.getId());
    }
  }

  @Deployment
  public void testGetVariableLocal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("getVariableLocal");
    assertThat(pi).isNotNull();
    String variableName = "Variable-That-Does-Not-Exist";
    Object value = runtimeService.getVariableLocal(pi.getId(), variableName);
    assertThat(value).isNull();
  }

  /**
   * A command to get the names of the variables
   *


   */
  private class GetVariableNamesCommand implements Command<List<String>> {

    private String executionId;
    private boolean isLocal;

    public GetVariableNamesCommand(String executionId, boolean isLocal) {
      this.executionId = executionId;
      this.isLocal = isLocal;
    }

    public List<String> execute(CommandContext commandContext) {
      if (executionId == null) {
        throw new ActivitiIllegalArgumentException("executionId is null");
      }

      ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(executionId);

      if (execution == null) {
        throw new ActivitiObjectNotFoundException("execution " + executionId + " doesn't exist", Execution.class);
      }

      List<String> executionVariables;
      if (isLocal) {
        executionVariables = new ArrayList<String>(execution.getVariableNamesLocal());
      } else {
        executionVariables = new ArrayList<String>(execution.getVariableNames());
      }

      return executionVariables;
    }

  }
}
