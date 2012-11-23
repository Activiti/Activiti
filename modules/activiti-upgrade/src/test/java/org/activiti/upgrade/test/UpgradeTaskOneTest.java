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
package org.activiti.upgrade.test;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.upgrade.UpgradeTestCase;


public class UpgradeTaskOneTest extends UpgradeTestCase {
  
  public static void main(String[] args) {
    runBeforeAndAfterInDevelopmentMode(new UpgradeTaskOneTest());
  }

  public void runInTheOldVersion() {
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
  }

  public void testSimplestTask() {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    TaskService taskService = processEngine.getTaskService();
    ManagementService managementService = processEngine.getManagementService();
    HistoryService historyService = processEngine.getHistoryService();

    Task task = taskService
      .createTaskQuery()
      .taskName("simpleTask2")
      .singleResult();
    
    String processInstanceId = task.getProcessInstanceId();
    
    long expectedHistoryTaskInstances = -1;
    String schemaHistory = managementService.getProperties().get("schema.history");
    if (schemaHistory.startsWith("create(5.0)")) {
      expectedHistoryTaskInstances = 0;
    } else {
      expectedHistoryTaskInstances = 2;
    }
    
    assertEquals(expectedHistoryTaskInstances, 
      historyService.createHistoricTaskInstanceQuery()
        .processInstanceId(processInstanceId)
        .orderByTaskName().asc()
        .count());
      
    taskService.complete(task.getId());
    
    assertEquals(1, runtimeService
            .createExecutionQuery()
            .processInstanceId(processInstanceId)
            .list()
            .size());

    assertEquals(expectedHistoryTaskInstances+1, 
            historyService.createHistoricTaskInstanceQuery()
              .processInstanceId(processInstanceId)
              .orderByTaskName().asc()
              .count());
            
    task = taskService
      .createTaskQuery()
      .taskName("simpleTask3")
      .singleResult();

    taskService.complete(task.getId());

    assertEquals(0, runtimeService
            .createExecutionQuery()
            .processInstanceId(processInstanceId)
            .list()
            .size());

    assertEquals(expectedHistoryTaskInstances+1, 
            historyService.createHistoricTaskInstanceQuery()
              .processInstanceId(processInstanceId)
              .orderByTaskName().asc()
              .count());
  }
}
