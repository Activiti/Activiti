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
package org.activiti.upgrade.data;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

/**
 * @author Joram Barrez
 */
public class CommonDataGenerator implements DataGenerator {
  
  protected ProcessEngine processEngine;
  
  public void run() {
    
    // UpgradeTaskOneTest
    RuntimeService runtimeService = processEngine.getRuntimeService();
    TaskService taskService = processEngine.getTaskService();

    processEngine.getRepositoryService()
      .createDeployment()
      .name("simpleTaskProcess")
      .addClasspathResource("org/activiti/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml")
      .deploy();

    runtimeService.startProcessInstanceByKey("simpleTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    taskService.complete(taskId);
    
    // UpgradeTaskTwoTest
    processEngine.getRepositoryService().createDeployment().name("simpleTaskProcess")
            .addClasspathResource("org/activiti/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml")
            .deploy();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("instrument", "trumpet");
    variables.put("player", "gonzo");
    runtimeService.startProcessInstanceByKey("taskWithExecutionVariablesProcess", variables);
  }
  
  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

}
