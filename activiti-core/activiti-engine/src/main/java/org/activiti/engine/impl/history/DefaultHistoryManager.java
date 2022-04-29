/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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


package org.activiti.engine.impl.history;

import java.util.Date;
import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.task.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager class that centralises recording of all history-related operations that are originated from inside the engine.
 *
 */
public class DefaultHistoryManager extends AbstractManager implements HistoryManager {

  private static Logger log = LoggerFactory.getLogger(DefaultHistoryManager.class.getName());

  private HistoryLevel historyLevel;

  public DefaultHistoryManager(ProcessEngineConfigurationImpl processEngineConfiguration, HistoryLevel historyLevel) {
    super(processEngineConfiguration);
    this.historyLevel = historyLevel;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# isHistoryLevelAtLeast(org.activiti.engine.impl.history.HistoryLevel)
   */
  @Override
  public boolean isHistoryLevelAtLeast(HistoryLevel level) {
    if (log.isDebugEnabled()) {
      log.debug("Current history level: {}, level required: {}", historyLevel, level);
    }
    // Comparing enums actually compares the location of values declared in
    // the enum
    return historyLevel.isAtLeast(level);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#isHistoryEnabled ()
   */
  @Override
  public boolean isHistoryEnabled() {
    if (log.isDebugEnabled()) {
      log.debug("Current history level: {}", historyLevel);
    }
    return !historyLevel.equals(HistoryLevel.NONE);
  }

  // Process related history

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordProcessInstanceEnd(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void recordProcessInstanceEnd(String processInstanceId, String deleteReason, String activityId) {

    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().findById(processInstanceId);

      if (historicProcessInstance != null) {
        historicProcessInstance.markEnded(deleteReason);
        historicProcessInstance.setEndActivityId(activityId);

        // Fire event
        ActivitiEventDispatcher activitiEventDispatcher = getEventDispatcher();
        if (activitiEventDispatcher != null && activitiEventDispatcher.isEnabled()) {
          activitiEventDispatcher.dispatchEvent(
              ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_ENDED, historicProcessInstance));
        }

      }
    }
  }

  @Override
  public void recordProcessInstanceNameChange(String processInstanceId, String newName) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().findById(processInstanceId);

      if (historicProcessInstance != null) {
        historicProcessInstance.setName(newName);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordProcessInstanceStart (org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordProcessInstanceStart(ExecutionEntity processInstance, FlowElement startElement) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().create(processInstance);
      historicProcessInstance.setStartActivityId(startElement.getId());

      // Insert historic process-instance
      getHistoricProcessInstanceEntityManager().insert(historicProcessInstance, false);

      // Fire event
      ActivitiEventDispatcher activitiEventDispatcher = getEventDispatcher();
      if (activitiEventDispatcher != null && activitiEventDispatcher.isEnabled()) {
        activitiEventDispatcher.dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, historicProcessInstance));
      }

    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordSubProcessInstanceStart (org.activiti.engine.impl.persistence.entity.ExecutionEntity,
   * org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordSubProcessInstanceStart(ExecutionEntity parentExecution, ExecutionEntity subProcessInstance, FlowElement initialElement) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {

      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().create(subProcessInstance);

      // Fix for ACT-1728: startActivityId not initialized with subprocess instance
      if (historicProcessInstance.getStartActivityId() == null) {
        historicProcessInstance.setStartActivityId(initialElement.getId());
      }
      getHistoricProcessInstanceEntityManager().insert(historicProcessInstance, false);

      // Fire event
      ActivitiEventDispatcher activitiEventDispatcher = getEventDispatcher();
      if (activitiEventDispatcher != null && activitiEventDispatcher.isEnabled()) {
        activitiEventDispatcher.dispatchEvent(
            ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, historicProcessInstance));
      }

      HistoricActivityInstanceEntity activitiyInstance = findActivityInstance(parentExecution, false, true);
      if (activitiyInstance != null) {
        activitiyInstance.setCalledProcessInstanceId(subProcessInstance.getProcessInstanceId());
      }

    }
  }

