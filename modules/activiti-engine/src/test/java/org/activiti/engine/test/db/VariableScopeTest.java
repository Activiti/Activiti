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

package org.activiti.engine.test.db;

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


/**
 * @author Tijs Rademakers
 */
public class VariableScopeTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testVariableScope() {
    
    // After starting the process, the task in the subprocess should be active
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "test");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery()
        .processInstanceId(pi.getId())
        .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // get variables for execution id user task, should return the new value of variable test --> test2
    assertEquals("test2", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test2", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));
    
    // get variables for process instance id, should return the initial value of variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));
    
    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "test", "test3");
    
    // get variables for execution id user task, should return the new value of variable test --> test3
    assertEquals("test3", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test3", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));
    
    // get variables for process instance id, should still return the initial value of variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));
    
    runtimeService.setVariable(pi.getId(), "test", "test4");
    
    // get variables for execution id user task, should return the old value of variable test --> test3
    assertEquals("test3", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test3", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));
    
    // get variables for process instance id, should also return the initial value of variable test --> test4
    assertEquals("test4", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test4", runtimeService.getVariables(pi.getId()).get("test"));
    
    // After completing the task in the subprocess, 
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
  }
  
  /**
   * A testcase to produce and fix issue ACT-862.
   * @author Roman Smirnov
   * @author Christian Lipphardt
   */
  @Deployment
  public void testVariableNamesScope() {
    
    // After starting the process, the task in the subprocess should be active
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("test", "test");
    varMap.put("helloWorld", "helloWorld");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery()
        .processInstanceId(pi.getId())
        .singleResult();
    runtimeService.setVariableLocal(pi.getProcessInstanceId(), "mainProcessLocalVariable", "Hello World");
    
    assertEquals("Task in subprocess", subProcessTask.getName());
      
    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "subProcessLocalVariable", "Hello SubProcess");

    // Returns a set of local variablenames of pi
    List<String> result = processEngineConfiguration.
            getCommandExecutor().
            execute(new GetVariableNamesCommand(pi.getProcessInstanceId(), true));
    
    // pi contains local the variablenames "test", "helloWorld" and "mainProcessLocalVariable" but not "subProcessLocalVariable"
    assertTrue(result.contains("test"));
    assertTrue(result.contains("helloWorld"));
    assertTrue(result.contains("mainProcessLocalVariable"));
    assertFalse(result.contains("subProcessLocalVariable"));
    
    // Returns a set of global variablenames of pi
    result = processEngineConfiguration.
            getCommandExecutor().
            execute(new GetVariableNamesCommand(pi.getProcessInstanceId(), false));

    // pi contains global the variablenames "test", "helloWorld" and "mainProcessLocalVariable" but not "subProcessLocalVariable"
    assertTrue(result.contains("test"));
    assertTrue(result.contains("mainProcessLocalVariable"));
    assertTrue(result.contains("helloWorld"));
    assertFalse(result.contains("subProcessLocalVariable"));
    
    // Returns a set of local variablenames of subProcessTask execution
    result = processEngineConfiguration.
            getCommandExecutor().
            execute(new GetVariableNamesCommand(subProcessTask.getExecutionId(), true));
    
    // subProcessTask execution contains local the variablenames "test", "subProcessLocalVariable" but not "helloWorld" and "mainProcessLocalVariable"
    assertTrue(result.contains("test")); // the variable "test" was set locally by SetLocalVariableTask
    assertTrue(result.contains("subProcessLocalVariable"));
    assertFalse(result.contains("helloWorld"));
    assertFalse(result.contains("mainProcessLocalVariable"));

    // Returns a set of global variablenames of subProcessTask execution
    result = processEngineConfiguration.
            getCommandExecutor().
            execute(new GetVariableNamesCommand(subProcessTask.getExecutionId(), false));
    
    // subProcessTask execution contains global all defined variablenames    
    assertTrue(result.contains("test")); // the variable "test" was set locally by SetLocalVariableTask
    assertTrue(result.contains("subProcessLocalVariable"));
    assertTrue(result.contains("helloWorld"));
    assertTrue(result.contains("mainProcessLocalVariable"));
    
    taskService.complete(subProcessTask.getId());
  }
  
  @Deployment
  public void testModeledVariableScope() {

    // After starting the process, the task in the subprocess should be active
    Map<String, Object> varMap = new HashMap<String, Object>();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSubProcess", varMap);
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess1", subProcessTask.getName());

    // get variables for execution id user task, should return the new value of
    // variable test --> test2
    assertEquals("test2", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("test2", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should return the initial value of
    // variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));

    runtimeService.setVariableLocal(subProcessTask.getExecutionId(), "test", "testX");

    // get variables for execution id user task, should return the new value of
    // variable test --> test3
    assertEquals("testX", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("testX", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should still return the initial
    // value of variable test --> test
    assertEquals("test", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("test", runtimeService.getVariables(pi.getId()).get("test"));

    runtimeService.setVariable(pi.getId(), "test", "testY");

    // get variables for execution id user task, should return the old value of
    // variable test --> test3
    assertEquals("testX", runtimeService.getVariable(subProcessTask.getExecutionId(), "test"));
    assertEquals("testX", runtimeService.getVariables(subProcessTask.getExecutionId()).get("test"));

    // get variables for process instance id, should also return the initial
    // value of variable test --> test4
    assertEquals("testY", runtimeService.getVariable(pi.getId(), "test"));
    assertEquals("testY", runtimeService.getVariables(pi.getId()).get("test"));

    // After completing the task in the subprocess,
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());

    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();

    for (Task subProcTask : subProcessTasks) {
      if (subProcTask.getName().equals("Task in subprocess2")) {
        // get variables for execution id user task, should return the old value
        // of variable test --> test3
        assertEquals("test3", runtimeService.getVariable(subProcTask.getExecutionId(), "test"));
        assertEquals("test3", runtimeService.getVariables(subProcTask.getExecutionId()).get("test"));

        // get variables for process instance id, should also return the initial
        // value of variable test --> testY
        assertEquals("testY", runtimeService.getVariable(pi.getId(), "test"));
        assertEquals("testY", runtimeService.getVariables(pi.getId()).get("test"));
      } else if (subProcTask.getName().equals("Task in subprocess3")) {
        // get variables for execution id user task, should return the old value
        // of variable test --> test4
        assertEquals("test4", runtimeService.getVariable(subProcTask.getExecutionId(), "test"));
        assertEquals("test4", runtimeService.getVariables(subProcTask.getExecutionId()).get("test"));

        // get variables for process instance id, should also return the initial
        // value of variable test --> testY
        assertEquals("testY", runtimeService.getVariable(pi.getId(), "test"));
        assertEquals("testY", runtimeService.getVariables(pi.getId()).get("test"));
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
    String variableName = "Variable-That-Does-Not-Exist";
    Object value = runtimeService.getVariableLocal(pi.getId(), variableName);
    assertNull(value);
  }

  /**
   * A command to get the names of the variables
   * @author Roman Smirnov
   * @author Christian Lipphardt
   */
  private class GetVariableNamesCommand implements Command<List<String>> {
    
    private String executionId;
    private boolean isLocal;
    
    
    public GetVariableNamesCommand(String executionId, boolean isLocal) {
     this.executionId = executionId;
     this.isLocal = isLocal;
    }

    public List<String> execute(CommandContext commandContext) {
      if(executionId == null) {
        throw new ActivitiIllegalArgumentException("executionId is null");
      }
      
      ExecutionEntity execution = commandContext
        .getExecutionEntityManager()
        .findExecutionById(executionId);
      
      if (execution==null) {
        throw new ActivitiObjectNotFoundException("execution "+executionId+" doesn't exist", Execution.class);
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
