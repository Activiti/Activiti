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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
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
   * Deletes the given task.
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   */
  void deleteTask(String taskId, boolean cascade);
  
  /**
   * Deletes all tasks of the given collection.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   */
  void deleteTasks(Collection<String> taskIds, boolean cascade);
  
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
   * @throws ActivitiException when no task exists with the given id or when this task is {@link DelegationState#PENDING} delegation.
   */
  void complete(String taskId);
  
  /**
   * Delegates the task to another user.  This means that the assignee is set 
   * and the delegation state is set to {@link DelegationState#PENDING} 
   * @param taskId The id of the task that will be delegated.
   * @param userId The id of the user that will be set as assignee.
   * @throws ActivitiException when no task exists with the given id.
   */
  void delegateTask(String taskId, String userId);
  
  /**
   * Marks that the assignee is done with this task and that it can be send back to the owner.  
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegationState} is set to {@link DelegationState#RESOLVED}.
   * @param taskId the id of the task to resolve, cannot be null.
   * @throws ActivitiException when no task exists with the given id.
   */
  void resolveTask(String taskId);

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
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void deleteCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void deleteCandidateGroup(String taskId, String groupId);
  
  /**
   * Removes the association between a user and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiException when the task or user doesn't exist.
   */
  void deleteUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Removes the association between a group and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiException when the task or group doesn't exist.
   */
  void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
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

  /** set variable on a task.  If the variable is not already existing, it will be created in the 
   * most outer scope.  This means the process instance in case this task is related to an 
   * execution. */
  void setVariable(String taskId, String variableName, Object value);

  /** set variables on a task.  If the variable is not already existing, it will be created in the 
   * most outer scope.  This means the process instance in case this task is related to an 
   * execution. */
  void setVariables(String taskId, Map<String, ? extends Object> variables);

  /** set variable on a task.  If the variable is not already existing, it will be created in the 
   * task.  */
  void setVariableLocal(String taskId, String variableName, Object value);

  /** set variables on a task.  If the variable is not already existing, it will be created in the 
   * task.  */
  void setVariablesLocal(String taskId, Map<String, ? extends Object> variables);

  /** get a variables and search in the task scope and if available also the execution scopes. */
  Object getVariable(String taskId, String variableName);

  /** get a variables and only search in the task scope.  */
  Object getVariableLocal(String taskId, String variableName);

  /** get all variables and search in the task scope and if available also the execution scopes. 
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)} 
   * for better performance.*/
  Map<String, Object> getVariables(String taskId);

  /** get all variables and search only in the task scope.
  * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)} 
  * for better performance.*/
  Map<String, Object> getVariablesLocal(String taskId);

  /** get values for all given variableNames and search only in the task scope. */
  Map<String, Object> getVariables(String taskId, Collection<String> variableNames);

  /** get a variable on a task */
  Map<String, Object> getVariablesLocal(String taskId, Collection<String> variableNames);
  
  /** Add a comment to a task and/or process instance. */
  void addComment(String taskId, String processInstanceId, String message);

  /** The comments related to the given task. */
  List<Comment> getTaskComments(String taskId);

  /** The comments related to the given process instance. */
  List<Comment> getProcessInstanceComments(String processInstanceId);

  /** Add a new attachment to a task and/or a process instance and use an input stream to provide the content */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content);

  /** Add a new attachment to a task and/or a process instance and use an url as the content */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url);
  
  /** Update the name and decription of an attachment */
  void saveAttachment(Attachment attachment);
  
  /** Retrieve a particular attachment */
  Attachment getAttachment(String attachmentId);
  
  /** Retrieve stream content of a particular attachment */
  InputStream getAttachmentContent(String attachmentId);
  
  /** The list of attachments associated to a task */
  List<Attachment> getTaskAttachments(String taskId);

  /** The list of attachments associated to a process instance */
  List<Attachment> getProcessInstanceAttachments(String processInstanceId);

  /** Delete an attachment */
  void deleteAttachment(String attachmentId);
}
