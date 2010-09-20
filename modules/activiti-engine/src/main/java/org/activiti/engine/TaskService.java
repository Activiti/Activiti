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
package org.activiti.engine;

import java.util.Collection;
import java.util.Map;

import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInvolvementType;
import org.activiti.engine.task.TaskQuery;

/** Service which provides access to {@link Task} related operations.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface TaskService {

	/**
	 * Creates a new task that is not related to any process instance.
	 * 
	 * The returned task is transient and must be saved with {@link #saveTask(Task)} 'manually'.
	 */
  Task newTask();
  
  /** create a new task with a user defined task id */
  Task newTask(String taskId);
	
	/**
	 * Saves the given task to the persistent data store. If the task is already
	 * present in the persistent store, it is updated.
	 * @param task the task, cannot be null.
	 */
	void saveTask(Task task);
	
	/**
	 * Deletes the given task.
	 * @param taskId The id of the task that will be deleted, cannot be null. If no task
	 * exists with the given taskId, the operation is ignored.
	 */
	void deleteTask(String taskId);
	
	/**
	 * Deletes all tasks of the given collection.
	 * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
	 * id's in the list that don't have an existing task will be ignored.
	 */
	void deleteTasks(Collection<String> taskIds);
	
	 /**
   * Claim responsibility for a task: the given user is made assignee for the task.
   * When the task is already assigned to the given user, this operation does nothing.
   * @param taskId task to claim, cannot be null.
   * @param userId user that claims the task, cannot be null.
   * @throws ActivitiException when the user or task doesn't exist or when the task
   * is already claimed by another user.
   */
  void claim(String taskId, String userId);
  
  
  /**
   * Retrieves the rendered task form for the given task.
   * @param taskId the id of the task to render the form for, cannot be null.
   * @return rendered task form. Returns null when the given task has no task form.
   */
  Object getTaskForm(String taskId);
  
  /**
   * Called when the task is successfully executed.
   * @param taskId the id of the task to complete, cannot be null.
   * @throws ActivitiException when no task exists with the given id.
   */
  void complete(String taskId);
  
  /**
   * Called when the task is successfully executed, 
   * and the required task parameters are given by the end-user.
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   * @throws ActivitiException when no task exists with the given id.
   */
  void complete(String taskId, Map<String, Object> variables);

  /**
   * Changes the assignee of the given task to the given userId.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee, cannot be null.
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Convenience shorthand for addUserInvolvement(taskId, userId, {@link TaskInvolvementType}.CANDIDATE
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for addUserInvolvement(taskId, groupId, {@link TaskInvolvementType}.CANDIDATE
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of involvement is defined by the
   * given involvementType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param involvmentType type of involvement, cannot be null (@see {@link TaskInvolvementType}).
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void addUserInvolvement(String taskId, String userId, String involvementType);
  
  /**
   * Involves a group with a task. The type of involvement is defined by the
   * given involvementType.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param involvmentType type of involvement, cannot be null (@see {@link TaskInvolvementType}).
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void addGroupInvolvement(String taskId, String groupId, String involvementType);
  
  /**
   * Changes the priority of the task.
   * 
   * Authorization: actual owner / business admin
   * 
   * @param taskId id of the task, cannot be null.
   * @param priority the new priority for the task.
   * @throws ActivitiException when the task doesn't exist.
   */
  void setPriority(String taskId, int priority);
  
  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   */
  TaskQuery createTaskQuery();
}
