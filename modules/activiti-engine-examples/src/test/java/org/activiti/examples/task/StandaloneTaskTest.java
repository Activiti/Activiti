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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class StandaloneTaskTest extends ActivitiTestCase {

  @Before
  public void setUp() throws Exception {
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("kermit"));
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("gonzo"));
  }

  @After
  public void tearDown() throws Exception {
    processEngineBuilder.getIdentityService().deleteUser("kermit");
    processEngineBuilder.getIdentityService().deleteUser("gonzo");
  }

  @Test
  public void testCreateToComplete() {

    // Create and save task
    Task task = processEngineBuilder.getTaskService().newTask();
    task.setName("testTask");
    processEngineBuilder.getTaskService().saveTask(task);
    String taskId = task.getId();

    // Add user as candidate user
    processEngineBuilder.getTaskService().addCandidateUser(taskId, "kermit");
    processEngineBuilder.getTaskService().addCandidateUser(taskId, "gonzo");

    // Retrieve task list for jbarrez
    List<Task> tasks = processEngineBuilder.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Retrieve task list for tbaeyens
    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Claim task
    processEngineBuilder.getTaskService().claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertTrue(processEngineBuilder.getTaskService().findUnassignedTasks("kermit").isEmpty());
    assertTrue(processEngineBuilder.getTaskService().findUnassignedTasks("gonzo").isEmpty());

    // Complete task
    processEngineBuilder.getTaskService().complete(taskId);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertNull(processEngineBuilder.getTaskService().findTask(taskId));
  }

}
