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

package org.activiti.engine.test.api.runtime;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Marcus Klimstra
 */
public class InstanceInvolvementTest extends PluggableActivitiTestCase {
  
  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/threeParallelTasks.bpmn20.xml"})
  public void testInvolvements() {
    // "user1", "user2", "user3" and "user4 should not be involved with any process instance
    assertNoInvolvement("user1");
    assertNoInvolvement("user2");
    assertNoInvolvement("user3");
    assertNoInvolvement("user4");
  
    // start a new process instance as "user1"
    String instanceId = startProcessAsUser("threeParallelTasks", "user1");
    
    // there are supposed to be 3 tasks
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(instanceId).list();
    assertEquals(3, tasks.size());
    
    // "user1" should now be involved as the starter of the new process instance. "user2" is still not involved.
    assertInvolvement("user1", instanceId);
    assertNoInvolvement("user2");
    
    // "user2" should be involved with the new process instance after claiming a task
    taskService.claim(tasks.get(0).getId(), "user2");
    assertInvolvement("user2", instanceId);
  
    // "user2" should still be involved with the new process instance even after completing his task
    taskService.complete(tasks.get(0).getId());
    assertInvolvement("user2", instanceId);
    
    // "user3" should be involved after completing a task even without claiming it
    completeTaskAsUser(tasks.get(1).getId(), "user3");
    assertInvolvement("user3", instanceId);
    
    // "user4" should be involved after manually adding an identity link
    runtimeService.addUserIdentityLink(instanceId, "user4", "custom");
    assertInvolvement("user4", instanceId);
    
    // verify all identity links for this instance
    // note that since "user1" already is the starter, he is not involved as a participant as well
    List<IdentityLink> identityLinks = runtimeService.getIdentityLinksForProcessInstance(instanceId);
    assertTrue(containsIdentityLink(identityLinks, "user1", "starter"));
    assertTrue(containsIdentityLink(identityLinks, "user2", "participant"));
    assertTrue(containsIdentityLink(identityLinks, "user3", "participant"));
    assertTrue(containsIdentityLink(identityLinks, "user4", "custom"));
    assertEquals(4, identityLinks.size());

    // "user1" completes the remaining task, ending the process
    completeTaskAsUser(tasks.get(2).getId(), "user1");
  
    // none of the users should now be involved with any process instance
    assertNoInvolvement("user1");
    assertNoInvolvement("user2");
    assertNoInvolvement("user3");
    assertNoInvolvement("user4");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/runtime/threeParallelTasks.bpmn20.xml"})
  public void testInstanceRemoval() {
    String instanceId = startProcessAsUser("threeParallelTasks", "user1");
    assertInvolvement("user1", instanceId);
    runtimeService.deleteProcessInstance(instanceId, "Testing instance removal");
    assertNoInvolvement("user1");
    // this will fail with a "DB NOT CLEAN" if the identity links are not removed
  }
  

  /**
   * Test for ACT-1686
   */
  @Deployment(resources={
  "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testUserMultipleTimesinvolvedWithProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // Add 2 links of a different type for the same user
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "type1");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "type2");
    
    assertEquals(1L, runtimeService.createProcessInstanceQuery().involvedUser("kermit").count());
  }

  
  private void assertNoInvolvement(String userId) {
    assertEquals(0L, runtimeService.createProcessInstanceQuery().involvedUser(userId).count());
  }
  
  private void assertInvolvement(String userId, String instanceId) {
    ProcessInstance involvedInstance = runtimeService
      .createProcessInstanceQuery()
      .involvedUser(userId)
      .singleResult();
    assertEquals(instanceId, involvedInstance.getId());
  }
  
  private String startProcessAsUser(String processId, String userId) {
    try {
      identityService.setAuthenticatedUserId(userId);
      return runtimeService.startProcessInstanceByKey(processId).getId();
    }
    finally {
      identityService.setAuthenticatedUserId(null);
    }
  }
  
  private void completeTaskAsUser(String taskId, String userId) {
    try {
      identityService.setAuthenticatedUserId(userId);
      taskService.complete(taskId);
    }
    finally {
      identityService.setAuthenticatedUserId(null);
    }
  }
  
  private boolean containsIdentityLink(List<IdentityLink> identityLinks, String userId, String type) {
    for (IdentityLink identityLink : identityLinks) {
      if (userId.equals(identityLink.getUserId()) && type.equals(identityLink.getType())) {
        return true;
      }
    }
    return false;
  }

}
