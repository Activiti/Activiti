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
package org.activiti.engine.test.bpmn.usertask;

import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * Testcase for the non-spec extensions to the task candidate use case.
 * 
 * @author Joram Barrez
 */
public class TaskAssignmentExtensionsTest extends PluggableActivitiTestCase {

  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
    identityService.saveUser(identityService.newUser("fozzie"));

    identityService.saveGroup(identityService.newGroup("management"));
    identityService.saveGroup(identityService.newGroup("accountancy"));

    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "accountancy");
    identityService.createMembership("fozzie", "management");
  }

  public void tearDown() throws Exception {
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteUser("kermit");
  }

  @Deployment
  public void testAssigneeExtension() {
    runtimeService.startProcessInstanceByKey("assigneeExtension");
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskAssignee("kermit")
      .list();
    assertEquals(1, tasks.size());
    assertEquals("my task", tasks.get(0).getName());
  }

  public void testDuplicateAssigneeDeclaration() {
    try {
      String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testDuplicateAssigneeDeclaration");
      repositoryService.createDeployment().addClasspathResource(resource).deploy();
      fail("Invalid BPMN 2.0 process should not parse, but it gets parsed sucessfully");
    } catch (XMLException e) {
      // Exception is to be expected
    }
  }
  
  @Deployment
  public void testOwnerExtension() {
    runtimeService.startProcessInstanceByKey("ownerExtension");
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskOwner("gonzo")
      .list();
    assertEquals(1, tasks.size());
    assertEquals("my task", tasks.get(0).getName());
  }

  @Deployment
  public void testCandidateUsersExtension() {
    runtimeService.startProcessInstanceByKey("candidateUsersExtension");
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    tasks = taskService.createTaskQuery().taskCandidateUser("gonzo").list();
    assertEquals(1, tasks.size());
  }

  @Deployment
  public void testCandidateGroupsExtension() {
    runtimeService.startProcessInstanceByKey("candidateGroupsExtension");

    // Bugfix check: potentially the query could return 2 tasks since
    // kermit is a member of the two candidate groups
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());

    tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());

    // Test the task query find-by-candidate-group operation
    TaskQuery query = taskService.createTaskQuery();
    assertEquals(1, query.taskCandidateGroup("management").count());
    assertEquals(1, query.taskCandidateGroup("accountancy").count());
  }

  // Test where the candidate user extension is used together
  // with the spec way of defining candidate users
  @Deployment
  public void testMixedCandidateUserDefinition() {
    runtimeService.startProcessInstanceByKey("mixedCandidateUser");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());

    tasks = taskService.createTaskQuery().taskCandidateUser("fozzie").list();
    assertEquals(1, tasks.size());

    tasks = taskService.createTaskQuery().taskCandidateUser("gonzo").list();
    assertEquals(1, tasks.size());

    tasks = taskService.createTaskQuery().taskCandidateUser("mispiggy").list();
    assertEquals(0, tasks.size());
  }

}
