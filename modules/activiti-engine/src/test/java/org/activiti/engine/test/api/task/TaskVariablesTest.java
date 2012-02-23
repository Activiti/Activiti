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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
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
    
    expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, taskService.getVariablesLocal(taskId));
    expectedVariables.put("player", "gonzo");
    expectedVariables.put("instrument", "trumpet");
    assertEquals(expectedVariables, runtimeService.getVariables(processInstanceId));
    assertEquals(expectedVariables, taskService.getVariables(taskId));
    assertEquals(expectedVariables, runtimeService.getVariablesLocal(processInstanceId));
    
    taskService.setVariableLocal(taskId, "budget", "unlimited");
    
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
}
