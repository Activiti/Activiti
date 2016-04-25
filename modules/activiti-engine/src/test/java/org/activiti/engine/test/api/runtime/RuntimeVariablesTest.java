package org.activiti.engine.test.api.runtime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
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
  
  private void checkVariable(String executionId, String name, String value, List<VariableInstance> variables){
    for(VariableInstance variable : variables){
        if(executionId.equals(variable.getExecutionId())){
            assertEquals(name, variable.getName());
            assertEquals(value, variable.getValue());
            return;
        }
    }
    fail();
  }
}
