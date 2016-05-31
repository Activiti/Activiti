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
package org.activiti.engine.test.api.runtime;

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
 * @author Daisuke Yoshimoto
 */
public class RuntimeVariablesTest  extends PluggableActivitiTestCase {

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
    executionIds.add(task1.getExecutionId());
    List<VariableInstance> variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertEquals(1, variables.size());
    checkVariable(task1.getExecutionId(), "executionVar1", "helloWorld1", variables);
    
    // 2 process
    executionIds = new HashSet<String>();
    executionIds.add(task1.getExecutionId());
    executionIds.add(task2.getExecutionId());
    variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertEquals(2, variables.size());
    checkVariable(task1.getExecutionId(), "executionVar1", "helloWorld1", variables);
    checkVariable(task2.getExecutionId(), "executionVar2", "helloWorld2", variables);
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/RuntimeVariablesTest.testGetVariablesByExecutionIds.bpmn20.xml"
  })
  public void testGetVariablesByExecutionIdsForSerializableType(){
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    
    StringBuilder sb = new StringBuilder("a");
    for(int i = 0; i < 4001; i++) {
         sb.append("a");
    }
    String serializableTypeVar = sb.toString();
    
    // Execution variables
    taskService.setVariable(task1.getId(), "executionVar1", serializableTypeVar);
    
    // only 1 process
    Set<String> executionIds = new HashSet<String>();
    executionIds.add(task1.getExecutionId());
    List<VariableInstance> variables = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertEquals(serializableTypeVar, variables.get(0).getValue());
  }
  
  private void checkVariable(String executionId, String name, String value, List<VariableInstance> variables){
    for (VariableInstance variable : variables){
      if (executionId.equals(variable.getExecutionId())){
        assertEquals(name, variable.getName());
        assertEquals(value, variable.getValue());
        return;
      }
    }
    fail();
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
    assertEquals(executionVariableInstances.size(), 2);
    assertEquals(executionVariableInstances.get(0).getName(), "executionVar");
    assertEquals(executionVariableInstances.get(0).getValue() , "executionVar");
    assertEquals(executionVariableInstances.get(1).getName(), "executionVar");
    assertEquals(executionVariableInstances.get(1).getValue() , "executionVar");
    
    executionIds = new HashSet<String>();
    executionIds.add(processInstance.getId());
    executionVariableInstances = runtimeService.getVariableInstancesByExecutionIds(executionIds);
    assertEquals(executionVariableInstances.size(), 1);
    assertEquals(executionVariableInstances.get(0).getName(), "processVar");
    assertEquals(executionVariableInstances.get(0).getValue() , "processVar");
  }
}
