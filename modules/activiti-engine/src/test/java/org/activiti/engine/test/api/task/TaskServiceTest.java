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
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInvolvementType;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 */
public class TaskServiceTest extends ActivitiInternalTestCase {

  public void testSaveTaskUpdate() {
    Task task = taskService.newTask();
    task.setDescription("description");
    task.setName("taskname");
    task.setPriority(0);
    taskService.saveTask(task);

    // Fetch the task again and update
    task = taskService.findTask(task.getId());
    assertEquals("description", task.getDescription());
    assertEquals("taskname", task.getName());
    assertEquals(0, task.getPriority().intValue());

    task.setDescription("updateddescription");
    task.setName("updatedtaskname");
    task.setPriority(1);
    taskService.saveTask(task);

    task = taskService.findTask(task.getId());
    assertEquals("updateddescription", task.getDescription());
    assertEquals("updatedtaskname", task.getName());
    assertEquals(1, task.getPriority().intValue());

    // Finally, delete task
    taskService.deleteTask(task.getId());
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
    taskService.deleteTasks(Arrays.asList("unexistingtaskid1", existingTask.getId()));

    existingTask = taskService.findTask(existingTask.getId());
    assertNull(existingTask);
  }

  public void testClaimNullArguments() {
    try {
      taskService.claim(null, "userid");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }

    try {
      taskService.claim("taskId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId is null", ae.getMessage());
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

  public void testClaimUnexistingUserId() {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    try {
      taskService.claim(task.getId(), "unexistinguserid");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot claim task", ae.getMessage());
      assertTextPresent("user unexistinguserid unknown.", ae.getMessage());
    }

    taskService.deleteTask(task.getId());
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

    taskService.deleteTask(task.getId());
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
    task = taskService.findTask(task.getId());
    
    // Claim the task again with the same user. No exception should be thrown
    taskService.claim(task.getId(), user.getId());

    taskService.deleteTask(task.getId());
    identityService.deleteUser(user.getId());
  }

  public void testGetTaskFormNullTaskId() {
    try {
      taskService.getTaskForm(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      // Expected Exception
    }
  }
  
  public void testGetTaskFormUnexistingTaskId() {
    try {
      taskService.getTaskForm("unexistingtask");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("No task found for id = 'unexistingtask'", ae.getMessage());
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
    
    taskService.complete(task.getId(), null);
    
    // Fetch the task again
    task = taskService.findTask(task.getId());
    assertNull(task);
  }
  
  @SuppressWarnings("unchecked")
  public void testCompleteTaskWithParametersEmptyParameters() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    taskService.complete(task.getId(), Collections.EMPTY_MAP);
    
    // Fetch the task again
    task = taskService.findTask(task.getId());
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

    taskService.deleteTask(task.getId());
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
    task = taskService.findTask(task.getId());
    assertEquals(user.getId(), task.getAssignee());
    
    identityService.deleteUser(user.getId());
    taskService.deleteTask(task.getId());
  }
  
  public void testSetAssigneeNullTaskId() {
    try {
      taskService.setAssignee(null, "userId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testSetAssigneeNullUserId() {
    try {
      taskService.setAssignee("taskId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
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
  
  public void testSetAssigneeUnexistingUser() {    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    try {
      taskService.setAssignee(task.getId(), "unexistinguser");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find user with id unexistinguser", ae.getMessage());
    }
    
    taskService.deleteTask(task.getId());
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
    taskService.deleteTask(task.getId());
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
  
  public void testAddCandidateUserUnexistingUser() {    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    try {
      taskService.addCandidateUser(task.getId(), "unexistinguser");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find user with id unexistinguser", ae.getMessage());
    }
    
    taskService.deleteTask(task.getId());
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
  
  public void testAddCandidateGroupUnexistingGroup() {    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    try {
      taskService.addCandidateGroup(task.getId(), "unexistinggroup");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find group with id unexistinggroup", ae.getMessage());
    }
    
    taskService.deleteTask(task.getId());
  }
  
  public void testAddGroupInvolvementNullTaskId() {
    try {
      taskService.addGroupInvolvement(null, "groupId", TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddGroupInvolvementNullUserId() {
    try {
      taskService.addGroupInvolvement("taskId", null, TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddGroupInvolvementUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.addGroupInvolvement("unexistingTaskId", user.getId(), TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testAddGroupInvolvementUnexistingGroup() {    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    try {
      taskService.addGroupInvolvement(task.getId(), "unexistinguser", TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find group with id unexistinguser", ae.getMessage());
    }
    
    taskService.deleteTask(task.getId());
  }
  
  public void testAddUserInvolvementNullTaskId() {
    try {
      taskService.addUserInvolvement(null, "userId", TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("taskId is null", ae.getMessage());
    }
  }
  
  public void testAddUserInvolvementNullUserId() {
    try {
      taskService.addUserInvolvement("taskId", null, TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("userId and groupId cannot both be null", ae.getMessage());
    }
  }
  
  public void testAddUserInvolvementUnexistingTask() {
    User user = identityService.newUser("user");
    identityService.saveUser(user);
    
    try {
      taskService.addUserInvolvement("unexistingTaskId", user.getId(), TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find task with id unexistingTaskId", ae.getMessage());
    }
    
    identityService.deleteUser(user.getId());
  }
  
  public void testAddUserInvolvementUnexistingUser() {    
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    try {
      taskService.addUserInvolvement(task.getId(), "unexistinguser", TaskInvolvementType.CANDIDATE);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("Cannot find user with id unexistinguser", ae.getMessage());
    }
    
    taskService.deleteTask(task.getId());
  }
  
  public void testSetPriority() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    
    taskService.setPriority(task.getId(), 12345);
    
    // Fetch task again to check if the priority is set
    task = taskService.findTask(task.getId());
    assertEquals(12345, task.getPriority().intValue());
    
    taskService.deleteTask(task.getId());
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
