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

import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.TaskQuery;

/** Service which provides access to {@link Task} and form related operations.
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
   * The difference with {@link #setAssignee(String, String)} is that here 
   * a check is done if the provided user is known by the identity component.
   * @param taskId task to claim, cannot be null.
   * @param userId user that claims the task. When userId is null the task is unclaimed,
   * assigned to no one.
   * @throws ActivitiException when the user or task doesn't exist or when the task
   * is already claimed by another user.
   */
  void claim(String taskId, String userId);
  
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
   * No check is done whether the user is known by the identity component.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee.
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Retrieves the {@link IdentityLink}s associated with the given task.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is associated with a certain task (eg. as candidate, assignee, etc.)
   */
  List<IdentityLink> getIdentityLinksForTask(String taskId);
  
  /**
   * Convenience shorthand for {@link #addUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #addGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of identity link is defined by the
   * given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void addUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Involves a group with a task. The type of identityLink is defined by the
   * given identityLink.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void addGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
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
