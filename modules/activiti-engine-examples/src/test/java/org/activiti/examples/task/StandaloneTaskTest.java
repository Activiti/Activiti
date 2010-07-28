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

import org.activiti.engine.Task;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class StandaloneTaskTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Before
  public void setUp() throws Exception {
    deployer.getIdentityService().saveUser(deployer.getIdentityService().newUser("kermit"));
    deployer.getIdentityService().saveUser(deployer.getIdentityService().newUser("gonzo"));
  }

  @After
  public void tearDown() throws Exception {
    deployer.getIdentityService().deleteUser("kermit");
    deployer.getIdentityService().deleteUser("gonzo");
  }

  @Test
  public void testCreateToComplete() {

    // Create and save task
    Task task = deployer.getTaskService().newTask();
    task.setName("testTask");
    deployer.getTaskService().saveTask(task);
    String taskId = task.getId();

    // Add user as candidate user
    deployer.getTaskService().addCandidateUser(taskId, "kermit");
    deployer.getTaskService().addCandidateUser(taskId, "gonzo");

    // Retrieve task list for jbarrez
    List<Task> tasks = deployer.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Retrieve task list for tbaeyens
    tasks = deployer.getTaskService().findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Claim task
    deployer.getTaskService().claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertTrue(deployer.getTaskService().findUnassignedTasks("kermit").isEmpty());
    assertTrue(deployer.getTaskService().findUnassignedTasks("gonzo").isEmpty());

    // Complete task
    deployer.getTaskService().complete(taskId);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertNull(deployer.getTaskService().findTask(taskId));
  }

}
