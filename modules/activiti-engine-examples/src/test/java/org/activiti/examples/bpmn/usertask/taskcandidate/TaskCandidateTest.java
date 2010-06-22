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
package org.activiti.examples.bpmn.usertask.taskcandidate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.activiti.IdentityService;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.TaskService;
import org.activiti.identity.Group;
import org.activiti.identity.User;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class TaskCandidateTest extends ActivitiTestCase {

  private static final String KERMIT = "kermit";

  private static final String GONZO = "gonzo";

  @Before
  public void setUp() throws Exception {
    IdentityService identityService = processEngineBuilder.getIdentityService();
    Group accountants = identityService.newGroup("accountancy");
    identityService.saveGroup(accountants);
    Group managers = identityService.newGroup("management");
    identityService.saveGroup(managers);
    Group sales = identityService.newGroup("sales");
    identityService.saveGroup(sales);

    User kermit = identityService.newUser(KERMIT);
    identityService.saveUser(kermit);
    identityService.createMembership(KERMIT, "accountancy");

    User gonzo = identityService.newUser(GONZO);
    identityService.saveUser(gonzo);
    identityService.createMembership(GONZO, "management");
    identityService.createMembership(GONZO, "accountancy");
    identityService.createMembership(GONZO, "sales");
  }

  @After
  public void tearDown() throws Exception {
    IdentityService identityService = processEngineBuilder.getIdentityService();
    identityService.deleteUser(KERMIT);
    identityService.deleteUser(GONZO);
    identityService.deleteGroup("sales");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
  }

  @Test
  @ProcessDeclared
  public void testSingleCandidateGroup() {

    // Deploy and start process
    ProcessInstance processInstance = processEngineBuilder.getProcessService().startProcessInstanceByKey("singleCandidateGroup");
    processEngineBuilder.expectProcessEnds(processInstance.getId());

    // Task should not yet be assigned to kermit
    TaskService taskService = processEngineBuilder.getTaskService();
    List<Task> tasks = taskService.findAssignedTasks(KERMIT);
    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list
    tasks = taskService.findUnassignedTasks(KERMIT);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // Claim the task
    taskService.claim(task.getId(), KERMIT);

    // The task must now be gone from the candidate task list
    tasks = taskService.findUnassignedTasks(KERMIT);
    assertTrue(tasks.isEmpty());

    // The task will be visible on the personal task list
    tasks = taskService.findAssignedTasks(KERMIT);
    assertEquals(1, tasks.size());
    assertEquals("Pay out expenses", task.getName());

    // Completing the task ends the process
    taskService.complete(task.getId());
  }

  @Test
  @ProcessDeclared
  public void testMultipleCandidateGroups() {

    // Deploy and start process
    ProcessInstance processInstance = processEngineBuilder.getProcessService().startProcessInstanceByKey("multipleCandidatesGroup");
    processEngineBuilder.expectProcessEnds(processInstance.getId());

    // Task should not yet be assigned to anyone
    TaskService taskService = processEngineBuilder.getTaskService();
    List<Task> tasks = taskService.findAssignedTasks(KERMIT);
    assertTrue(tasks.isEmpty());
    tasks = taskService.findAssignedTasks(GONZO);
    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list of Gonzo and Kermit
    // and anyone in the management/accountancy group
    assertEquals(1, taskService.findUnassignedTasks(KERMIT).size());
    assertEquals(1, taskService.findUnassignedTasks(GONZO).size());
    assertEquals(1, taskService.createTaskQuery().candidateGroup("management").count());
    assertEquals(1, taskService.createTaskQuery().candidateGroup("accountancy").count());
    assertEquals(0, taskService.createTaskQuery().candidateGroup("sales").count());

    // Gonzo claims the task
    tasks = taskService.findUnassignedTasks(GONZO);
    Task task = tasks.get(0);
    assertEquals("Approve expenses", task.getName());
    taskService.claim(task.getId(), GONZO);

    // The task must now be gone from the candidate task lists
    assertTrue(taskService.findUnassignedTasks(KERMIT).isEmpty());
    assertTrue(taskService.findUnassignedTasks(GONZO).isEmpty());
    assertEquals(0, taskService.createTaskQuery().candidateGroup("management").count());

    // The task will be visible on the personal task list of Gonzo
    assertEquals(1, taskService.findAssignedTasks(GONZO).size());

    // But not on the personal task list of (for example) Kermit
    assertTrue(taskService.findAssignedTasks(KERMIT).isEmpty());

    // Completing the task ends the process
    taskService.complete(task.getId());
  }

  @Test
  @ProcessDeclared
  public void testMultipleCandidateUsers() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("multipleCandidateUsers");

    assertEquals(1, processEngineBuilder.getTaskService().findUnassignedTasks(GONZO).size());
    assertEquals(1, processEngineBuilder.getTaskService().findUnassignedTasks(KERMIT).size());
  }

  @Test
  @ProcessDeclared
  public void testMixedCandidateUserAndGroup() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("mixedCandidateUserAndGroup");

    assertEquals(1, processEngineBuilder.getTaskService().findUnassignedTasks(GONZO).size());
    assertEquals(1, processEngineBuilder.getTaskService().findUnassignedTasks(KERMIT).size());
  }

}
