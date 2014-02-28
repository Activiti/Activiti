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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez, Saeid Mirzaei
 */
public class TaskCandidateTest extends PluggableActivitiTestCase {

  private static final String KERMIT = "kermit";

  private static final String GONZO = "gonzo";

  public void setUp() throws Exception {
    super.setUp();
    
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

  public void tearDown() throws Exception {
    identityService.deleteUser(KERMIT);
    identityService.deleteUser(GONZO);
    identityService.deleteGroup("sales");
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    
    super.tearDown();
  }

  @Deployment
  public void testSingleCandidateGroup() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("singleCandidateGroup");

    // Task should not yet be assigned to kermit
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();
    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT).list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // Claim the task
    taskService.claim(task.getId(), KERMIT);

    // The task must now be gone from the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT).list();
    assertTrue(tasks.isEmpty());

    // The task will be visible on the personal task list
    tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Pay out expenses", task.getName());

    // Completing the task ends the process
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testMultipleCandidateGroups() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multipleCandidatesGroup");

    // Task should not yet be assigned to anyone
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee(KERMIT)
      .list();
    
    assertTrue(tasks.isEmpty());
    tasks = taskService
      .createTaskQuery()
      .taskAssignee(GONZO)
      .list();
    
    assertTrue(tasks.isEmpty());

    // The task should be visible in the candidate task list of Gonzo and Kermit
    // and anyone in the management/accountancy group
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("management").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("accountancy").count());
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup("sales").count());

    // Gonzo claims the task
    tasks = taskService.createTaskQuery().taskCandidateUser(GONZO).list();
    Task task = tasks.get(0);
    assertEquals("Approve expenses", task.getName());
    taskService.claim(task.getId(), GONZO);

    // The task must now be gone from the candidate task lists
    assertTrue(taskService.createTaskQuery().taskCandidateUser(KERMIT).list().isEmpty());
    assertTrue(taskService.createTaskQuery().taskCandidateUser(GONZO).list().isEmpty());
    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup("management").count());

    // The task will be visible on the personal task list of Gonzo
    assertEquals(1, taskService
      .createTaskQuery()
      .taskAssignee(GONZO)
      .count());

    // But not on the personal task list of (for example) Kermit
    assertEquals(0, taskService.createTaskQuery().taskAssignee(KERMIT).count());

    // Completing the task ends the process
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testMultipleCandidateUsers() {
    runtimeService.startProcessInstanceByKey("multipleCandidateUsersExample", Collections.singletonMap("Variable", (Object)"var"));

    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
    
    List<Task> tasks = taskService.createTaskQuery().taskInvolvedUser(KERMIT).list();
    assertEquals(1, tasks.size());
    
    Task task = tasks.get(0);
    taskService.setVariableLocal(task.getId(), "taskVar", 123);
    tasks = taskService.createTaskQuery().taskInvolvedUser(KERMIT).includeProcessVariables().includeTaskLocalVariables().list();
    task = tasks.get(0);
    
    assertEquals(1, task.getProcessVariables().size());
    assertEquals(1, task.getTaskLocalVariables().size());
    taskService.addUserIdentityLink(task.getId(), GONZO, "test");
    
    tasks = taskService.createTaskQuery().taskInvolvedUser(GONZO).includeProcessVariables().includeTaskLocalVariables().list();
    assertEquals(1, tasks.size());
    assertEquals(1, task.getProcessVariables().size());
    assertEquals(1, task.getTaskLocalVariables().size());
  }

  @Deployment
  public void testMixedCandidateUserAndGroup() {
    runtimeService.startProcessInstanceByKey("mixedCandidateUserAndGroupExample");

    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(GONZO).list().size());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
  }
  
  // test if candidate group works with expression, when there is a function with one parameter
  @Deployment
    public void testCandidateExpressionOneParam() {
	  Map<String, Object> params = new HashMap<String, Object>();
	  params.put("testBean", new TestBean());
	  
      runtimeService.startProcessInstanceByKey("candidateWithExpression", params);
      assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).list().size());
       
    }

  // test if candidate group works with expression, when there is a function with two parameters
  @Deployment
  public void testCandidateExpressionTwoParams() {
	  Map<String, Object> params = new HashMap<String, Object>();
	  params.put("testBean", new TestBean());
	  
    runtimeService.startProcessInstanceByKey("candidateWithExpression", params);
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser(KERMIT).count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("sales").count());
  }

}
