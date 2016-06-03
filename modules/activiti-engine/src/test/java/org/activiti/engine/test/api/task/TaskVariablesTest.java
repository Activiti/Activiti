/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.task;

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


/**
 * @author Tom Baeyens
 */
public class TaskVariablesTest extends PluggableActivitiTestCase {

  public void testStandaloneTaskVariables() {
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    taskService.saveTask(task);

    String taskId = task.getId();
    taskService.setVariable(taskId, "instrument", "trumpet");
    assertEquals("trumpet", taskService.getVariable(taskId, "instrument"));
    
    taskService.deleteTask(taskId, true);
  }
  
  @Deployment
  public void testTaskExecutionVariables() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("oneTaskProcess").getId();
    String taskId = taskService.createTaskQuery().singleResult().getId();
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    
    runtimeService.setVariable(processInstanceId, "instrument", "trumpet");
    
    expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    
    taskService.setVariable(taskId, "player", "gonzo");
    assertTrue(taskService.hasVariable(taskId, "player"));
    assertFalse(taskService.hasVariableLocal(taskId, "budget"));
    
    expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    
    taskService.setVariableLocal(taskId, "budget", "unlimited");
    assertTrue(taskService.hasVariableLocal(taskId, "budget"));
    assertTrue(taskService.hasVariable(taskId, "budget"));
    
    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("budget", "unlimited");
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, taskService.getVariables(taskId));

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
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
  	assertEquals("Hello world", variable.getValue());
  	
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
    assertEquals(2, variables.size());
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList1.get(1).getId(), "taskVar2" , "sayHello2", variables);
    
    // 2 process
    taskIds = new HashSet<String>();
    taskIds.add(taskList1.get(0).getId());
    taskIds.add(taskList1.get(1).getId());
    taskIds.add(taskList2.get(0).getId());
    taskIds.add(taskList2.get(1).getId());
    variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertEquals(4, variables.size());
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList1.get(1).getId(), "taskVar2" , "sayHello2", variables);
    checkVariable(taskList2.get(0).getId(), "taskVar3" , "sayHello3", variables);
    checkVariable(taskList2.get(1).getId(), "taskVar4" , "sayHello4", variables);
    
    // mixture 2 process
    taskIds = new HashSet<String>();
    taskIds.add(taskList1.get(0).getId());
    taskIds.add(taskList2.get(1).getId());
    variables = taskService.getVariableInstancesLocalByTaskIds(taskIds);
    assertEquals(2, variables.size());
    checkVariable(taskList1.get(0).getId(), "taskVar1" , "sayHello1", variables);
    checkVariable(taskList2.get(1).getId(), "taskVar4" , "sayHello4", variables);
  }
  
  private void checkVariable(String taskId, String name, String value, List<VariableInstance> variables){
    for (VariableInstance variable : variables){
      if (taskId.equals(variable.getTaskId())){
        assertEquals(name, variable.getName());
        assertEquals(value, variable.getValue());
        return;
      }
    }
    fail();
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
    assertEquals(serializableTypeVar, variables.get(0).getValue());
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
    assertEquals(variableInstances.size(), 2);
    assertEquals(variableInstances.get(0).getName(), "taskVar");
    assertEquals(variableInstances.get(0).getValue() , "taskVar");
    assertEquals(variableInstances.get(1).getName(), "taskVar");
    assertEquals(variableInstances.get(1).getValue() , "taskVar");
  }
  
  public static class MyVariable implements Serializable {
  	
    private static final long serialVersionUID = 1L;
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
