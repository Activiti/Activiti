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

import java.util.logging.Logger;

import org.activiti.persistence.entity.Task;



/**
 * @author Tom Baeyens
 */
public class TasksTest extends RestTestCase {
  
  private static Logger log = Logger.getLogger(TasksTest.class.getName());

  public void testTasks() throws Exception {
    // the default user is kermit
    
    Task taskOne = new Task()
      .setTitle("helloworld")
      .setAssignee("kermit");
    createTask(taskOne);
    
    Task taskTwo = new Task()
      .setTitle("hiuniverse")
      .setAssignee("kermit");
    createTask(taskTwo);
    
    Task taskThree= new Task()
      .setTitle("yowwhatsup")
      .setAssignee("fozzie");
    createTask(taskThree);

    log.info("tasks of default user kermit:");
    log.info(get("/tasks?first=0&max=10"));
    
    setUser("fozzie", "fozzie", true);

    log.info("tasks of default user fozzie:");
    log.info(get("/tasks?first=0&max=10"));
    
    setUser("misspiggy", "misspiggy", true);

    log.info("tasks of default user misspiggy:");
    log.info(get("/tasks?first=0&max=10"));

    log.info("task two");
    log.info(get("/task/"+taskTwo.getId()));
  }
}
