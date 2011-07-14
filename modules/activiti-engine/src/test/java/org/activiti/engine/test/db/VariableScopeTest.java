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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
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
}
