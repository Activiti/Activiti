/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.examples.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;


public class StandaloneTaskTest extends PluggableActivitiTestCase {


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
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("testTask");

    // Retrieve task list for gonzo
    tasks = taskService.createTaskQuery().taskCandidateUser("gonzo").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("testTask");

    task.setName("Update name");
    taskService.saveTask(task);
    tasks = taskService.createTaskQuery().taskCandidateUser("kermit").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Update name");

    // Claim task
    taskService.claim(taskId, "kermit");

    // Tasks shouldn't appear in the candidate tasklists anymore
    assertThat(taskService.createTaskQuery().taskCandidateUser("kermit").list().isEmpty()).isTrue();
    assertThat(taskService.createTaskQuery().taskCandidateUser("gonzo").list().isEmpty()).isTrue();

    // Complete task
    taskService.deleteTask(taskId, true);

    // Task should be removed from runtime data
    // TODO: check for historic data when implemented!
    assertThat(taskService.createTaskQuery().taskId(taskId).singleResult()).isNull();
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
    assertThatExceptionOfType(ActivitiOptimisticLockingException.class)
      .as("should get an exception here as the task was modified by someone else.")
      .isThrownBy(() -> taskService.saveTask(task2));

    taskService.deleteTask(taskId, true);
  }

  // See https://activiti.atlassian.net/browse/ACT-1290
  public void testRevisionUpdatedOnSave() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(1);

    task.setDescription("first modification");
    taskService.saveTask(task);
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(2);

    task.setDescription("second modification");
    taskService.saveTask(task);
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(3);

    taskService.deleteTask(task.getId(), true);
  }

  // See https://activiti.atlassian.net/browse/ACT-1290
  public void testRevisionUpdatedOnSaveWhenFetchedUsingQuery() {
    Task task = taskService.newTask();
    taskService.saveTask(task);
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(1);

    task.setAssignee("kermit");
    taskService.saveTask(task);
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(2);

    // Now fetch the task through the query api
    task = taskService.createTaskQuery().singleResult();
    assertThat(((TaskEntity) task).getRevision()).isEqualTo(2);
    task.setPriority(1);
    taskService.saveTask(task);

    assertThat(((TaskEntity) task).getRevision()).isEqualTo(3);

    taskService.deleteTask(task.getId(), true);
  }

  public void testHistoricVariableOkOnUpdate() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
  		// 1. create a task
  		Task task = taskService.newTask();
  		task.setName("test execution");
  		task.setOwner("josOwner");
  		task.setAssignee("JosAssignee");
  		taskService.saveTask(task);

  		// 2. set task variables
  		Map<String, Object> taskVariables = new HashMap<String, Object>();
  		taskVariables.put("finishedAmount", 0);
  		taskService.setVariables(task.getId(), taskVariables);

  		// 3. complete this task with a new variable
  		Map<String, Object> finishVariables = new HashMap<String, Object>();
  		finishVariables.put("finishedAmount", 40);
  		taskService.complete(task.getId(), finishVariables);

  		// 4. get completed variable
  		List<HistoricVariableInstance> hisVarList = historyService.createHistoricVariableInstanceQuery().taskId(task.getId()).list();
  		assertThat(hisVarList).hasSize(1);
  		assertThat(hisVarList.get(0).getValue()).isEqualTo(40);

  		// Cleanup
  		historyService.deleteHistoricTaskInstance(task.getId());
    }
	}

}
