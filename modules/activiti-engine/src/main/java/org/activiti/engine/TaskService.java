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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.query.NativeQuery;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.NativeTaskQuery;
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
	 * After a new task has been saved, the task instance passed into this method
	 * is updated with the id of the newly created task.
	 * @param task the task, cannot be null.
	 */
	void saveTask(Task task);
	
	/**
	 * Deletes the given task, not deleting historic information that is related to this task.
	 * @param taskId The id of the task that will be deleted, cannot be null. If no task
	 * exists with the given taskId, the operation is ignored.
	 * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
	 * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *   of a running process.
	 */
	void deleteTask(String taskId);
	
	/**
	 * Deletes all tasks of the given collection, not deleting historic information that is related 
	 * to these tasks.
	 * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
	 * id's in the list that don't have an existing task will be ignored.
	 * @throws ActivitiObjectNotFoundException when one of the task does not exist.
	 * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
	 */
	void deleteTasks(Collection<String> taskIds);
	
  /**
   * Deletes the given task.
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
   * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *   of a running process.
   */
  void deleteTask(String taskId, boolean cascade);
  
  /**
   * Deletes all tasks of the given collection.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   * @throws ActivitiObjectNotFoundException when one of the tasks does not exist.
   * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
   */
  void deleteTasks(Collection<String> taskIds, boolean cascade);
  
  /**
   * Deletes the given task, not deleting historic information that is related to this task..
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
   * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *  of a running process
   */
  void deleteTask(String taskId, String deleteReason);
  
  /**
   * Deletes all tasks of the given collection, not deleting historic information that is related to these tasks.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   * @throws ActivitiObjectNotFoundException when one of the tasks does not exist.
   * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
   */
  void deleteTasks(Collection<String> taskIds, String deleteReason);
  
	 /**
   * Claim responsibility for a task: the given user is made assignee for the task.
   * The difference with {@link #setAssignee(String, String)} is that here 
   * a check is done if the task already has a user assigned to it.
   * No check is done whether the user is known by the identity component.
   * @param taskId task to claim, cannot be null.
   * @param userId user that claims the task. When userId is null the task is unclaimed,
   * assigned to no one.
   * @throws ActivitiObjectNotFoundException when the task doesn't exist.
   * @throws ActivitiTaskAlreadyClaimedException when the task is already claimed by another user.
   */
  void claim(String taskId, String userId);
  
  /**
   * A shortcut to {@link #claim} with null user in order to unclaim the task
   * @param taskId task to unclaim, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task doesn't exist. 
   */
  void unclaim(String taskId);
  
  /**
   * Called when the task is successfully executed.
   * @param taskId the id of the task to complete, cannot be null.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   * @throws ActivitiException when this task is {@link DelegationState#PENDING} delegation.
   */
  void complete(String taskId);
  
  /**
   * Delegates the task to another user. This means that the assignee is set 
   * and the delegation state is set to {@link DelegationState#PENDING}.
   * If no owner is set on the task, the owner is set to the current assignee
   * of the task.
   * @param taskId The id of the task that will be delegated.
   * @param userId The id of the user that will be set as assignee.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void delegateTask(String taskId, String userId);
  
  /**
   * Marks that the assignee is done with this task and that it can be send back to the owner.  
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegationState} is set to {@link DelegationState#RESOLVED}.
   * @param taskId the id of the task to resolve, cannot be null.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void resolveTask(String taskId);
  
  /**
   * Marks that the assignee is done with this task providing the required
   * variables and that it can be sent back to the owner. Can only be called
   * when this task is {@link DelegationState#PENDING} delegation. After this
   * method returns, the {@link Task#getDelegationState() delegationState} is
   * set to {@link DelegationState#RESOLVED}.
   * 
   * @param taskId
   * @param variables
   * @throws ProcessEngineException When no task exists with the given id.
   */
  void resolveTask(String taskId, Map<String, Object> variables);

  /**
   * Called when the task is successfully executed, 
   * and the required task parameters are given by the end-user.
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void complete(String taskId, Map<String, Object> variables);
  
  /**
   * Called when the task is successfully executed, 
   * and the required task paramaters are given by the end-user.
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   * @param localScope If true, the provided variables will be stored task-local, 
   * 									 instead of process instance wide (which is the default for {@link #complete(String, Map)}).
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void complete(String taskId, Map<String, Object> variables, boolean localScope);

  /**
   * Changes the assignee of the given task to the given userId.
   * No check is done whether the user is known by the identity component.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Transfers ownership of this task to another user.
   * No check is done whether the user is known by the identity component.
   * @param taskId id of the task, cannot be null.
   * @param userId of the person that is receiving ownership.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void setOwner(String taskId, String userId);
  
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
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #addGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of identity link is defined by the
   * given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void addUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Involves a group with a task. The type of identityLink is defined by the
   * given identityLink.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void addGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void deleteCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void deleteCandidateGroup(String taskId, String groupId);
  
  /**
   * Removes the association between a user and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void deleteUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Removes the association between a group and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Changes the priority of the task.
   * 
   * Authorization: actual owner / business admin
   * 
   * @param taskId id of the task, cannot be null.
   * @param priority the new priority for the task.
   * @throws ActivitiObjectNotFoundException when the task doesn't exist.
   */
  void setPriority(String taskId, int priority);

  /**
   * Changes the due date of the task
   *
   * @param taskId id of the task, cannot be null.
   * @param dueDate the new due date for the task
   * @throws ActivitiException when the task doesn't exist.
   */
  void setDueDate(String taskId, Date dueDate);

  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   */
  TaskQuery createTaskQuery();
  
  /**
   * Returns a new {@link NativeQuery} for tasks.
   */
  NativeTaskQuery createNativeTaskQuery();

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

  /** get a variables and search in the task scope and if available also the execution scopes. */
  <T> T getVariable(String taskId, String variableName, Class<T> variableClass);
  
  /** checks whether or not the task has a variable defined with the given name, in the task scope and if available also the execution scopes. */
  boolean hasVariable(String taskId, String variableName);

  /** checks whether or not the task has a variable defined with the given name. */
  Object getVariableLocal(String taskId, String variableName);

  /** checks whether or not the task has a variable defined with the given name. */
  <T> T getVariableLocal(String taskId, String variableName, Class<T> variableClass);
  
  /** checks whether or not the task has a variable defined with the given name, local task scope only. */
  boolean hasVariableLocal(String taskId, String variableName);

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
  
  /** get all variables and search only in the task scope. */
  List<VariableInstance> getVariableInstancesLocalByTaskIds(Set<String> taskIds);
  
  /**
   * Removes the variable from the task.
   * When the variable does not exist, nothing happens.
   */
  void removeVariable(String taskId, String variableName);

  /**
   * Removes the variable from the task (not considering parent scopes).
   * When the variable does not exist, nothing happens.
   */
  void removeVariableLocal(String taskId, String variableName);

  /**
   * Removes all variables in the given collection from the task.
   * Non existing variable names are simply ignored.
   */
  void removeVariables(String taskId, Collection<String> variableNames);

  /**
   * Removes all variables in the given collection from the task (not considering parent scopes).
   * Non existing variable names are simply ignored.
   */
  void removeVariablesLocal(String taskId, Collection<String> variableNames);

  /** Add a comment to a task and/or process instance. */
  Comment addComment(String taskId, String processInstanceId, String message);
  
  /** Add a comment to a task and/or process instance with a custom type. */
  Comment addComment(String taskId, String processInstanceId, String type, String message);
  
  /** 
   * Returns an individual comment with the given id. Returns null if no comment exists with the given id.
   */
  Comment getComment(String commentId);
  
  /** Removes all comments from the provided task and/or process instance*/
  void deleteComments(String taskId, String processInstanceId);
  
  /** 
   * Removes an individual comment with the given id.
   * @throws ActivitiObjectNotFoundException when no comment exists with the given id. 
   */
  void deleteComment(String commentId);

  /** The comments related to the given task. */
  List<Comment> getTaskComments(String taskId);
  
  /** The comments related to the given task of the given type. */
  List<Comment> getTaskComments(String taskId, String type);
  
  /** All comments of a given type. */
  List<Comment> getCommentsByType(String type);

  /** The all events related to the given task. */
  List<Event> getTaskEvents(String taskId);
  
  /** Returns an individual event with the given id. Returns null if no event exists with the given id. */
  Event getEvent(String eventId);

  /** The comments related to the given process instance. */
  List<Comment> getProcessInstanceComments(String processInstanceId);

  /** The comments related to the given process instance. */
  List<Comment> getProcessInstanceComments(String processInstanceId, String type);

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

  /** The list of subtasks for this parent task */
  List<Task> getSubTasks(String parentTaskId);
}
