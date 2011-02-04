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

package org.activiti.engine.test.bpmn.multiinstance;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class MultiInstanceTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testSequentialUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasks").getId();
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_0", task.getAssignee());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_1", task.getAssignee());
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    assertEquals("kermit_2", task.getAssignee());
    taskService.complete(task.getId());
    
    assertNull(taskService.createTaskQuery().singleResult());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testSequentialUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miSequentialUserTasksWithTimer").getId();
    
    // Complete 1 of 3 tasks
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }
  
  @Deployment
  public void testParallelUserTasks() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasks").getId();
    
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(3, tasks.size());
    assertEquals("My Task 0", tasks.get(0).getName());
    assertEquals("My Task 1", tasks.get(1).getName());
    assertEquals("My Task 2", tasks.get(2).getName());
    
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    taskService.complete(tasks.get(2).getId());
    assertProcessEnded(procId); 
  }
  
  @Deployment
  public void testParallelUserTasksWithTimer() {
    String procId = runtimeService.startProcessInstanceByKey("miParallelUserTasksWithTimer").getId();
    
    List<Task> tasks = taskService.createTaskQuery().list();
    taskService.complete(tasks.get(0).getId());
    
    // Fire timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    Task taskAfterTimer = taskService.createTaskQuery().singleResult();
    assertEquals("taskAfterTimer", taskAfterTimer.getTaskDefinitionKey());
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(procId);
  }

}
