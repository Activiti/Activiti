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
package org.activiti.test.bpmn.usertask;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Testcase for the non-spec extensions to the task candidate use case.
 * 
 * @author Joram Barrez
 */
public class TaskAssignmentExtensionsTest extends ActivitiTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("kermit"));
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("gonzo"));
    processEngineBuilder.getIdentityService().saveUser(processEngineBuilder.getIdentityService().newUser("fozzie"));

    processEngineBuilder.getIdentityService().saveGroup(processEngineBuilder.getIdentityService().newGroup("management"));
    processEngineBuilder.getIdentityService().saveGroup(processEngineBuilder.getIdentityService().newGroup("accountancy"));

    processEngineBuilder.getIdentityService().createMembership("kermit", "management");
    processEngineBuilder.getIdentityService().createMembership("kermit", "accountancy");
    processEngineBuilder.getIdentityService().createMembership("fozzie", "management");
  }

  @After
  public void tearDown() throws Exception {
    processEngineBuilder.getIdentityService().deleteGroup("accountancy");
    processEngineBuilder.getIdentityService().deleteGroup("management");
    processEngineBuilder.getIdentityService().deleteUser("fozzie");
    processEngineBuilder.getIdentityService().deleteUser("gonzo");
    processEngineBuilder.getIdentityService().deleteUser("kermit");
  }

  @Test
  @ProcessDeclared
  public void testAssigneeExtension() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("assigneeExtension");
    List<Task> tasks = processEngineBuilder.getTaskService().findAssignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("my task", tasks.get(0).getName());
  }

  @Test
  public void testDuplicateAssigneeDeclaration() {
    exception.expect(ActivitiException.class);
    exception.expectMessage("duplicate assignee declaration for task");
    String resource = ResourceUtils.getProcessDefinitionResource(getClass(), (getClass().getSimpleName()+".testDuplicateAssigneeDeclaration.bpmn20.xml"));
    processEngineBuilder.getProcessService().createDeployment().name(resource).addClasspathResource(resource).deploy();
  }

  @Test
  @ProcessDeclared
  public void testCandidateUsersExtension() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("candidateUsersExtension");
    List<Task> tasks = processEngineBuilder.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
  }

  @Test
  @ProcessDeclared
  public void testCandidateGroupsExtension() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("candidateGroupsExtension");

    // Bugfix check: potentially the query could return 2 tasks since
    // kermit is a member of the two candidate groups
    List<Task> tasks = processEngineBuilder.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());

    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());

    // Test the task query find-by-candidate-group operation
    TaskQuery query = processEngineBuilder.getTaskService().createTaskQuery();
    assertEquals(1, query.candidateGroup("management").count());
    assertEquals(1, query.candidateGroup("accountancy").count());
  }

  // Test where the candidate user extension is used together
  // with the spec way of defining candidate users
  @Test
  @ProcessDeclared
  public void testMixedCandidateUserDefinition() {
    processEngineBuilder.getProcessService().startProcessInstanceByKey("mixedCandidateUser");

    List<Task> tasks = processEngineBuilder.getTaskService().findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());

    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());

    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());

    tasks = processEngineBuilder.getTaskService().findUnassignedTasks("mispiggy");
    assertEquals(0, tasks.size());
  }

}
