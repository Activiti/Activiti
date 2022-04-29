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

package org.activiti.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

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

/**
 */
public class RuntimeVariablesTest extends PluggableActivitiTestCase {

  @Deployment
  public void testGetVariablesByExecutionIds(){
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();

    // Task local variables
    taskService.setVariableLocal(task1.getId(), "taskVar1", "sayHello1");
    // Execution variables
    taskService.setVariable(task1.getId(), "executionVar1", "helloWorld1");

    // Task local variables
    taskService.setVariableLocal(task2.getId(), "taskVar2", "sayHello2");
    // Execution variables
    taskService.setVariable(task2.getId(), "executionVar2", "helloWorld2");

    // only 1 process
    Set<String> executionIds = new HashSet<String>();
    executionIds.add(processInstance1.getId());
    List<VariableInstance> variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertThat(variables).hasSize(1);
    checkVariable(processInstance1.getId(), "executionVar1", "helloWorld1", variables);

    // 2 process
    executionIds = new HashSet<String>();
    executionIds.add(processInstance1.getId());
    executionIds.add(processInstance2.getId());
    variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertThat(variables).hasSize(2);
    checkVariable(processInstance1.getId(), "executionVar1", "helloWorld1", variables);
    checkVariable(processInstance2.getId(), "executionVar2", "helloWorld2", variables);
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/RuntimeVariablesTest.testGetVariablesByExecutionIds.bpmn20.xml"
  })
  public void testGetSerializableTypeVariable(){
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();

    StringBuilder sb = new StringBuilder("a");
    for (int i = 0; i < 4001; i++) {
      sb.append("a");
    }
    String serializableTypeVar = sb.toString();

    // Task variables
    taskService.setVariableLocal(task1.getId(), "taskVar1", serializableTypeVar);

    VariableInstance variableInstance = taskService.getVariableInstance(task1.getId(), "taskVar1");
    assertThat(variableInstance.getValue()).isEqualTo(serializableTypeVar);

    Map<String, VariableInstance> variableInstances = taskService.getVariableInstances(task1.getId());
    assertThat(variableInstances.get("taskVar1").getValue()).isEqualTo(serializableTypeVar);

    // Execution variables
    taskService.setVariable(task1.getId(), "executionVar1", serializableTypeVar);

    Set<String> executionIds = new HashSet<String>();
    executionIds.add(processInstance1.getId());
    List<VariableInstance> variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertThat(variables.get(0).getValue()).isEqualTo(serializableTypeVar);

    variableInstance = runtimeService.getVariableInstance(processInstance1.getId(), "executionVar1");
    assertThat(variableInstance.getValue()).isEqualTo(serializableTypeVar);

    variableInstances = runtimeService.getVariableInstances(processInstance1.getId());
    assertThat(variableInstances.get("executionVar1").getValue()).isEqualTo(serializableTypeVar);
  }

  private void checkVariable(String executionId, String name, String value, List<VariableInstance> variables) {
    assertThat(variables)
      .filteredOn(variable -> executionId.equals(variable.getExecutionId()))
      .first()
      .satisfies(variable -> {
          assertThat(variable.getName()).isEqualTo(name);
          assertThat(variable.getValue()).isEqualTo(value);
      });
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/variableScope.bpmn20.xml"
  })
  public void testGetVariablesByExecutionIdsForScope(){
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

    List<VariableInstance> executionVariableInstances = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertThat(2).isEqualTo(executionVariableInstances.size());
    assertThat("executionVar").isEqualTo(executionVariableInstances.get(0).getName());
    assertThat("executionVar").isEqualTo(executionVariableInstances.get(0).getValue() );
    assertThat("executionVar").isEqualTo(executionVariableInstances.get(1).getName());
    assertThat("executionVar").isEqualTo(executionVariableInstances.get(1).getValue() );

    executionIds = new HashSet<String>();
    executionIds.add(processInstance.getId());
    executionVariableInstances = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertThat(1).isEqualTo(executionVariableInstances.size());
    assertThat("processVar").isEqualTo(executionVariableInstances.get(0).getName());
    assertThat("processVar").isEqualTo(executionVariableInstances.get(0).getValue() );
  }
}
