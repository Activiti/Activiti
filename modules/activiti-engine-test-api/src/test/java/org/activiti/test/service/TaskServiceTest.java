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
package org.activiti.test.service;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class TaskServiceTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @Deployment(resources={"twoTasksProcess.bpmn20.xml"})
  public void testCompleteWithParametersTask() {
    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("twoTasksProcess");
    
    // Fetch first task
    Task task = deployer.getTaskService().createTaskQuery().singleResult();
    assertEquals("First task", task.getName());
    
    // Complete first task
    Map<String, Object> taskParams = new HashMap<String, Object>();
    taskParams.put("myParam", "myValue");
    deployer.getTaskService().complete(task.getId(), taskParams);
    
    // Fetch second task
    task = deployer.getTaskService().createTaskQuery().singleResult();
    assertEquals("Second task", task.getName());
    
    // Verify task parameters set on execution
    Map<String, Object> variables = deployer.getProcessService().getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertEquals("myValue", variables.get("myParam"));
  }

}
