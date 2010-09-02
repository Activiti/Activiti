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
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInvolvementType;
import org.activiti.engine.task.TaskQuery;

/** provides access to {@link Task} related operations.
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
	 * Saves the given task to the persistent signalData store.
	 */
	void saveTask(Task task);
	
	/**
   * Returns the task with given id.
   */
  Task findTask(String taskId);
	
	/**
	 * Deletes the given task.
	 * @param taskId The id of the task that will be deleted.
	 */
	void deleteTask(String taskId);
	
	/**
	 * Deletes all tasks of the given collection.
	 * @param taskIds The ids of the tasks that will be deleted.
	 */
	void deleteTasks(Collection<String> taskIds);
	
	/**
   * Retrieves the list of tasks that are directly assigned to the given user.
   */
	List<Task> findAssignedTasks(String assignee);
	
	/**
	 * Retrieves the list of tasks that are directly assigned to the given user.
	 * @param page allows to retrieve only a part of the results. 
	 *             if null, no paging will be applied.
	 */
	List<Task> findAssignedTasks(String assignee, Page page);
	
	/**
   * Retrieves the list of tasks that potentially can be done by the given user.
   * 
   * This means that the returned tasks are not yet directly assigned to the user,
   * but rather to a certain role or group.
   * 
   * To move a task from the 'candidate' task list to the 'personal' task list,
   * call the <i>claim()</i> operation.
   */
  List<Task> findUnassignedTasks(String userId);
	
	/**
	 * Same as <i>findUnassignedTasks</i>, but paged.
	 * 
	 * @param page allows to retrieve only a part of the results. 
   *             if null, no paging will be applied.
	 */
	List<Task> findUnassignedTasks(String userId, Page page);
	
	 /**
   * Claim responsibility for a task: the given user is made assignee for the task.
   */
  void claim(String taskId, String userId);
  
  /**
   * Releases (revokes) the given task. Opposite operation of <i>claim</i>
   * 
   * Only usable when the task is in the 'in progress' or 'reserved' state.
   * The task state will be put back to 'ready', without an actual owner.
   */
  void revoke(String taskId);
  
  /**
   * Retrieves the rendered task form for the given task. 
   */
  Object getTaskForm(String taskId);
  
  /**
   * Called when the task is successfully executed.
   */
  void complete(String taskId);
  
  /**
   * Called when the task is successfully executed, 
   * and the required task parameters are given by the end-user.
   */
  void complete(String taskId, Map<String, Object> variables);

  /**
   * Changes the assignee of the given task to the given userId.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Convience shorthand for addUserInvolvement(taskId, userId, {@link TaskInvolvementType}.CANDIDATE
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convience shorthand for addUserInvolvement(taskId, groupId, {@link TaskInvolvementType}.CANDIDATE
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of involvement is defined by the
   * given involvementType (@see {@link TaskInvolvementType})
   */
  void addUserInvolvement(String taskId, String userId, String involvementType);
  
  /**
   * Involves a group with a task. The type of involvement is defined by the
   * given involvementType (@see {@link TaskInvolvementType})
   */
  void addGroupInvolvement(String taskId, String groupId, String involvementType);
  
  /**
   * Changes the priority of the task.
   * 
   * Authorization: actual owner / business admin
   */
  void setPriority(String taskId, int priority);
  
  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   */
  TaskQuery createTaskQuery();
}
