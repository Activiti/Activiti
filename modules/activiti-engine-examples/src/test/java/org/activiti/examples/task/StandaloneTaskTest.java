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
package org.activiti.examples.task;

import java.util.List;

import org.activiti.engine.Task;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Joram Barrez
 */
public class StandaloneTaskTest extends ProcessEngineTestCase {

  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
  }

  public void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("gonzo");
  }

  public void testCreateToComplete() {

    // Create and save task
    Task task = taskService.newTask();
    task.setName("testTask");
    taskService.saveTask(task);
    String taskId = task.getId();

    // Add user as candidate user
    taskService.addCandidateUser(taskId, "kermit");
    taskService.addCandidateUser(taskId, "gonzo");

    // Retrieve task list for jbarrez
    List<Task> tasks = taskService.findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Retrieve task list for tbaeyens
    tasks = taskService.findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Claim task
    taskService.claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertTrue(taskService.findUnassignedTasks("kermit").isEmpty());
    assertTrue(taskService.findUnassignedTasks("gonzo").isEmpty());

    // Complete task
    taskService.complete(taskId);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertNull(taskService.findTask(taskId));
  }

}
