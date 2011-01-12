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
package org.activiti.engine.test.bpmn.event.error;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class BoundaryErrorEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testCatchErrorOnEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorOnEmbeddedSubprocess");
    
    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("subprocessTask", task.getName());
    
    // After task completion, error end event is reached and catched
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertEquals("task after catching the error", task.getName());
  }
  
//  @Deployment
//  public void testCatchErrorOfInnerSubprocessOnOuterSubprocess() {
//    runtimeService.startProcessInstanceByKey("boundaryErrorTest");
//
//    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
//    assertEquals(2, tasks.size());
//    assertEquals("Inner subprocess task 1", tasks.get(0).getName());
//    assertEquals("Inner subprocess task 2", tasks.get(1).getName());
//    
//    // Completing task 2, will cause the end error event to throw error with code 123
//    taskService.complete(tasks.get(1).getId());
//    tasks = taskService.createTaskQuery().list();
//    Task taskAfterError = taskService.createTaskQuery().singleResult();
//    assertEquals("task outside subprocess", taskAfterError.getName());
//    
//    System.out.println("iek");
//  }

}
