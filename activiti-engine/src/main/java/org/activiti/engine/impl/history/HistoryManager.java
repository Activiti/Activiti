package org.activiti.engine.impl.history;

import java.util.Date;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.IdentityLink;

@Internal
public interface HistoryManager {

  /**
   * @return true, if the configured history-level is equal to OR set to a higher value than the given level.
   */
  boolean isHistoryLevelAtLeast(HistoryLevel level);

  /**
   * @return true, if history-level is configured to level other than "none".
   */
  boolean isHistoryEnabled();

  /**
   * Record a process-instance ended. Updates the historic process instance if activity history is enabled.
   */
  void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId);

  /**
   * Record a process-instance started and record start-event if activity history is enabled.
   */
  void recordProcessInstanceStart(ExecutionEntity processInstance, FlowElement startElement);

  /**
   * Record a process-instance name change.
   */
  void recordProcessInstanceNameChange(String processInstanceId, String newName);

  /**
   * Record a sub-process-instance started and alters the calledProcessinstanceId on the current active activity's historic counterpart. Only effective when activity history is enabled.
   */
  void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance, FlowElement initialFlowElement);

  /**
   * Record the start of an activity, if activity history is enabled.
   */
  void recordActivityStart(ExecutionEntity executionEntity);

  /**
   * Record the end of an activity, if activity history is enabled.
   */
  void recordActivityEnd(ExecutionEntity executionEntity, String deleteReason);

  /**
   * Finds the {@link HistoricActivityInstanceEntity} that is active in the given execution.
   */
  HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution, boolean createOnNotFound, boolean validateEndTimeNull);

  /**
   * Record a change of the process-definition id of a process instance, if activity history is enabled.
   */
  void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId);

  /**
   * Record the creation of a task, if audit history is enabled.
   */
  void recordTaskCreated(TaskEntity task, ExecutionEntity execution);

  /**
   * Record the assignment of task, if activity history is enabled.
   */
  void recordTaskAssignment(TaskEntity task);

  /**
   * record task instance claim time, if audit history is enabled
   *
   * @param task
   */

  void recordTaskClaim(TaskEntity task);

  /**
   * Record the id of a the task associated with a historic activity, if activity history is enabled.
   */
  void recordTaskId(TaskEntity task);

  /**
   * Record task as ended, if audit history is enabled.
   */
  void recordTaskEnd(String taskId, String deleteReason);

  /**
   * Record task assignee change, if audit history is enabled.
   */
  void recordTaskAssigneeChange(String taskId, String assignee);

  /**
   * Record task owner change, if audit history is enabled.
   */
  void recordTaskOwnerChange(String taskId, String owner);

  /**
   * Record task name change, if audit history is enabled.
   */
  void recordTaskNameChange(String taskId, String taskName);

  /**
   * Record task description change, if audit history is enabled.
   */
  void recordTaskDescriptionChange(String taskId, String description);

  /**
   * Record task due date change, if audit history is enabled.
   */
  void recordTaskDueDateChange(String taskId, Date dueDate);

  /**
   * Record task priority change, if audit history is enabled.
   */
  void recordTaskPriorityChange(String taskId, int priority);

  /**
   * Record task category change, if audit history is enabled.
   */
  void recordTaskCategoryChange(String taskId, String category);

  /**
   * Record task form key change, if audit history is enabled.
   */
  void recordTaskFormKeyChange(String taskId, String formKey);

  /**
   * Record task parent task id change, if audit history is enabled.
   */
  void recordTaskParentTaskIdChange(String taskId, String parentTaskId);

  /**
   * Record task execution id change, if audit history is enabled.
   */
  void recordTaskExecutionIdChange(String taskId, String executionId);

  /**
   * Record task definition key change, if audit history is enabled.
   */
  void recordTaskDefinitionKeyChange(String taskId, String taskDefinitionKey);
  
  /**
   * Record a change of the process-definition id of a task instance, if activity history is enabled.
   */
  public abstract void recordTaskProcessDefinitionChange(String taskId, String processDefinitionId);

  /**
   * Record a variable has been created, if audit history is enabled.
   */
  void recordVariableCreate(VariableInstanceEntity variable);

  /**
   * Record a variable has been created, if audit history is enabled.
   */
  void recordHistoricDetailVariableCreate(VariableInstanceEntity variable, ExecutionEntity sourceActivityExecution, boolean useActivityId);

  /**
   * Record a variable has been updated, if audit history is enabled.
   */
  void recordVariableUpdate(VariableInstanceEntity variable);

  /**
   * Record a variable has been deleted, if audit history is enabled.
   */
  void recordVariableRemoved(VariableInstanceEntity variable);

  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create);

  /**
   * Creates a new comment to indicate a new user {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createUserIdentityLinkComment(String taskId, String userId, String type, boolean create);

  /**
   * Creates a new comment to indicate a new group {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createGroupIdentityLinkComment(String taskId, String groupId, String type, boolean create);

  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create, boolean forceNullUserId);

  /**
   * Creates a new comment to indicate a new user {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createUserIdentityLinkComment(String taskId, String userId, String type, boolean create, boolean forceNullUserId);

  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create);

  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, if history is enabled.
   */
  void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create, boolean forceNullUserId);

  /**
   * Creates a new comment to indicate a new attachment has been created or deleted, if history is enabled.
   */
  void createAttachmentComment(String taskId, String processInstanceId, String attachmentName, boolean create);

  // Identity link related history
  /**
   * Record the creation of a new {@link IdentityLink}, if audit history is enabled.
   */
  void recordIdentityLinkCreated(IdentityLinkEntity identityLink);

  void deleteHistoricIdentityLink(String id);

  void updateProcessBusinessKeyInHistory(ExecutionEntity processInstance);

}