  // Activity related history

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordActivityStart (org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordActivityStart(ExecutionEntity executionEntity) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      if (executionEntity.getActivityId() != null && executionEntity.getCurrentFlowElement() != null) {

        HistoricActivityInstanceEntity historicActivityInstanceEntity = null;

        // Historic activity instance could have been created (but only in cache, never persisted)
        // for example when submitting form properties
        HistoricActivityInstanceEntity historicActivityInstanceEntityFromCache =
            getHistoricActivityInstanceFromCache(executionEntity.getId(), executionEntity.getActivityId(), true);
        if (historicActivityInstanceEntityFromCache != null) {
          historicActivityInstanceEntity = historicActivityInstanceEntityFromCache;
        } else {
          historicActivityInstanceEntity = createHistoricActivityInstanceEntity(executionEntity);
        }

        // Fire event
        ActivitiEventDispatcher activitiEventDispatcher = getEventDispatcher();
        if (activitiEventDispatcher != null && activitiEventDispatcher.isEnabled()) {
          activitiEventDispatcher.dispatchEvent(
              ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED, historicActivityInstanceEntity));
        }

      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordActivityEnd (org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordActivityEnd(ExecutionEntity executionEntity, String deleteReason) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity, false, true);
      if (historicActivityInstance != null) {
        historicActivityInstance.markEnded(deleteReason);

        // Fire event
        ActivitiEventDispatcher activitiEventDispatcher = getEventDispatcher();
        if (activitiEventDispatcher != null && activitiEventDispatcher.isEnabled()) {
          activitiEventDispatcher.dispatchEvent(
              ActivitiEventBuilder.createEntityEvent(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED, historicActivityInstance));
        }
      }
    }
  }

  @Override
  public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution, boolean createOnNotFound, boolean endTimeMustBeNull) {
    String activityId = null;
    if (execution.getCurrentFlowElement() instanceof FlowNode) {
      activityId = execution.getCurrentFlowElement().getId();
    } else if (execution.getCurrentFlowElement() instanceof SequenceFlow
        && execution.getCurrentActivitiListener() == null) { // while executing sequence flow listeners, we don't want historic activities
      activityId = ( (SequenceFlow) (execution.getCurrentFlowElement())).getSourceFlowElement().getId();
    }

    if (activityId != null) {
      return findActivityInstance(execution, activityId, createOnNotFound, endTimeMustBeNull);
    }

    return null;
  }


  public HistoricActivityInstanceEntity findActivityInstance(ExecutionEntity execution, String activityId, boolean createOnNotFound, boolean endTimeMustBeNull) {

    // No use looking for the HistoricActivityInstance when no activityId is provided.
    if (activityId == null) {
      return null;
    }

    String executionId = execution.getId();

    // Check the cache
    HistoricActivityInstanceEntity historicActivityInstanceEntityFromCache =
        getHistoricActivityInstanceFromCache(executionId, activityId, endTimeMustBeNull);
    if (historicActivityInstanceEntityFromCache != null) {
      return historicActivityInstanceEntityFromCache;
    }

    // If the execution was freshly created, there is no need to check the database,
    // there can never be an entry for a historic activity instance with this execution id.
    if (!execution.isInserted() && !execution.isProcessInstanceType()) {

      // Check the database
      List<HistoricActivityInstanceEntity> historicActivityInstances = getHistoricActivityInstanceEntityManager()
          .findUnfinishedHistoricActivityInstancesByExecutionAndActivityId(executionId, activityId);

      if (historicActivityInstances.size() > 0) {
        return historicActivityInstances.get(0);
      }

    }

    if (execution.getParentId() != null) {
      HistoricActivityInstanceEntity historicActivityInstanceFromParent
        = findActivityInstance((ExecutionEntity) execution.getParent(), activityId, false, endTimeMustBeNull); // always false for create, we only check if it can be found
      if (historicActivityInstanceFromParent != null) {
        return historicActivityInstanceFromParent;
      }
    }

    if (createOnNotFound
        && activityId != null
        && ( (execution.getCurrentFlowElement() != null && execution.getCurrentFlowElement() instanceof FlowNode) || execution.getCurrentFlowElement() == null)) {
      return createHistoricActivityInstanceEntity(execution);
    }

    return null;
  }

  protected HistoricActivityInstanceEntity getHistoricActivityInstanceFromCache(String executionId, String activityId, boolean endTimeMustBeNull) {
    List<HistoricActivityInstanceEntity> cachedHistoricActivityInstances = getEntityCache().findInCache(HistoricActivityInstanceEntity.class);
    for (HistoricActivityInstanceEntity cachedHistoricActivityInstance : cachedHistoricActivityInstances) {
      if (activityId != null
          && activityId.equals(cachedHistoricActivityInstance.getActivityId())
          && (!endTimeMustBeNull || cachedHistoricActivityInstance.getEndTime() == null)) {
        if (executionId.equals(cachedHistoricActivityInstance.getExecutionId())) {
          return cachedHistoricActivityInstance;
        }
      }
    }

    return null;
  }

  protected HistoricActivityInstanceEntity createHistoricActivityInstanceEntity(ExecutionEntity execution) {
    IdGenerator idGenerator = getProcessEngineConfiguration().getIdGenerator();

    String processDefinitionId = execution.getProcessDefinitionId();
    String processInstanceId = execution.getProcessInstanceId();

    HistoricActivityInstanceEntity historicActivityInstance = getHistoricActivityInstanceEntityManager().create();
    historicActivityInstance.setId(idGenerator.getNextId());
    historicActivityInstance.setProcessDefinitionId(processDefinitionId);
    historicActivityInstance.setProcessInstanceId(processInstanceId);
    historicActivityInstance.setExecutionId(execution.getId());
    historicActivityInstance.setActivityId(execution.getActivityId());
    if (execution.getCurrentFlowElement() != null) {
      historicActivityInstance.setActivityName(execution.getCurrentFlowElement().getName());
      historicActivityInstance.setActivityType(parseActivityType(execution.getCurrentFlowElement()));
    }
    Date now = getClock().getCurrentTime();
    historicActivityInstance.setStartTime(now);

    // Inherit tenant id (if applicable)
    if (execution.getTenantId() != null) {
      historicActivityInstance.setTenantId(execution.getTenantId());
    }

    getHistoricActivityInstanceEntityManager().insert(historicActivityInstance);
    return historicActivityInstance;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordProcessDefinitionChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordProcessDefinitionChange(String processInstanceId, String processDefinitionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().findById(processInstanceId);
      if (historicProcessInstance != null) {
        historicProcessInstance.setProcessDefinitionId(processDefinitionId);
      }
    }
  }

  // Task related history

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskCreated (org.activiti.engine.impl.persistence.entity.TaskEntity,
   * org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void recordTaskCreated(TaskEntity task, ExecutionEntity execution) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().create(task, execution);
      getHistoricTaskInstanceEntityManager().insert(historicTaskInstance, false);
    }

    recordTaskId(task);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskAssignment (org.activiti.engine.impl.persistence.entity.TaskEntity)
   */
  @Override
  public void recordTaskAssignment(TaskEntity task) {
    ExecutionEntity executionEntity = task.getExecution();
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      if (executionEntity != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(executionEntity, false, true);
        if (historicActivityInstance != null) {
          historicActivityInstance.setAssignee(task.getAssignee());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskClaim (org.activiti.engine.impl.persistence.entity.TaskEntity)
   */

  @Override
  public void recordTaskClaim(TaskEntity task) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(task.getId());
      if (historicTaskInstance != null) {
        historicTaskInstance.setClaimTime(task.getClaimTime());
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskId (org.activiti.engine.impl.persistence.entity.TaskEntity)
   */
  @Override
  public void recordTaskId(TaskEntity task) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      ExecutionEntity execution = task.getExecution();
      if (execution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(execution, false, true);
        if (historicActivityInstance != null) {
          historicActivityInstance.setTaskId(task.getId());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskEnd (java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskEnd(String taskId, String deleteReason) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.markEnded(deleteReason);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskAssigneeChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskAssigneeChange(String taskId, String assignee) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setAssignee(assignee);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskOwnerChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskOwnerChange(String taskId, String owner) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setOwner(owner);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskNameChange (java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskNameChange(String taskId, String taskName) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setName(taskName);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskDescriptionChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskDescriptionChange(String taskId, String description) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setDescription(description);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskDueDateChange(java.lang.String, java.util.Date)
   */
  @Override
  public void recordTaskDueDateChange(String taskId, Date dueDate) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setDueDate(dueDate);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskPriorityChange(java.lang.String, int)
   */
  @Override
  public void recordTaskPriorityChange(String taskId, int priority) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setPriority(priority);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskCategoryChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskCategoryChange(String taskId, String category) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setCategory(category);
      }
    }
  }

  @Override
  public void recordTaskFormKeyChange(String taskId, String formKey) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setFormKey(formKey);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskParentTaskIdChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskParentTaskIdChange(String taskId, String parentTaskId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setParentTaskId(parentTaskId);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskExecutionIdChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskExecutionIdChange(String taskId, String executionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setExecutionId(executionId);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordTaskDefinitionKeyChange (org.activiti.engine.impl.persistence.entity.TaskEntity, java.lang.String)
   */
  @Override
  public void recordTaskDefinitionKeyChange(String taskId, String taskDefinitionKey) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setTaskDefinitionKey(taskDefinitionKey);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordTaskProcessDefinitionChange(java.lang.String, java.lang.String)
   */
  @Override
  public void recordTaskProcessDefinitionChange(String taskId, String processDefinitionId) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricTaskInstanceEntity historicTaskInstance = getHistoricTaskInstanceEntityManager().findById(taskId);
      if (historicTaskInstance != null) {
        historicTaskInstance.setProcessDefinitionId(processDefinitionId);
      }
    }
  }

  // Variables related history

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordVariableCreate (org.activiti.engine.impl.persistence.entity.VariableInstanceEntity)
   */
  @Override
  public void recordVariableCreate(VariableInstanceEntity variable) {
    // Historic variables
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
     getHistoricVariableInstanceEntityManager().copyAndInsert(variable);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordHistoricDetailVariableCreate (org.activiti.engine.impl.persistence.entity.VariableInstanceEntity,
   * org.activiti.engine.impl.persistence.entity.ExecutionEntity, boolean)
   */
  @Override
  public void recordHistoricDetailVariableCreate(VariableInstanceEntity variable, ExecutionEntity sourceActivityExecution, boolean useActivityId) {
    if (isHistoryLevelAtLeast(HistoryLevel.FULL)) {

      HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = getHistoricDetailEntityManager().copyAndInsertHistoricDetailVariableInstanceUpdateEntity(variable);

      if (useActivityId && sourceActivityExecution != null) {
        HistoricActivityInstanceEntity historicActivityInstance = findActivityInstance(sourceActivityExecution, false, false);
        if (historicActivityInstance != null) {
          historicVariableUpdate.setActivityInstanceId(historicActivityInstance.getId());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface#recordVariableUpdate (org.activiti.engine.impl.persistence.entity.VariableInstanceEntity)
   */
  @Override
  public void recordVariableUpdate(VariableInstanceEntity variable) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricVariableInstanceEntity historicProcessVariable = getEntityCache().findInCache(HistoricVariableInstanceEntity.class, variable.getId());
      if (historicProcessVariable == null) {
        historicProcessVariable = getHistoricVariableInstanceEntityManager().findHistoricVariableInstanceByVariableInstanceId(variable.getId());
      }

      if (historicProcessVariable != null) {
        getHistoricVariableInstanceEntityManager().copyVariableValue(historicProcessVariable, variable);
      } else {
        getHistoricVariableInstanceEntityManager().copyAndInsert(variable);
      }
    }
  }

  // Comment related history



  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# createIdentityLinkComment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create) {
    createIdentityLinkComment(taskId, userId, groupId, type, create, false);
  }

  @Override
  public void createUserIdentityLinkComment(String taskId, String userId, String type, boolean create) {
    createIdentityLinkComment(taskId, userId, null, type, create, false);
  }

  @Override
  public void createGroupIdentityLinkComment(String taskId, String groupId, String type, boolean create) {
    createIdentityLinkComment(taskId, null, groupId, type, create, false);
  }

  @Override
  public void createUserIdentityLinkComment(String taskId, String userId, String type, boolean create, boolean forceNullUserId) {
    createIdentityLinkComment(taskId, userId, null, type, create, forceNullUserId);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# createIdentityLinkComment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, boolean)
   */
  @Override
  public void createIdentityLinkComment(String taskId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    if (isHistoryEnabled()) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = getCommentEntityManager().create();
      comment.setUserId(authenticatedUserId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(getClock().getCurrentTime());
      comment.setTaskId(taskId);
      if (userId != null || forceNullUserId) {
        if (create) {
          comment.setAction(Event.ACTION_ADD_USER_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_USER_LINK);
        }
        comment.setMessage(new String[] { userId, type });
      } else {
        if (create) {
          comment.setAction(Event.ACTION_ADD_GROUP_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_GROUP_LINK);
        }
        comment.setMessage(new String[] { groupId, type });
      }

      getCommentEntityManager().insert(comment);
    }
  }

  @Override
  public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create) {
    createProcessInstanceIdentityLinkComment(processInstanceId, userId, groupId, type, create, false);
  }

  @Override
  public void createProcessInstanceIdentityLinkComment(String processInstanceId, String userId, String groupId, String type, boolean create, boolean forceNullUserId) {
    if (isHistoryEnabled()) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = getCommentEntityManager().create();
      comment.setUserId(authenticatedUserId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(getClock().getCurrentTime());
      comment.setProcessInstanceId(processInstanceId);
      if (userId != null || forceNullUserId) {
        if (create) {
          comment.setAction(Event.ACTION_ADD_USER_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_USER_LINK);
        }
        comment.setMessage(new String[] { userId, type });
      } else {
        if (create) {
          comment.setAction(Event.ACTION_ADD_GROUP_LINK);
        } else {
          comment.setAction(Event.ACTION_DELETE_GROUP_LINK);
        }
        comment.setMessage(new String[] { groupId, type });
      }
      getCommentEntityManager().insert(comment);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# createAttachmentComment(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  @Override
  public void createAttachmentComment(String taskId, String processInstanceId, String attachmentName, boolean create) {
    if (isHistoryEnabled()) {
      String userId = Authentication.getAuthenticatedUserId();
      CommentEntity comment = getCommentEntityManager().create();
      comment.setUserId(userId);
      comment.setType(CommentEntity.TYPE_EVENT);
      comment.setTime(getClock().getCurrentTime());
      comment.setTaskId(taskId);
      comment.setProcessInstanceId(processInstanceId);
      if (create) {
        comment.setAction(Event.ACTION_ADD_ATTACHMENT);
      } else {
        comment.setAction(Event.ACTION_DELETE_ATTACHMENT);
      }
      comment.setMessage(attachmentName);
      getCommentEntityManager().insert(comment);
    }
  }

  // Identity link related history
  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# recordIdentityLinkCreated (org.activiti.engine.impl.persistence.entity.IdentityLinkEntity)
   */
  @Override
  public void recordIdentityLinkCreated(IdentityLinkEntity identityLink) {
    // It makes no sense storing historic counterpart for an identity-link
    // that is related
    // to a process-definition only as this is never kept in history
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT) && (identityLink.getProcessInstanceId() != null || identityLink.getTaskId() != null)) {
      HistoricIdentityLinkEntity historicIdentityLinkEntity = getHistoricIdentityLinkEntityManager().create();
      historicIdentityLinkEntity.setId(identityLink.getId());
      historicIdentityLinkEntity.setGroupId(identityLink.getGroupId());
      historicIdentityLinkEntity.setProcessInstanceId(identityLink.getProcessInstanceId());
      historicIdentityLinkEntity.setTaskId(identityLink.getTaskId());
      historicIdentityLinkEntity.setType(identityLink.getType());
      historicIdentityLinkEntity.setUserId(identityLink.getUserId());
      getHistoricIdentityLinkEntityManager().insert(historicIdentityLinkEntity, false);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# deleteHistoricIdentityLink(java.lang.String)
   */
  @Override
  public void deleteHistoricIdentityLink(String id) {
    if (isHistoryLevelAtLeast(HistoryLevel.AUDIT)) {
      getHistoricIdentityLinkEntityManager().delete(id);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.activiti.engine.impl.history.HistoryManagerInterface# updateProcessBusinessKeyInHistory (org.activiti.engine.impl.persistence.entity.ExecutionEntity)
   */
  @Override
  public void updateProcessBusinessKeyInHistory(ExecutionEntity processInstance) {
    if (isHistoryEnabled()) {
      if (log.isDebugEnabled()) {
        log.debug("updateProcessBusinessKeyInHistory : {}", processInstance.getId());
      }
      if (processInstance != null) {
        HistoricProcessInstanceEntity historicProcessInstance = getHistoricProcessInstanceEntityManager().findById(processInstance.getId());
        if (historicProcessInstance != null) {
          historicProcessInstance.setBusinessKey(processInstance.getProcessInstanceBusinessKey());
          getHistoricProcessInstanceEntityManager().update(historicProcessInstance, false);
        }
      }
    }
  }

  @Override
  public void recordVariableRemoved(VariableInstanceEntity variable) {
    if (isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricVariableInstanceEntity historicProcessVariable = getEntityCache()
          .findInCache(HistoricVariableInstanceEntity.class, variable.getId());
      if (historicProcessVariable == null) {
        historicProcessVariable = getHistoricVariableInstanceEntityManager()
            .findHistoricVariableInstanceByVariableInstanceId(variable.getId());
      }

      if (historicProcessVariable != null) {
        getHistoricVariableInstanceEntityManager().delete(historicProcessVariable);
      }
    }
  }

  protected String parseActivityType(FlowElement element) {
    String elementType = element.getClass().getSimpleName();
    elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
    return elementType;
  }

  protected EntityCache getEntityCache() {
    return getSession(EntityCache.class);
  }

  public HistoryLevel getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(HistoryLevel historyLevel) {
    this.historyLevel = historyLevel;
  }

}
