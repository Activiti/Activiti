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
package org.activiti.examples.bpmn.tasklistener;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class CustomTaskAssignmentTest extends ActivitiInternalTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveUser(identityService.newUser("gonzo"));
    
    identityService.saveGroup(identityService.newGroup("management"));
    
    identityService.createMembership("kermit", "management");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteGroup("management");
    super.tearDown();
  }
  
  @Deployment
  public void testCandidateGroupAssignment() {
    runtimeService.startProcessInstanceByKey("customTaskAssignment");
    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup("management").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("fozzie").count());
  }
  
  @Deployment
  public void testCandidateUserAssignment() {
    runtimeService.startProcessInstanceByKey("customTaskAssignment");
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("kermit").count());
    assertEquals(1, taskService.createTaskQuery().taskCandidateUser("fozzie").count());
    assertEquals(0, taskService.createTaskQuery().taskCandidateUser("gonzo").count());
  }
  
  @Deployment
  public void testAssigneeAssignment() {
    runtimeService.startProcessInstanceByKey("customTaskAssignment");
    assertNotNull(taskService.createTaskQuery().taskAssignee("kermit").singleResult());
    assertEquals(0, taskService.createTaskQuery().taskAssignee("gonzo").count());
  }
  
  @Deployment
  public void testOverwriteExistingAssignments() {
    
  }

}
