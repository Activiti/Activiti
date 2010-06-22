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
package org.activiti.test.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.IdentityService;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class TaskQueryTest extends ActivitiTestCase {

  private List<String> taskIds;
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Before
  public void setUp() throws Exception {
    taskIds = generateTestTasks();

    IdentityService identityService = deployer.getIdentityService();
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
    identityService.saveUser(identityService.newUser("fozzie"));

    identityService.saveGroup(identityService.newGroup("management"));
    identityService.saveGroup(identityService.newGroup("accountancy"));

    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "accountancy");
    identityService.createMembership("fozzie", "management");
  }

  @After
  public void tearDown() throws Exception {
    IdentityService identityService = deployer.getIdentityService();
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteUser("kermit");
    deployer.deleteTasks(taskIds);
  }

  @Test
  public void testQueryNoSpecifics() {
    TaskQuery query = deployer.getTaskService().createTaskQuery();
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }
  }

  @Test
  public void testQueryByAssignee() {
    TaskQuery query = deployer.getTaskService().createTaskQuery().assignee("gonzo");
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    query = deployer.getTaskService().createTaskQuery().assignee("kermit");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());
  }

  @Test
  public void testQueryByCandidate() {
    TaskQuery query = deployer.getTaskService().createTaskQuery().candidateUser("kermit");
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }

    query = deployer.getTaskService().createTaskQuery().candidateUser("fozzie");
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }
  }

  @Test
  public void testQueryByCandidateGroup() {
    TaskQuery query = deployer.getTaskService().createTaskQuery().candidateGroup("management");
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
    }

    query = deployer.getTaskService().createTaskQuery().candidateGroup("sales");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  @Test
  public void testQueryPagedList() {
    TaskQuery query = deployer.getTaskService().createTaskQuery().candidateUser("kermit");

    // Verifying the un-paged results
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());

    // Verifying paged results
    assertEquals(2, query.paginatedList(0, 2).size());
    assertEquals(2, query.paginatedList(2, 2).size());
    assertEquals(3, query.paginatedList(4, 3).size());
    assertEquals(1, query.paginatedList(10, 3).size());
    assertEquals(1, query.paginatedList(10, 1).size());

    // Verifying odd usages
    assertEquals(0, query.paginatedList(-1, -1).size());
    assertEquals(0, query.paginatedList(11, 2).size()); // 10 is the last index
                                                        // with a result
    assertEquals(11, query.paginatedList(0, 15).size()); // there are only 11
                                                         // tasks
  }

  /**
   * Generates some test tasks. - 6 tasks where kermit is a candidate - 1 tasks
   * where gonzo is assignee - 2 tasks assigned to management group - 2 tasks
   * assigned to accountancy group - 1 task assigned to both the management and
   * accountancy group
   */
  private List<String> generateTestTasks() {
    List<String> ids = new ArrayList<String>();

    // 6 tasks for kermit
    for (int i = 0; i < 6; i++) {
      Task task = deployer.getTaskService().newTask();
      task.setName("testTask");
      deployer.getTaskService().saveTask(task);
      ids.add(task.getId());
      deployer.getTaskService().addCandidateUser(task.getId(), "kermit");
    }

    // 1 task for gonzo
    Task task = deployer.getTaskService().newTask();
    deployer.getTaskService().saveTask(task);
    deployer.getTaskService().setAssignee(task.getId(), "gonzo");
    ids.add(task.getId());

    // 2 tasks for management group
    for (int i = 0; i < 2; i++) {
      task = deployer.getTaskService().newTask();
      deployer.getTaskService().saveTask(task);
      deployer.getTaskService().addCandidateGroup(task.getId(), "management");
      ids.add(task.getId());
    }

    // 2 tasks for accountancy group
    for (int i = 0; i < 2; i++) {
      task = deployer.getTaskService().newTask();
      deployer.getTaskService().saveTask(task);
      deployer.getTaskService().addCandidateGroup(task.getId(), "accountancy");
      ids.add(task.getId());
    }

    // 1 task assigned to management and accountancy group
    task = deployer.getTaskService().newTask();
    deployer.getTaskService().saveTask(task);
    deployer.getTaskService().addCandidateGroup(task.getId(), "management");
    deployer.getTaskService().addCandidateGroup(task.getId(), "accountancy");
    ids.add(task.getId());

    return ids;
  }

}
