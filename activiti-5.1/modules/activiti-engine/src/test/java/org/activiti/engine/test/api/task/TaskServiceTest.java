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

package org.activiti.engine.test.api.task;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TaskServiceTest extends PluggableActivitiTestCase {

  public void testSaveTaskUpdate() {
    Task task = taskService.newTask();
    task.setDescription("description");
    task.setName("taskname");
    task.setPriority(0);
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("description", task.getDescription());
    assertEquals("taskname", task.getName());
    assertEquals(0, task.getPriority());

    task.setDescription("updateddescription");
    task.setName("updatedtaskname");
    task.setPriority(1);
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("updateddescription", task.getDescription());
    assertEquals("updatedtaskname", task.getName());
    assertEquals(1, task.getPriority());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }

  public void testTaskAssignee() {
    Task task = taskService.newTask();
    task.setAssignee("johndoe");
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("johndoe", task.getAssignee());

    task.setAssignee("joesmoe");
    taskService.saveTask(task);

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals("joesmoe", task.getAssignee());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }

  public void testSaveTaskNullTask() {
    try {
      taskService.saveTask(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("task is null", ae.getMessage());
    }
  }

  public void testDeleteTaskNullTaskId() {
    try {
      taskService.deleteTask(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      // Expected exception
    }
  }

  public void testDeleteTaskUnexistingTaskId() {
    // Deleting unexisting task should be silently ignored
    taskService.deleteTask("unexistingtaskid");
  }

  public void testDeleteTasksNullTaskIds() {
    try {
      taskService.deleteTasks(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      // Expected exception
    }
  }

  public void testDeleteTasksTaskIdsUnexistingTaskId() {

    Task existingTask = taskService.newTask();
    taskService.saveTask(existingTask);

    // The unexisting taskId's should be silently ignored. Existing task should
    // have been deleted.
    taskService.deleteTasks(Arrays.asList("unexistingtaskid1", existingTask.getId()), true);

    existingTask = taskService.createTaskQuery().taskId(existingTask.getId()).singleResult();
    assertNull(existingTask);
  }

  public void testClaimNullArguments() {
    try {
      taskService.claim(null, "userid");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  public void testClaimUnexistingTaskId() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    try {
      taskService.claim("unexistingtaskid", user.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingtaskid", ae.getMessage());
    }

    identityService.deleteUser(user.getId());
  }

  public void testClaimAlreadyClaimedTaskByOtherUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    User secondUser = identityService.newUser("seconduser");
    identityService.saveUser(secondUser);
    
    // Claim task the first time
    taskService.claim(task.getId(), user.getId());

    try {
      taskService.claim(task.getId(), secondUser.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Task " + task.getId() + " is already claimed by someone else", ae.getMessage());
    }

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
    identityService.deleteUser(secondUser.getId());
  }
  
  public void testClaimAlreadyClaimedTaskBySameUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    // Claim task the first time
    taskService.claim(task.getId(), user.getId());
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    
    // Claim the task again with the same user. No exception should be thrown
    taskService.claim(task.getId(), user.getId());

    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
  }
  
  public void testUnClaimTask() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    // Claim task the first time
    taskService.claim(task.getId(), user.getId());
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(user.getId(), task.getAssignee());
    
    // Unclaim the task
    taskService.claim(task.getId(), null);
    
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertNull(task.getAssignee());
    
    taskService.deleteTask(task.getId(), true);
    identityService.deleteUser(user.getId());
  }

  public void testGetTaskFormNullTaskId() {
    try {
      formService.getRenderedTaskForm(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      // Expected Exception
    }
  }
  
  public void testGetTaskFormUnexistingTaskId() {
    try {
      formService.getRenderedTaskForm("unexistingtask");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Task 'unexistingtask' not found", ae.getMessage());
    }
  }
  
  public void testCompleteTaskNullTaskId() {
    try {
      taskService.complete(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testCompleteTaskUnexistingTaskId() {
    try {
      taskService.complete("unexistingtask");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }
  
  public void testCompleteTaskWithParametersNullTaskId() {
    try {
      taskService.complete(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testCompleteTaskWithParametersUnexistingTaskId() {
    try {
      taskService.complete("unexistingtask");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }
  
  public void testCompleteTaskWithParametersNullParameters() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.complete(taskId, null);
    historyService.deleteHistoricTaskInstance(taskId);
    
    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertNull(task);
  }
  
  @SuppressWarnings("unchecked")
  public void testCompleteTaskWithParametersEmptyParameters() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    String taskId = task.getId();
    taskService.complete(taskId, Collections.EMPTY_MAP);
    historyService.deleteHistoricTaskInstance(taskId);
    
    // Fetch the task again
    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertNull(task);
  }
  
  
  @Deployment(resources = { 
    "org/activiti/engine/test/api/twoTasksProcess.bpmn20.xml" })
  public void testCompleteWithParametersTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // Fetch first task
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("First task", task.getName());

    // Complete first task
    Map<String, Object> taskParams = new HashMap<String, Object>();
    taskParams.put("myParam", "myValue");
    taskService.complete(task.getId(), taskParams);

    // Fetch second task
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Second task", task.getName());

    // Verify task parameters set on execution
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertEquals("myValue", variables.get("myParam"));

    taskService.deleteTask(task.getId(), true);
  }
  
  public void testSetAssignee() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    Task task = taskService.newTask();
    assertNull(task.getAssignee());
    taskService.saveTask(task);
    
    // Set assignee
    taskService.setAssignee(task.getId(), user.getId());
    
    // Fetch task again
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(user.getId(), task.getAssignee());
    
    identityService.deleteUser(user.getId());
    taskService.deleteTask(task.getId(), true);
  }
  
  public void testSetAssigneeNullTaskId() {
    try {
      taskService.setAssignee(null, "userId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testSetAssigneeUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.setAssignee("unexistingTaskId", user.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testAddCandidateUserDuplicate() {
    // Check behavior when adding the same user twice as candidate
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    taskService.addCandidateUser(task.getId(), user.getId());

    // Add as candidate the second time
    taskService.addCandidateUser(task.getId(), user.getId());
    
    identityService.deleteUser(user.getId());
    taskService.deleteTask(task.getId(), true);
  }
  
  public void testAddCandidateUserNullTaskId() {
    try {
      taskService.addCandidateUser(null, "userId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddCandidateUserNullUserId() {
    try {
      taskService.addCandidateUser("taskId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddCandidateUserUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.addCandidateUser("unexistingTaskId", user.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testAddCandidateGroupNullTaskId() {
    try {
      taskService.addCandidateGroup(null, "groupId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddCandidateGroupNullGroupId() {
    try {
      taskService.addCandidateGroup("taskId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddCandidateGroupUnexistingTask() {
    Group group = identityService.newGroup("group");
    identityService.saveGroup(group);
    try {
      taskService.addCandidateGroup("unexistingTaskId", group.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    identityService.deleteGroup(group.getId());
  }
  
  public void testAddGroupIdentityLinkNullTaskId() {
    try {
      taskService.addGroupIdentityLink(null, "groupId", IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddGroupIdentityLinkNullUserId() {
    try {
      taskService.addGroupIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddGroupIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.addGroupIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testAddUserIdentityLinkNullTaskId() {
    try {
      taskService.addUserIdentityLink(null, "userId", IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddUserIdentityLinkNullUserId() {
    try {
      taskService.addUserIdentityLink("taskId", null, IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddUserIdentityLinkUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.addUserIdentityLink("unexistingTaskId", user.getId(), IdentityLinkType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testGetIdentityLinksWithCandidateUser() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();
    
    identityService.saveUser(identityService.newUser("kermit"));
    
    taskService.addCandidateUser(taskId, "kermit");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("kermit", identityLinks.get(0).getUserId());
    assertNull(identityLinks.get(0).getGroupId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());
    
    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteUser("kermit");
  }
  
  public void testGetIdentityLinksWithCandidateGroup() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();
    
    identityService.saveGroup(identityService.newGroup("muppets"));
    
    taskService.addCandidateGroup(taskId, "muppets");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("muppets", identityLinks.get(0).getGroupId());
    assertNull(identityLinks.get(0).getUserId());
    assertEquals(IdentityLinkType.CANDIDATE, identityLinks.get(0).getType());
    
    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteGroup("muppets");
  }
  
  public void testGetIdentityLinksWithAssignee() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();
    
    identityService.saveUser(identityService.newUser("kermit"));
    
    taskService.claim(taskId, "kermit");
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
    assertEquals(1, identityLinks.size());
    assertEquals("kermit", identityLinks.get(0).getUserId());
    assertNull(identityLinks.get(0).getGroupId());
    assertEquals(IdentityLinkType.ASSIGNEE, identityLinks.get(0).getType());
    
    //cleanup
    taskService.deleteTask(taskId, true);
    identityService.deleteUser("kermit");
  }
  
  public void testSetPriority() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    taskService.setPriority(task.getId(), 12345);
    
    // Fetch task again to check if the priority is set
    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    assertEquals(12345, task.getPriority());
    
    taskService.deleteTask(task.getId(), true);
  }
  
  public void testSetPriorityUnexistingTaskId() {
    try {
      taskService.setPriority("unexistingtask", 12345);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingtask", ae.getMessage());
    }
  }
  
  public void testSetPriorityNullTaskId() {
    try {
      taskService.setPriority(null, 12345);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }

  
}
