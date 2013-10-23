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

package org.activiti.engine.impl.history;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricFormPropertyEntity;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class that centralises recording of all history-related operations
 * that are originated from inside the engine.
 * 
 * @author Frederik Heremans
 */
public class HistoryManager extends AbstractManager {
  
  private static Logger log = LoggerFactory.getLogger(HistoryManager.class.getName());
  
  private HistoryLevel historyLevel;
  
  public HistoryManager() {
    this.historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();
  }
  
  /**
   * @return true, if the configured history-level is equal to OR set to
   * a higher value than the given level.
   */
  public boolean isHistoryLevelAtLeast(HistoryLevel level) {
    if(log.isDebugEnabled()) {
      log.debug("Current history level: {}, level required: {}", historyLevel, level);
    }
    // Comparing enums actually compares the location of values declared in the enum
    return historyLevel.isAtLeast(level);
  }
  
  /**
   * @return true, if history-level is configured to level other than "none".
   */
  public boolean isHistoryEnabled() {
    if(log.isDebugEnabled()) {
      log.debug("Current history level: {}", historyLevel);
    }
    return !historyLevel.equals(HistoryLevel.NONE);
  }
  
  // Process related history
  
  /**
   * Record a process-instance ended. Updates the historic process instance if activity history is enabled.
   */
  public void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId) {
    
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceManager()
              .findHistoricProcessInstance(processInstanceId);
      
      if (historicProcessInstance!=null) {
        historicProcessInstance.markEnded(deleteReason);
        historicProcessInstance.setEndActivityId(activityId);
      }
    }
  }
  
  /**
   * Record a process-instance started and record start-event if activity history is enabled.
   */
  public void recordProcessInstanceStart(ExecutionEntity processInstance) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity(processInstance);
      
      // Insert historic process-instance
      getDbSqlSession().insert(historicProcessInstance);
  
      // Also record the start-event manually, as there is no "start" activity history listener for this
      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
      
      String processDefinitionId = processInstance.getProcessDefinitionId();
      String processInstanceId = processInstance.getProcessInstanceId();
      String executionId = processInstance.getId();
  
      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
      historicActivityInstance.setId(idGenerator.getNextId());
      historicActivityInstance.setProcessDefinitionId(processDefinitionId);
      historicActivityInstance.setProcessInstanceId(processInstanceId);
      historicActivityInstance.setExecutionId(executionId);
      historicActivityInstance.setActivityId(processInstance.getActivityId());
      historicActivityInstance.setActivityName((String) processInstance.getActivity().getProperty("name"));
      historicActivityInstance.setActivityType((String) processInstance.getActivity().getProperty("type"));
      Date now = ClockUtil.getCurrentTime();
      historicActivityInstance.setStartTime(now);
      
      getDbSqlSession()
        .insert(historicActivityInstance);
    }
  }
  
  /**
   * Record a sub-process-instance started and alters the calledProcessinstanceId
   * on the current active activity's historic counterpart. Only effective when activity history is enabled.
   */
  public void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity((ExecutionEntity) subProcessInstance);
     
      ActivityImpl initialActivity = subProcessInstance.getActivity();
      // Fix for ACT-1728: startActivityId not initialized with subprocess-instance
      if(historicProcessInstance.getStartActivityId() == null) {
      	historicProcessInstance.setStartActivityId(subProcessInstance.getProcessDefinition().getInitial().getId());
      	initialActivity = subProcessInstance.getProcessDefinition().getInitial();
      }
      getDbSqlSession().insert(historicProcessInstance);
      
      
      HistoricActivityInstanceEntity activitiyInstance = findActivityInstance(parentExecution);
      if (activitiyInstance != null) {
        activitiyInstance.setCalledProcessInstanceId(subProcessInstance.getProcessInstanceId());
      }
      
      // Fix for ACT-1728: start-event not recorded for subprocesses
      IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
      
      // Also record the start-event manually, as there is no "start" activity history listener for this
      HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
      historicActivityInstance.setId(idGenerator.getNextId());
      historicActivityInstance.setProcessDefinitionId(subProcessInstance.getProcessDefinitionId());
      historicActivityInstance.setProcessInstanceId(subProcessInstance.getProcessInstanceId());
      historicActivityInstance.setExecutionId(subProcessInstance.getId());
      historicActivityInstance.setActivityId(initialActivity.getId());
      historicActivityInstance.setActivityName((String) initialActivity.getProperty("name"));
      historicActivityInstance.setActivityType((String) initialActivity.getProperty("type"));
      Date now = ClockUtil.getCurrentTime();
      historicActivityInstance.setStartTime(now);
      
      getDbSqlSession()
        .insert(historicActivityInstance);
    }
  }
  
  // Activity related history

  /**
   * Record the start of an activitiy, if activity history is enabled.
   */
  public void recordActivityStart(ExecutionEntity executionEntity) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
    	if(executionEntity.getActivity() != null) {
    		IdGenerator idGenerator = Context.getProcessEngineConfiguration().getIdGenerator();
    		
    		String processDefinitionId = executionEntity.getProcessDefinitionId();
    		String processInstanceId = executionEntity.getProcessInstanceId();
    		String executionId = executionEntity.getId();
    		
    		HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
    		historicActivityInstance.setId(idGenerator.getNextId());
    		historicActivityInstance.setProcessDefinitionId(processDefinitionId);
    		historicActivityInstance.setProcessInstanceId(processInstanceId);
    		historicActivityInstance.setExecutionId(executionId);
    		historicActivityInstance.setActivityId(executionEntity.getActivityId());
    		historicActivityInstance.setActivityName((String) executionEntity.getActivity().getProperty("name"));
    		historicActivityInstance.setActivityType((String) executionEntity.getActivity().getProperty("type"));
    		historicActivityInstance.setStartTime(ClockUtil.getCurrentTime());
    		
    		getDbSqlSession().insert(historicActivityInstance);
    	}
    }
  }
  
  /**
   * Record the end of an activitiy, if activity history is enabled.
   */
  public void recordActivityEnd(ExecutionEntity executionEntity) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
      if (historicActivityInstance!=null) {
        historicActivityInstance.markEnded(null);
      }
    }
  }
  
  /**
   * Record the end of a start-task, if activity history is enabled.
   */
  public void recordStartEventEnded(String executionId, String activityId) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      // Interrupted executions might not have an activityId set, skip recording history.
      if(activityId == null) {
        return;
      }
      
      // Search for the historic activity instance in the dbsqlsession cache, since process hasn't been persisted to db yet
      List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession().findInCache(HistoricActivityInstanceEntity.class);
      for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
        if ( executionId.equals(cachedHistoricActivityInstance.getExecutionId())
                && (activityId.equals(cachedHistoricActivityInstance.getActivityId()))
                && (cachedHistoricActivityInstance.getEndTime()==null)
                ) {
          cachedHistoricActivityInstance.markEnded(null);
          return;
        }
      }
    }
  }
  
  /**
   * Finds the {@link HistoricActivityInstanceEntity} that is active in the given
   * execution. Uses the {@link DbSqlSession} cache to make sure the right instance
   * is returned, regardless of whether or not entities have already been flushed to DB.
   */
  public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution) {
    String executionId = execution.getId();
    String activityId = execution.getActivityId();

    // search for the historic activity instance in the dbsqlsession cache
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession().findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
      if (executionId.equals(cachedHistoricActivityInstance.getExecutionId())
           && activityId != null
           && (activityId.equals(cachedHistoricActivityInstance.getActivityId()))
           && (cachedHistoricActivityInstance.getEndTime()==null)
         ) {
        return cachedHistoricActivityInstance;
      }
    }
    
    List<HistoricActivityInstance> historicActivityInstances = new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
      .executionId(executionId)
      .activityId(activityId)
      .unfinished()
      .listPage(0, 1);
    
    if (!historicActivityInstances.isEmpty()) {
      return (HistoricActivityInstanceEntity) historicActivityInstances.get(0);
    }
    
    if (execution.getParentId()!=null) {
      return findActivityInstance((ExecutionEntity) execution.getParent());
    }
    
    return null;
  }
  
  /**
   * Replaces any open historic activityInstances' execution-id's to the id of the replaced
   * execution, if activity history is enabled. 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void recordExecutionReplacedBy(ExecutionEntity execution, InterpretableExecution replacedBy) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      // Update the cached historic activity instances that are open
      List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getDbSqlSession().findInCache(HistoricActivityInstanceEntity.class);
      for (HistoricActivityInstanceEntity cachedHistoricActivityInstance: cachedHistoricActivityInstances) {
        if ( (cachedHistoricActivityInstance.getEndTime()==null)
             && (execution.getId().equals(cachedHistoricActivityInstance.getExecutionId())) 
           ) {
          cachedHistoricActivityInstance.setExecutionId(replacedBy.getId());
        }
      }
    
      // Update the persisted historic activity instances that are open
      List<HistoricActivityInstanceEntity> historicActivityInstances = (List) new HistoricActivityInstanceQueryImpl(Context.getCommandContext())
        .executionId(execution.getId())
        .unfinished()
        .list();
      for (HistoricActivityInstanceEntity historicActivityInstance: historicActivityInstances) {
        historicActivityInstance.setExecutionId(replacedBy.getId());
      }
    }
  }
  /**
   * Record a change of the process-definition id of a process instance, if activity history is enabled.
   */
  public void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceManager().findHistoricProcessInstance(processInstanceId);
      if(historicProcessInstance != null) {
        historicProcessInstance.setProcessDefinitionId(processDefinitionId);
      }
    }
  }
  
  
  // Task related history 
  
  /**
   * Record the creation of a task, if audit history is enabled.
   */
  public void recordTaskCreated(TaskEntity task, ExecutionEntity execution) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = new HistoricTaskInstanceEntity(task, execution);
      getDbSqlSession().insert(historicTaskInstance);
    }
  }
  
  /**
   * Record the assignment of task, if activity history is enabled.
   */
  public void recordTaskAssignment(TaskEntity task) {
    ExecutionEntity executionEntity = task.getExecution();
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      if (executionEntity != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity);
        if(historicActivityInstance != null) {
          historicActivityInstance.setAssignee(task.getAssignee());
        }
      }
    }
  }
  
  /**
   * record task instance claim time, if audit history is enabled 
   * @param taskId
   */

  public void recordTaskClaim(String taskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setClaimTime( ClockUtil.getCurrentTime());
      }
    }    
  }

  
  /**
   * Record the id of a the task associated with a historic activity, if activity history is enabled.
   */
  public void recordTaskId(TaskEntity task) {
    if(isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(execution);
        if(historicActivityInstance != null) {
          historicActivityInstance.setTaskId(task.getId());
        }
      }
    }
  }
  
  /**
   * Record task as ended, if audit history is enabled.
   */
  public void recordTaskEnd(String taskId, String deleteReason) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.markEnded(deleteReason);
      }
    }
  }
  
  /**
   * Record task assignee change, if audit history is enabled.
   */
  public void recordTaskAssigneeChange(String taskId, String assignee) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setAssignee(assignee);
      }
    }
  }
  
  /**
   * Record task owner change, if audit history is enabled.
   */
  public void recordTaskOwnerChange(String taskId, String owner) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setOwner(owner);
      }
    }
  }

  /**
   * Record task name change, if audit history is enabled.
   */
  public void recordTaskNameChange(String taskId, String taskName) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setName(taskName);
      }
    }
  }

  /**
   * Record task description change, if audit history is enabled.
   */
  public void recordTaskDescriptionChange(String taskId, String description) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDescription(description);
      }
    }
  }

  /**
   * Record task due date change, if audit history is enabled.
   */
  public void recordTaskDueDateChange(String taskId, Date dueDate) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setDueDate(dueDate);
      }
    }
  }

  /**
   * Record task priority change, if audit history is enabled.
   */
  public void recordTaskPriorityChange(String taskId, int priority) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setPriority(priority);
      }
    }
  }

  /**
   * Record task parent task id change, if audit history is enabled.
   */
  public void recordTaskParentTaskIdChange(String taskId, String parentTaskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setParentTaskId(parentTaskId);
      }
    }
  }

  /**
   * Record task execution id change, if audit history is enabled.
   */
  public void recordTaskExecutionIdChange(String taskId, String executionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
      if (historicTaskInstance!=null) {
        historicTaskInstance.setExecutionId(executionId);
      }
    }
  }
  
  /**
   * Record task definition key change, if audit history is enabled.
   */
  public void recordTaskDefinitionKeyChange(TaskEntity task, String taskDefinitionKey) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, task.getId());
      if (historicTaskInstance != null) {
        historicTaskInstance.setTaskDefinitionKey(taskDefinitionKey);
        
        if (taskDefinitionKey != null) {
          TaskFormHandler taskFormHandler = task.getTaskDefinition().getTaskFormHandler();
          if (taskFormHandler != null) {
            if (taskFormHandler.getFormKey() != null) {
              Object formValue = taskFormHandler.getFormKey().getValue(task.getExecution());
              if (formValue != null) {
                historicTaskInstance.setFormKey(formValue.toString());
              }
            }
          }
        }
      }
    }
  }
  
 
  // Variables related history
  
  /**
   * Record a variable has been created, if audit history is enabled.
   */
  public void recordVariableCreate(VariableInstanceEntity variable) {
    // Historic variables
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricVariableInstanceEntity.copyAndInsert(variable);
    }
  }
  
  /**
   * Record a variable has been created, if audit history is enabled.
   */
  public void recordHistoricDetailVariableCreate(VariableInstanceEntity variable, ExecutionEntity sourceActivityExecution, boolean useActivityId) {
    if (isHistoryLevelAtLeast(HistoryLevel.FULL)) {
      
      HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = 
          HistoricDetailVariableInstanceUpdateEntity.copyAndInsert(variable);
      
      if (useActivityId && sourceActivityExecution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(sourceActivityExecution); 
        if (historicActivityInstance!=null) {
          historicVariableUpdate.setActivityInstanceId(historicActivityInstance.getId());
        }
      }
    }
  }
  
  /**
   * Record a variable has been updated, if audit history is enabled.
   */
  public void recordVariableUpdate(VariableInstanceEntity variable) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricVariableInstanceEntity historicProcessVariable = 
          getDbSqlSession().findInCache(HistoricVariableInstanceEntity.class, variable.getId());
      if (historicProcessVariable==null) {
        historicProcessVariable = Context.getCommandContext()
                .getHistoricVariableInstanceEntityManager()
                .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
      }
      
      if (historicProcessVariable!=null) {
        historicProcessVariable.copyValue(variable);
      } else {
        HistoricVariableInstanceEntity.copyAndInsert(variable);
      }
    }
  }
  
  // Comment related history
  
  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
   * if history is enabled. 
   */
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create) {
    createIdentityLinkComment(taskId, userId, groupId, type, create, false);
  }
  
  /**
   * Creates a new comment to indicate a new {@link IdentityLink} has been created or deleted, 
   * if history is enabled. 
   */
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    if(isHistoryEnabled()) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = new CommentEntity();
      comment.setUserId(authenticatedUserId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(ClockUtil.getCurrentTime());
      comment.setTaskId(taskId);
      if (userId!=null || forceNullUserId) {
        if(create) {
          comment.setAction(Event.ACTION_ADD_USER_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_USER_LINK);
        }
        comment.setMessage(new String[]{userId, type});
      } else {
        if(create) {
          comment.setAction(Event.ACTION_ADD_GROUP_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_GROUP_LINK);
        }
        comment.setMessage(new String[]{groupId, type});
      }
      getSession(CommentEntityManager.class).insert(comment);
    }
  }
  
  /**
   * Creates a new comment to indicate a new attachment has been created or deleted, 
   * if history is enabled. 
   */
  public void createAttachmentComment(String taskId, String processInstanceId, String attachmentName, boolean create) {
    if (isHistoryEnabled()) {
      String userId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = new CommentEntity();
      comment.setUserId(userId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(ClockUtil.getCurrentTime());
      comment.setTaskId(taskId);
      comment.setProcessInstanceId(processInstanceId);
      if(create) {
        comment.setAction(Event.ACTION_ADD_ATTACHMENT);
      } else {
        comment.setAction(Event.ACTION_DELETE_ATTACHMENT);
      }
      comment.setMessage(attachmentName);
      getSession(CommentEntityManager.class).insert(comment);
    }
  }

  /**
   * Report form properties submitted, if audit history is enabled.
   */
  public void reportFormPropertiesSubmitted(ExecutionEntity processInstance, Map<String, String> properties, String taskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      for (String propertyId: properties.keySet()) {
        String propertyValue = properties.get(propertyId);
        HistoricFormPropertyEntity historicFormProperty = new HistoricFormPropertyEntity(processInstance, propertyId, propertyValue, taskId);
        getDbSqlSession().insert(historicFormProperty);
      }
    }
  }
  
  // Identity link related history
  /**
   * Record the creation of a new {@link IdentityLink}, if audit history is enabled.
   */
  public void recordIdentityLinkCreated(IdentityLinkEntity identityLink) {
    // It makes no sense storing historic counterpart for an identity-link that is related
    // to a process-definition only as this is never kept in history
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT) && (identityLink.getProcessInstanceId() != null || identityLink.getTaskId() != null)) {
      HistoricIdentityLinkEntity historicIdentityLinkEntity = new HistoricIdentityLinkEntity(identityLink);
      getDbSqlSession().insert(historicIdentityLinkEntity);
    }
  }

  public void deleteHistoricIdentityLink(String id) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLink(id);
    }
  }
  
  public void updateProcessBusinessKeyInHistory(ExecutionEntity processInstance) {
    if (isHistoryEnabled()) {
      if(log.isDebugEnabled()) {
        log.debug("updateProcessBusinessKeyInHistory : {}", processInstance.getId());
      }
      if (processInstance != null) {
        HistoricProcessInstanceEntity historicProcessInstance = getDbSqlSession().selectById(HistoricProcessInstanceEntity.class, processInstance.getId());
        if (historicProcessInstance != null) {
          historicProcessInstance.setBusinessKey(processInstance.getProcessBusinessKey());
          getDbSqlSession().update(historicProcessInstance);
        }
      }
    }
  }
}
