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

package org.activiti.rest.test;

import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class TasksTest extends RestTestCase {

  public void testTasks() throws Exception {
    // the default user is kermit
    
    Task taskOne = taskService.newTask();
    taskOne.setName("helloworld");
    taskOne.setAssignee("kermit");
    taskService.saveTask(taskOne);
    
    Task taskTwo = taskService.newTask();
    taskTwo.setName("hiuniverse");
    taskTwo.setAssignee("kermit");
    taskService.saveTask(taskTwo);
    
    System.out.println(get("/tasks?first=0&max=10"));
    
    setUser("fozzie", "fozzie", true);

    System.out.println(get("/tasks?first=0&max=10"));
    
    taskService.deleteTask(taskOne.getId(), true);
    taskService.deleteTask(taskTwo.getId(), true);
  }
}
