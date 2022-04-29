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


package org.activiti.engine.test.api.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class TaskVariablesTest extends PluggableActivitiTestCase {

  public void testStandaloneTaskVariables() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.setVariable(taskId, "instrument", "trumpet");
    assertThat(taskService.getVariable(taskId, "instrument")).isEqualTo("trumpet");

    taskService.deleteTask(taskId, true);
  }

  @Deployment
  public void testTaskExecutionVariables() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    assertThat(runtimeService.getVariables(processInstanceId)).isEqualTo(expectedVariables);
    assertThat(taskService.getVariables(taskId)).isEqualTo(expectedVariables);
    assertThat(runtimeService.getVariablesLocal(processInstanceId)).isEqualTo(expectedVariables);
    assertThat(taskService.getVariablesLocal(taskId)).isEqualTo(expectedVariables);

    runtimeService.setVariable(processInstanceId, "instrument", "trumpet");

    expectedVariables = new HashMap<String, Object>();
    assertThat(taskService.getVariablesLocal(taskId)).isEqualTo(expectedVariables);
    expectedVariables.put("instrument", "trumpet");
    assertThat(runtimeService.getVariables(processInstanceId)).isEqualTo(expectedVariables);
    assertThat(taskService.getVariables(taskId)).isEqualTo(expectedVariables);
    assertThat(runtimeService.getVariablesLocal(processInstanceId)).isEqualTo(expectedVariables);

    taskService.setVariable(taskId, "player", "gonzo");
    assertThat(taskService.hasVariable(taskId, "player")).isTrue();
    assertThat(taskService.hasVariableLocal(taskId, "budget")).isFalse();

    expectedVariables = new HashMap<String, Object>();
    assertThat(taskService.getVariablesLocal(taskId)).isEqualTo(expectedVariables);
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertThat(runtimeService.getVariables(processInstanceId)).isEqualTo(expectedVariables);
    assertThat(taskService.getVariables(taskId)).isEqualTo(expectedVariables);
    assertThat(runtimeService.getVariablesLocal(processInstanceId)).isEqualTo(expectedVariables);

    taskService.setVariableLocal(taskId, "budget", "unlimited");
    assertThat(taskService.hasVariableLocal(taskId, "budget")).isTrue();
    assertThat(taskService.hasVariable(taskId, "budget")).isTrue();

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("budget", "unlimited");
    assertThat(taskService.getVariablesLocal(taskId)).isEqualTo(expectedVariables);
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertThat(taskService.getVariables(taskId)).isEqualTo(expectedVariables);

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertThat(runtimeService.getVariables(processInstanceId)).isEqualTo(expectedVariables);
    assertThat(runtimeService.getVariablesLocal(processInstanceId)).isEqualTo(expectedVariables);
  }

  public void testSerializableTaskVariable() {
    Task task = taskService.newTask();
    task.setName("MyTask");
    taskService.saveTask(task);

    // Set variable
    Map<String, Object> vars = new HashMap<String, Object>();
    MyVariable myVariable = new MyVariable("Hello world");
    vars.put("theVar", myVariable);
    taskService.setVariables(task.getId(), vars);

    // Fetch variable
    MyVariable variable = (MyVariable) taskService.getVariable(task.getId(), "theVar");
    assertThat(variable.getValue()).isEqualTo("Hello world");

    // Cleanup
    taskService.deleteTask(task.getId(), true);
  }

  @Deployment
  public void testGetVariablesLocalByTaskIds(){
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("twoTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("twoTaskProcess");
    List<Task> taskList1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).list();
    List<Task> taskList2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).list();

    // Task local variables
    for(Task task : taskList1){
      if ("usertask1".equals(task.getTaskDefinitionKey())){
        taskService.setVariableLocal(task.getId(), "taskVar1", "sayHello1");
      } else {
        taskService.setVariableLocal(task.getId(), "taskVar2", "sayHello2");
      }
      // Execution variables
      taskService.setVariable(task.getId(), "executionVar1", "helloWorld1");
    }
    // Task local variables
    for (Task task : taskList2){
      if ("usertask1".equals(task.getTaskDefinitionKey())){
        taskService.setVariableLocal(task.getId(), "taskVar3", "sayHello3");
      } else {
        taskService.setVariableLocal(task.getId(), "taskVar4", "sayHello4");
      }
      // Execution variables
      taskService.setVariable(task.getId(), "executionVar2", "helloWorld2");
    }

    // only 1 process
    Set<String> taskIds = new HashSet<String>();
    taskIds.add(taskList1.get(0).getId());
    taskIds.add(taskList1.get(1).getId());
    List<VariableInstance> variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertThat(variables).hasSize(2);
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList1.get(1).getId(), "taskVar2" , "sayHello2", variables);

    // 2 process
    taskIds = new HashSet<String>();
    taskIds.add(taskList1.get(0).getId());
    taskIds.add(taskList1.get(1).getId());
    taskIds.add(taskList2.get(0).getId());
    taskIds.add(taskList2.get(1).getId());
    variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertThat(variables).hasSize(4);
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList1.get(1).getId(), "taskVar2" , "sayHello2", variables);
    checkVariable(taskList2.get(0).getId(), "taskVar3" , "sayHello3", variables);
    checkVariable(taskList2.get(1).getId(), "taskVar4" , "sayHello4", variables);

    // mixture 2 process
    taskIds = new HashSet<String>();
    taskIds.add(taskList1.get(0).getId());
    taskIds.add(taskList2.get(1).getId());
    variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertThat(variables).hasSize(2);
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList2.get(1).getId(), "taskVar4" , "sayHello4", variables);
  }

  @Deployment
  public void testGetVariablesCopiedIntoTasks(){
    //variables not automatically copied into tasks at engine level unless we turn this on
    processEngineConfiguration.setCopyVariablesToLocalForTasks(true);

    Map<String,Object> startVariables = new HashMap<>();
    startVariables.put("start1","start1");
    startVariables.put("start2","start2");

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("twoTaskProcess",startVariables);
    Task userTask1 = taskService.createTaskQuery().taskDefinitionKey("usertask1").singleResult();
    Task userTask2 = taskService.createTaskQuery().taskDefinitionKey("usertask2").singleResult();

    //both should have the process variables copied into their local
    assertThat(taskService.getVariablesLocal(userTask1.getId())).isEqualTo(startVariables);
    assertThat(taskService.getVariablesLocal(userTask2.getId())).isEqualTo(startVariables);


    //if one modifies, the other should not see the modification
    taskService.setVariableLocal(userTask1.getId(),"start1","modifiedstart1");

    assertThat(startVariables).isEqualTo(taskService.getVariablesLocal(userTask2.getId()));
    taskService.complete(userTask1.getId());

    //after completion the process variable should be updated but only that one and not task2's local variable
    assertThat(runtimeService.getVariable(processInstance1.getId(),"start1")).isEqualTo("modifiedstart1");
    assertThat(runtimeService.getVariable(processInstance1.getId(),"start2")).isEqualTo("start2");
    assertThat(taskService.getVariablesLocal(userTask2.getId())).isEqualTo(startVariables);

    processEngineConfiguration.setCopyVariablesToLocalForTasks(false);
  }

  private void checkVariable(String taskId, String name, String value, List<VariableInstance> variables) {
    assertThat(variables)
        .filteredOn(variable -> taskId.equals(variable.getTaskId()))
        .hasSize(1)
        .first()
        .satisfies(variable -> {
            assertThat(variable.getName()).isEqualTo(name);
            assertThat(variable.getValue()).isEqualTo(value);
        });
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/task/TaskVariablesTest.testTaskExecutionVariables.bpmn20.xml"
  })
  public void testGetVariablesLocalByTaskIdsForSerializableType(){
    runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();

    StringBuilder sb = new StringBuilder("a");
    for (int i = 0; i < 4001; i++) {
      sb.append("a");
    }
    String serializableTypeVar = sb.toString();

    taskService.setVariableLocal(taskId, "taskVar1", serializableTypeVar);

    // only 1 process
    Set<String> taskIds = new HashSet<String>();
    taskIds.add(taskId);
    List<VariableInstance> variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertThat(variables.get(0).getValue()).isEqualTo(serializableTypeVar);
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/variableScope.bpmn20.xml"
  })
  public void testGetVariablesLocalByTaskIdsForScope(){
    Map<String, Object> processVars = new HashMap<String, Object>();
    processVars.put("processVar", "processVar");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableScopeProcess", processVars);

    Set<String> executionIds = new HashSet<String>();
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions){
      if (!processInstance.getId().equals(execution.getId())){
        executionIds.add(execution.getId());
        runtimeService.setVariableLocal(execution.getId(), "executionVar", "executionVar");
      }
    }

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    Set<String> taskIds = new HashSet<String>();
    for (Task task : tasks){
        taskService.setVariableLocal(task.getId(), "taskVar", "taskVar");
        taskIds.add(task.getId());
    }

    List<VariableInstance> variableInstances = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertThat(2).isEqualTo(variableInstances.size());
    assertThat("taskVar").isEqualTo(variableInstances.get(0).getName());
    assertThat("taskVar").isEqualTo(variableInstances.get(0).getValue() );
    assertThat("taskVar").isEqualTo(variableInstances.get(1).getName());
    assertThat("taskVar").isEqualTo(variableInstances.get(1).getValue() );
  }

  public static class MyVariable implements Serializable {

    private String value;

    public MyVariable(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }

}
