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

import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.test.ActivitiTestCase;


/**
 * Testcase for the non-spec extensions to the task candidate use case.
 * 
 * @author Joram Barrez
 */
public class TaskAssignmentExtensionsTest extends ActivitiTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
    identityService.saveUser(identityService.newUser("fozzie"));
    
    identityService.saveGroup(identityService.newGroup("management"));
    identityService.saveGroup(identityService.newGroup("accountancy"));
    
    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "accountancy");
    identityService.createMembership("fozzie", "management");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteUser("kermit");
    super.tearDown();
  }
  
  public void testAssigneeExtension() {
    deployProcessForThisTestMethod();
    processService.startProcessInstanceByKey("assigneeExtension");
    
    List<Task> tasks = taskService.findAssignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("my task", tasks.get(0).getName());
  }
  
  public void testDuplicateAssigneeDeclaration() {
    try {
      deployProcessForThisTestMethod();
      fail("Duplicate assignee declaration should throw an exception");
    } catch (ActivitiException e) {
      assertTextPresent("duplicate assignee declaration", e.getMessage());
    }
  }
  
  public void testCandidateUsersExtension() {
    deployProcessForThisTestMethod();
    processService.startProcessInstanceByKey("candidateUsersExtension");
    
    List<Task> tasks = taskService.findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    tasks = taskService.findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
  }
  
  public void testCandidateGroupsExtension() {
    deployProcessForThisTestMethod();
    processService.startProcessInstanceByKey("candidateGroupsExtension");
    
    // Bugfix check: potentially the query could return 2 tasks since
    // kermit is a member of the two candidate groups 
    List<Task> tasks = taskService.findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());
    
    tasks  = taskService.findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    assertEquals("make profit", tasks.get(0).getName());
    
    // Test the task query find-by-candidate-group operation
    TaskQuery query = taskService.createTaskQuery();
    assertEquals(1, query.candidateGroup("management").count());
    assertEquals(1, query.candidateGroup("accountancy").count());
  }
  
  // Test where the candidate user extension is used together
  // with the spec way of defining candidate users
  public void testMixedCandidateUserDefinition() {
    deployProcessForThisTestMethod();
    processService.startProcessInstanceByKey("mixedCandidateUser");
    
    List<Task> tasks = taskService.findUnassignedTasks("kermit");
    assertEquals(1, tasks.size());
    
    tasks = taskService.findUnassignedTasks("fozzie");
    assertEquals(1, tasks.size());
    
    tasks = taskService.findUnassignedTasks("gonzo");
    assertEquals(1, tasks.size());
    
    tasks = taskService.findUnassignedTasks("mispiggy");
    assertEquals(0, tasks.size());
  }

}
