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

package org.activiti.examples.bpmn.gateway;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ParallelGatewayTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testForkJoin() {

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");
    TaskQuery query = taskService
                        .createTaskQuery()
                        .processInstanceId(pi.getId())
                        .orderByTaskName()
                        .asc();

    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    // the tasks are ordered by name (see above)
    Task task1 = tasks.get(0);
    assertEquals("Receive Payment", task1.getName());
    Task task2 = tasks.get(1);
    assertEquals("Ship Order", task2.getName());
    
    // Completing both tasks will join the concurrent executions
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    
    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("Archive Order", tasks.get(0).getName());
  }

  @Deployment
  public void testUnbalancedForkJoin() {
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("UnbalancedForkJoin");
    TaskQuery query = taskService.createTaskQuery()
                                 .processInstanceId(pi.getId())
                                 .orderByTaskName()
                                 .asc();
    
    List<Task> tasks = query.list(); 
    assertEquals(3, tasks.size());
    // the tasks are ordered by name (see above)
    Task task1 = tasks.get(0);
    assertEquals("Task 1", task1.getName());
    Task task2 = tasks.get(1);
    assertEquals("Task 2", task2.getName());
    
    // Completing the first task should *not* trigger the join
    taskService.complete(task1.getId());
    
    // Completing the second task should trigger the first join
    taskService.complete(task2.getId());
    
    tasks = query.list();
    Task task3 = tasks.get(0);
    assertEquals(2, tasks.size());
    assertEquals("Task 3", task3.getName());
    Task task4 = tasks.get(1);
    assertEquals("Task 4", task4.getName());
    
    // Completing the remaing tasks should trigger the second join and end the process
    taskService.complete(task3.getId());
    taskService.complete(task4.getId());
    
    assertProcessEnded(pi.getId());
  }
  
}
