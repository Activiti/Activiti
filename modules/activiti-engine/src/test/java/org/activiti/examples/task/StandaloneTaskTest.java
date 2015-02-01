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
package org.activiti.examples.task;

import java.util.List;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * @author Joram Barrez
 */
public class StandaloneTaskTest extends PluggableActivitiTestCase {

  public void setUp() throws Exception {
    super.setUp();
    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
  }

  public void tearDown() throws Exception {
    identityService.deleteUser("kermit");
    identityService.deleteUser("gonzo");
    super.tearDown();
  }

  public void testCreateToComplete() {

    // Create and save task
    Task task = taskService.newTask();
    task.setName("testTask");
    taskService.saveTask(task);
    String taskId = task.getId();

    // Add user as candidate user
    taskService.addCandidateUser(taskId, "kermit");
    taskService.addCandidateUser(taskId, "gonzo");

    // Retrieve task list for kermit
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());

    // Retrieve task list for gonzo
    tasks = taskService.createTaskQuery().taskCandidateUser("gonzo").list();
    assertEquals(1, tasks.size());
    assertEquals("testTask", tasks.get(0).getName());
    
    task.setName("Update name");
    taskService.saveTask(task);
    tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertEquals(1, tasks.size());
    assertEquals("Update name", tasks.get(0).getName());

    // Claim task
    taskService.claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertTrue(taskService.createTaskQuery().taskCandidateUser("kermit").list().isEmpty());
    assertTrue(taskService.createTaskQuery().taskCandidateUser("gonzo").list().isEmpty());

    // Complete task
    taskService.deleteTask(taskId, true);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertNull(taskService.createTaskQuery().taskId(taskId).singleResult());
  }
  
  public void testOptimisticLockingThrownOnMultipleUpdates() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    String taskId = task.getId();
    
    // first modification
    Task task1 = taskService.createTaskQuery().taskId(taskId).singleResult();
    Task task2 = taskService.createTaskQuery().taskId(taskId).singleResult();
    
    task1.setDescription("first modification");
    taskService.saveTask(task1);

    // second modification on the initial instance
    task2.setDescription("second modification");
    try {
      taskService.saveTask(task2);
      fail("should get an exception here as the task was modified by someone else.");
    } catch (ActivitiOptimisticLockingException expected) {
      //  exception was thrown as expected
    }
    
    taskService.deleteTask(taskId, true);
  }
  
  // See http://jira.codehaus.org/browse/ACT-1290 
  public void testRevisionUpdatedOnSave() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    assertEquals(1, ((TaskEntity) task).getRevision());

    task.setDescription("first modification");
    taskService.saveTask(task);
    assertEquals(2, ((TaskEntity) task).getRevision());

    task.setDescription("second modification");
    taskService.saveTask(task);
    assertEquals(3, ((TaskEntity) task).getRevision());
    
    taskService.deleteTask(task.getId(), true);
  }

  // See http://jira.codehaus.org/browse/ACT-1290 
  public void testRevisionUpdatedOnSaveWhenFetchedUsingQuery() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    assertEquals(1, ((TaskEntity) task).getRevision());
    
    task.setAssignee("kermit");
    taskService.saveTask(task);
    assertEquals(2, ((TaskEntity) task).getRevision());
    
    // Now fetch the task through the query api
    task = taskService.createTaskQuery().singleResult();
    assertEquals(2, ((TaskEntity) task).getRevision());
    task.setPriority(1);
    taskService.saveTask(task);
    
    assertEquals(3, ((TaskEntity) task).getRevision());
    
    taskService.deleteTask(task.getId(), true);
  }

}
