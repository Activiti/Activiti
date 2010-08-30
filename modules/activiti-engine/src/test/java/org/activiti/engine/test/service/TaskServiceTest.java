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
package org.activiti.engine.test.service;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Joram Barrez
 */
public class TaskServiceTest extends ProcessEngineTestCase {
  
  @Deployment(resources={
    "org/activiti/engine/test/service/twoTasksProcess.bpmn20.xml"})
  public void testCompleteWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");
    
    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());
    
    // Complete first task
    Map<String, Object> taskParams = new HashMap<String, Object>();
    taskParams.put("myParam", "myValue");
    taskService.complete(task.getId(), taskParams);
    
    // Fetch second task
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Second task", task.getName());
    
    // Verify task parameters set on execution
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertEquals("myValue", variables.get("myParam"));
  }

}
