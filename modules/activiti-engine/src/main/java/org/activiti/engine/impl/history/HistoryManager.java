package org.activiti.engine.impl.history;

import java.util.Date;
import java.util.Map;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.task.IdentityLink;

public interface HistoryManager extends Session {

	/**
	 * @return true, if the configured history-level is equal to OR set to
	 * a higher value than the given level.
	 */
	public abstract boolean isHistoryLevelAtLeast(HistoryLevel level);

	/**
	 * @return true, if history-level is configured to level other than "none".
	 */
	public abstract boolean isHistoryEnabled();

	/**
	 * Record a process-instance ended. Updates the historic process instance if activity history is enabled.
	 */
	public abstract void recordProcessInstanceEnd(String processInstanceId,
			String deleteReason, String activityId);

	/**
	 * Record a process-instance started and record start-event if activity history is enabled.
	 */
	public abstract void recordProcessInstanceStart(
			ExecutionEntity processInstance);

	/**
     * Record a process-instance name change.
     */
    public abstract void recordProcessInstanceNameChange(
        String processInstanceId, String newName);
	
	/**
	 * Record a sub-process-instance started and alters the calledProcessinstanceId
	 * on the current active activity's historic counterpart. Only effective when activity history is enabled.
	 */
	public abstract void recordSubProcessInstanceStart(
			ExecutionEntity parentExecution, ExecutionEntity subProcessInstance);

	/**
	 * Record the start of an activitiy, if activity history is enabled.
	 */
	public abstract void recordActivityStart(ExecutionEntity executionEntity);

	/**
	 * Record the end of an activitiy, if activity history is enabled.
	 */
	public abstract void recordActivityEnd(ExecutionEntity executionEntity);

	/**
	 * Record the end of a start event, if activity history is enabled.
	 */
	public abstract void recordStartEventEnded(ExecutionEntity execution, String activityId);

	/**
	 * Finds the {@link HistoricActivityInstanceEntity} that is active in the given
	 * execution. Uses the {@link DbSqlSession} cache to make sure the right instance
	 * is returned, regardless of whether or not entities have already been flushed to DB.
	 */
	public abstract HistoricActivityInstanceEntity findActivityInstance(
			ExecutionEntity execution);

	/**
	 * Replaces any open historic activityInstances' execution-id's to the id of the replaced
	 * execution, if activity history is enabled. 
	 */
	public abstract void recordExecutionReplacedBy(ExecutionEntity execution,
			InterpretableExecution replacedBy);

	/**
	 * Record a change of the process-definition id of a process instance, if activity history is enabled.
	 */
	public abstract void recordProcessDefinitionChange(
			String processInstanceId, String processDefinitionId);

	/**
	 * Record the creation of a task, if audit history is enabled.
	 */
	public abstract void recordTaskCreated(TaskEntity task,
			ExecutionEntity execution);

	/**
	 * Record the assignment of task, if activity history is enabled.
	 */
	public abstract void recordTaskAssignment(TaskEntity task);

	/**
	 * record task instance claim time, if audit history is enabled 
	 * @param taskId
	 */

	public abstract void recordTaskClaim(String taskId);

	/**
	 * Record the id of a the task associated with a historic activity, if activity history is enabled.
	 */
	public abstract void recordTaskId(TaskEntity task);

	/**
	 * Record task as ended, if audit history is enabled.
	 */
	public abstract void recordTaskEnd(String taskId, String deleteReason);

	/**
	 * Record task assignee change, if audit history is enabled.
	 */
	public abstract void recordTaskAssigneeChange(String taskId, String assignee);

	/**
	 * Record task owner change, if audit history is enabled.
	 */
	public abstract void recordTaskOwnerChange(String taskId, String owner);

	/**
	 * Record task name change, if audit history is enabled.
	 */
	public abstract void recordTaskNameChange(String taskId, String taskName);

	/**
	 * Record task description change, if audit history is enabled.
	 */
	public abstract void recordTaskDescriptionChange(String taskId,
			String description);

	/**
	 * Record task due date change, if audit history is enabled.
	 */
	public abstract void recordTaskDueDateChange(String taskId, Date dueDate);

	/**
	 * Record task priority change, if audit history is enabled.
	 */
	public abstract void recordTaskPriorityChange(String taskId, int priority);

	/**
	 * Record task category change, if audit history is enabled.
	 */
	public abstract void recordTaskCategoryChange(String taskId, String category);
	
	/**
	 * Record task form key change, if audit history is enabled.
	 */
	public abstract void recordTaskFormKeyChange(String taskId, String formKey);

	/**
	 * Record task parent task id change, if audit history is enabled.
	 */
	public abstract void recordTaskParentTaskIdChange(String taskId,
			String parentTaskId);

	/**
	 * Record task execution id change, if audit history is enabled.
	 */
	public abstract void recordTaskExecutionIdChange(String taskId, String executionId);

	/**
	 * Record task definition key change, if audit history is enabled.
	 */
	public abstract void recordTaskDefinitionKeyChange(TaskEntity task, String taskDefinitionKey);
	
	/**
	 * Record a change of the process-definition id of a task instance, if activity history is enabled.
	 */
	public abstract void recordTaskProcessDefinitionChange(String taskId, String processDefinitionId);

	/**
	 * Record a variable has been created, if audit history is enabled.
	 */
	public abstract void recordVariableCreate(VariableInstanceEntity variable);

	/**
	 * Record a variable has been created, if audit history is enabled.
	 */
	public abstract void recordHistoricDetailVariableCreate(
			VariableInstanceEntity variable,
			ExecutionEntity sourceActivityExecution, boolean useActivityId);

	/**
	 * Record a variable has been updated, if audit history is enabled.
	 */
	public abstract void recordVariableUpdate(VariableInstanceEntity variable);
	
	/**
	 * Record a variable has been deleted, if audit history is enabled.
	 */
	public abstract void recordVariableRemoved(VariableInstanceEntity variable);

	/**
	 * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
	 * if history is enabled. 
	 */
	public abstract void createIdentityLinkComment(String taskId,
			String userId, String groupId, String type, boolean create);

	/**
	 * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
	 * if history is enabled. 
	 */
	public abstract void createIdentityLinkComment(String taskId,
			String userId, String groupId, String type, boolean create,
			boolean forceNullUserId);

	/**
	 * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
	 * if history is enabled. 
	 */
	public abstract void createProcessInstanceIdentityLinkComment(String processInstanceId,
      String userId, String groupId, String type, boolean create);

	/**
	 * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
	 * if history is enabled. 
	 */
	public abstract void createProcessInstanceIdentityLinkComment(String processInstanceId,
      String userId, String groupId, String type, boolean create,
      boolean forceNullUserId);

	/**
	 * Creates a new comment to indicate a new attachment has been created or deleted, 
	 * if history is enabled. 
	 */
	public abstract void createAttachmentComment(String taskId,
			String processInstanceId, String attachmentName, boolean create);

	/**
	 * Report form properties submitted, if audit history is enabled.
	 */
	public abstract void reportFormPropertiesSubmitted(
			ExecutionEntity processInstance, Map<String, String> properties,
			String taskId);

	// Identity link related history
	/**
	 * Record the creation of a new {@link IdentityLink}, if audit history is enabled.
	 */
	public abstract void recordIdentityLinkCreated(
			IdentityLinkEntity identityLink);

	public abstract void deleteHistoricIdentityLink(String id);

	public abstract void updateProcessBusinessKeyInHistory(
			ExecutionEntity processInstance);

}