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


package org.activiti.engine.impl.persistence.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiProcessCancelledEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.CountingExecutionEntity;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.ExecutionDataManager;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**


 */
public class ExecutionEntityManagerImpl extends AbstractEntityManager<ExecutionEntity> implements ExecutionEntityManager {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionEntityManagerImpl.class);

  protected ExecutionDataManager executionDataManager;

  public ExecutionEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, ExecutionDataManager executionDataManager) {
    super(processEngineConfiguration);
    this.executionDataManager = executionDataManager;
  }

  @Override
  protected DataManager<ExecutionEntity> getDataManager() {
    return executionDataManager;
  }

  // Overriding the default delete methods to set the 'isDeleted' flag

  @Override
  public void delete(ExecutionEntity entity) {
    delete(entity, true);
  }

  @Override
  public void delete(ExecutionEntity entity, boolean fireDeleteEvent) {
    super.delete(entity, fireDeleteEvent);
    entity.setDeleted(true);
  }

  // FIND METHODS

  @Override
  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
    return executionDataManager.findSubProcessInstanceBySuperExecutionId(superExecutionId);
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
    return executionDataManager.findChildExecutionsByParentExecutionId(parentExecutionId);
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByProcessInstanceId(String processInstanceId) {
    return executionDataManager.findChildExecutionsByProcessInstanceId(processInstanceId);
  }

  @Override
  public List<ExecutionEntity> findExecutionsByParentExecutionAndActivityIds(final String parentExecutionId, final Collection<String> activityIds) {
    return executionDataManager.findExecutionsByParentExecutionAndActivityIds(parentExecutionId, activityIds);
  }

  @Override
  public long findExecutionCountByQueryCriteria(ExecutionQueryImpl executionQuery) {
    return executionDataManager.findExecutionCountByQueryCriteria(executionQuery);
  }

  @Override
  public List<ExecutionEntity> findExecutionsByQueryCriteria(ExecutionQueryImpl executionQuery, Page page) {
    return executionDataManager.findExecutionsByQueryCriteria(executionQuery, page);
  }

  @Override
  public long findProcessInstanceCountByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return executionDataManager.findProcessInstanceCountByQueryCriteria(executionQuery);
  }

  @Override
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return executionDataManager.findProcessInstanceByQueryCriteria(executionQuery);
  }

  @Override
  public ExecutionEntity findByRootProcessInstanceId(String rootProcessInstanceId) {
    List<ExecutionEntity> executions = executionDataManager.findExecutionsByRootProcessInstanceId(rootProcessInstanceId);
    return processExecutionTree(rootProcessInstanceId, executions);

  }

  /**
   * Processes a collection of {@link ExecutionEntity} instances, which form on execution tree.
   * All the executions share the same rootProcessInstanceId (which is provided).
   * The return value will be the root {@link ExecutionEntity} instance, with all child {@link ExecutionEntity}
   * instances populated and set using the {@link ExecutionEntity} instances from the provided collections
   */
  protected ExecutionEntity processExecutionTree(String rootProcessInstanceId, List<ExecutionEntity> executions) {
    ExecutionEntity rootExecution = null;

    // Collect executions
    Map<String, ExecutionEntity> executionMap = new HashMap<String, ExecutionEntity>(executions.size());
    for (ExecutionEntity executionEntity : executions) {
      if (executionEntity.getId().equals(rootProcessInstanceId)) {
        rootExecution = executionEntity;
      }
      executionMap.put(executionEntity.getId(), executionEntity);
    }

    // Set relationships
    for (ExecutionEntity executionEntity : executions) {

      // Root process instance relationship
      if (executionEntity.getRootProcessInstanceId() != null) {
        executionEntity.setRootProcessInstance(executionMap.get(executionEntity.getRootProcessInstanceId()));
      }

      // Process instance relationship
      if (executionEntity.getProcessInstanceId() != null) {
        executionEntity.setProcessInstance(executionMap.get(executionEntity.getProcessInstanceId()));
      }

      // Parent - child relationship
      if (executionEntity.getParentId() != null) {
        ExecutionEntity parentExecutionEntity = executionMap.get(executionEntity.getParentId());
        executionEntity.setParent(parentExecutionEntity);
        parentExecutionEntity.addChildExecution(executionEntity);
      }

      // Super - sub execution relationship
      if (executionEntity.getSuperExecution() != null) {
        ExecutionEntity superExecutionEntity = executionMap.get(executionEntity.getSuperExecutionId());
        executionEntity.setSuperExecution(superExecutionEntity);
        superExecutionEntity.setSubProcessInstance(executionEntity);
      }

    }
    return rootExecution;
  }

  @Override
  public List<ProcessInstance> findProcessInstanceAndVariablesByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return executionDataManager.findProcessInstanceAndVariablesByQueryCriteria(executionQuery);
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByProcessInstanceId(final String processInstanceId) {
    return executionDataManager.findInactiveExecutionsByProcessInstanceId(processInstanceId);
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByActivityIdAndProcessInstanceId(final String activityId, final String processInstanceId) {
   return executionDataManager.findInactiveExecutionsByActivityIdAndProcessInstanceId(activityId, processInstanceId);
  }

  @Override
  public List<Execution> findExecutionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return executionDataManager.findExecutionsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public List<ProcessInstance> findProcessInstanceByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return executionDataManager.findProcessInstanceByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findExecutionCountByNativeQuery(Map<String, Object> parameterMap) {
    return executionDataManager.findExecutionCountByNativeQuery(parameterMap);
  }

  // CREATE METHODS

  @Override
  public ExecutionEntity createProcessInstanceExecution(ProcessDefinition processDefinition, String businessKey, String tenantId, String initiatorVariableName) {
    ExecutionEntity processInstanceExecution = executionDataManager.create();

    if (isExecutionRelatedEntityCountEnabledGlobally()) {
      ((CountingExecutionEntity) processInstanceExecution).setCountEnabled(true);
    }

    processInstanceExecution.setProcessDefinitionId(processDefinition.getId());
    processInstanceExecution.setProcessDefinitionKey(processDefinition.getKey());
    processInstanceExecution.setProcessDefinitionName(processDefinition.getName());
    processInstanceExecution.setProcessDefinitionVersion(processDefinition.getVersion());
    processInstanceExecution.setAppVersion(processDefinition.getAppVersion());
    processInstanceExecution.setBusinessKey(businessKey);
    processInstanceExecution.setScope(true); // process instance is always a scope for all child executions

    // Inherit tenant id (if any)
    if (tenantId != null) {
      processInstanceExecution.setTenantId(tenantId);
    }

    String authenticatedUserId = Authentication.getAuthenticatedUserId();

    processInstanceExecution.setStartUserId(authenticatedUserId);

    // Store in database
    insert(processInstanceExecution, false);

    if (initiatorVariableName != null) {
      processInstanceExecution.setVariable(initiatorVariableName, authenticatedUserId);
    }

    // Need to be after insert, cause we need the id
    processInstanceExecution.setProcessInstanceId(processInstanceExecution.getId());
    processInstanceExecution.setRootProcessInstanceId(processInstanceExecution.getId());
    if (authenticatedUserId != null) {
      getIdentityLinkEntityManager().addIdentityLink(processInstanceExecution, authenticatedUserId, null, IdentityLinkType.STARTER);
    }

    // Fire events
    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processInstanceExecution));
    }

    return processInstanceExecution;
  }

  public ExecutionEntity updateProcessInstanceStartDate(ExecutionEntity processInstanceExecution) {
      processInstanceExecution.setStartTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime());
      return update(processInstanceExecution);
  }

  /**
   * Creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   */
  @Override
  public ExecutionEntity createChildExecution(ExecutionEntity parentExecutionEntity) {
    ExecutionEntity childExecution = executionDataManager.create();
    inheritCommonProperties(parentExecutionEntity, childExecution);
    childExecution.setParent(parentExecutionEntity);
    childExecution.setProcessDefinitionId(parentExecutionEntity.getProcessDefinitionId());
    childExecution.setProcessDefinitionKey(parentExecutionEntity.getProcessDefinitionKey());
    childExecution.setProcessInstanceId(parentExecutionEntity.getProcessInstanceId() != null
        ? parentExecutionEntity.getProcessInstanceId() : parentExecutionEntity.getId());
    childExecution.setParentProcessInstanceId(parentExecutionEntity.getParentProcessInstanceId());
    childExecution.setAppVersion(parentExecutionEntity.getAppVersion());
    childExecution.setScope(false);

    // manage the bidirectional parent-child relation
    parentExecutionEntity.addChildExecution(childExecution);

    // Insert the child execution
    insert(childExecution, false);

    if (logger.isDebugEnabled()) {
      logger.debug("Child execution {} created with parent {}", childExecution, parentExecutionEntity.getId());
    }

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, childExecution));
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, childExecution));
    }

    return childExecution;
  }

  @Override
  public ExecutionEntity createSubprocessInstance(ProcessDefinition processDefinition, ExecutionEntity superExecutionEntity, String businessKey) {
    ExecutionEntity subProcessInstance = executionDataManager.create();
    inheritCommonProperties(superExecutionEntity, subProcessInstance);
    subProcessInstance.setProcessDefinitionId(processDefinition.getId());
    subProcessInstance.setProcessDefinitionKey(processDefinition.getKey());
    subProcessInstance.setProcessDefinitionName(processDefinition.getName());
    subProcessInstance.setProcessDefinitionVersion(processDefinition.getVersion());
    subProcessInstance.setName(superExecutionEntity.getProcessInstance().getName());
    subProcessInstance.setSuperExecution(superExecutionEntity);
    subProcessInstance.setRootProcessInstanceId(superExecutionEntity.getRootProcessInstanceId());
    subProcessInstance.setScope(true); // process instance is always a scope for all child executions
    subProcessInstance.setStartUserId(Authentication.getAuthenticatedUserId());
    subProcessInstance.setBusinessKey(businessKey);
    subProcessInstance.setAppVersion(processDefinition.getAppVersion());

    // Store in database
    insert(subProcessInstance, false);

    if (logger.isDebugEnabled()) {
      logger.debug("Child execution {} created with super execution {}", subProcessInstance, superExecutionEntity.getId());
    }

    subProcessInstance.setProcessInstanceId(subProcessInstance.getId());
    superExecutionEntity.setSubProcessInstance(subProcessInstance);

    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, subProcessInstance));
    }

    return subProcessInstance;
  }

  protected void inheritCommonProperties(ExecutionEntity parentExecutionEntity, ExecutionEntity childExecution) {

    // Inherits the 'count' feature from the parent.
    // If the parent was not 'counting', we can't make the child 'counting' again.
    if (parentExecutionEntity instanceof CountingExecutionEntity) {
      CountingExecutionEntity countingParentExecutionEntity = (CountingExecutionEntity) parentExecutionEntity;
      ((CountingExecutionEntity) childExecution).setCountEnabled(countingParentExecutionEntity.isCountEnabled());
    }

    childExecution.setRootProcessInstanceId(parentExecutionEntity.getRootProcessInstanceId());
    childExecution.setActive(true);
    childExecution.setStartTime(processEngineConfiguration.getClock().getCurrentTime());

    if (parentExecutionEntity.getTenantId() != null) {
      childExecution.setTenantId(parentExecutionEntity.getTenantId());
    }

  }

  // UPDATE METHODS

  @Override
  public void updateExecutionTenantIdForDeployment(String deploymentId, String newTenantId) {
    executionDataManager.updateExecutionTenantIdForDeployment(deploymentId, newTenantId);
  }

  // DELETE METHODS

  @Override
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade) {
    List<String> processInstanceIds = executionDataManager.findProcessInstanceIdsByProcessDefinitionId(processDefinitionId);

    for (String processInstanceId : processInstanceIds) {
      deleteProcessInstance(processInstanceId, deleteReason, cascade);
    }

    if (cascade) {
      getHistoricProcessInstanceEntityManager().deleteHistoricProcessInstanceByProcessDefinitionId(processDefinitionId);
    }
  }

  @Override
  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade) {
    ExecutionEntity execution = findById(processInstanceId);

    if (execution == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id '" + processInstanceId + "'", ProcessInstance.class);
    }

    deleteProcessInstanceCascade(execution, deleteReason, cascade);
  }

  protected void deleteProcessInstanceCascade(ExecutionEntity execution, String deleteReason, boolean deleteHistory) {

    // fill default reason if none provided
    if (deleteReason == null) {
      deleteReason = DeleteReason.PROCESS_INSTANCE_DELETED;
    }

    for (ExecutionEntity subExecutionEntity : execution.getExecutions()) {
      if (subExecutionEntity.isMultiInstanceRoot()) {
        for (ExecutionEntity miExecutionEntity : subExecutionEntity.getExecutions()) {
          if (miExecutionEntity.getSubProcessInstance() != null) {
            deleteProcessInstanceCascade(miExecutionEntity.getSubProcessInstance(), deleteReason, deleteHistory);
          }
        }

      } else if (subExecutionEntity.getSubProcessInstance() != null) {
        deleteProcessInstanceCascade(subExecutionEntity.getSubProcessInstance(), deleteReason, deleteHistory);
      }
    }

    getTaskEntityManager().deleteTasksByProcessInstanceId(execution.getId(), deleteReason, deleteHistory);

      dispatchProcessCancelledEvent(execution, deleteReason);

      // delete the execution BEFORE we delete the history, otherwise we will
    // produce orphan HistoricVariableInstance instances

    ExecutionEntity processInstanceExecutionEntity = execution.getProcessInstance();
    if (processInstanceExecutionEntity == null) {
      return;
    }

    List<ExecutionEntity> childExecutions = collectChildren(execution.getProcessInstance());
    for (int i=childExecutions.size()-1; i>=0; i--) {
      ExecutionEntity childExecutionEntity = childExecutions.get(i);
      deleteExecutionAndRelatedData(childExecutionEntity, deleteReason);
    }

    deleteExecutionAndRelatedData(execution, deleteReason);

    if (deleteHistory) {
      getHistoricProcessInstanceEntityManager().delete(execution.getId());
    }

    getHistoryManager().recordProcessInstanceEnd(processInstanceExecutionEntity.getId(), deleteReason, null);
    processInstanceExecutionEntity.setDeleted(true);
  }

    private void dispatchProcessCancelledEvent(ExecutionEntity execution, String deleteReason) {
        if (getEventDispatcher().isEnabled()) {
            ProcessInstance processInstance = execution.getProcessInstance();
            ActivitiProcessCancelledEvent processCancelledEvent = ActivitiEventBuilder
                .createProcessCancelledEvent(processInstance, deleteReason);
            getEventDispatcher().dispatchEvent(processCancelledEvent);
        }
    }

    @Override
  public void deleteExecutionAndRelatedData(ExecutionEntity executionEntity, String deleteReason) {
    getHistoryManager().recordActivityEnd(executionEntity, deleteReason);
    deleteDataForExecution(executionEntity, deleteReason);
    delete(executionEntity);
  }

  @Override
  public void cancelExecutionAndRelatedData(ExecutionEntity executionEntity, String deleteReason) {
    getHistoryManager().recordActivityEnd(executionEntity, deleteReason);
    cancelDataForExecution(executionEntity, deleteReason);
    delete(executionEntity);
  }

  @Override
  public void deleteProcessInstanceExecutionEntity(String processInstanceId,
      String currentFlowElementId, String deleteReason, boolean cascade, boolean cancel) {

    ExecutionEntity processInstanceEntity = findById(processInstanceId);

    if (processInstanceEntity == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id '" + processInstanceId + "'", ProcessInstance.class);
    }

    if (processInstanceEntity.isDeleted()) {
      return;
    }

    // Call activities
    for (ExecutionEntity subExecutionEntity : processInstanceEntity.getExecutions()) {
      if (subExecutionEntity.getSubProcessInstance() != null &&  !subExecutionEntity.isEnded()) {
        deleteProcessInstanceCascade(subExecutionEntity.getSubProcessInstance(), deleteReason, cascade);
      }
    }

    // delete event scope executions
    for (ExecutionEntity childExecution : processInstanceEntity.getExecutions()) {
      if (childExecution.isEventScope()) {
        deleteExecutionAndRelatedData(childExecution, null);
      }
    }

    if(cancel) {
        cancelChildExecutions(processInstanceEntity, deleteReason);
        cancelExecutionAndRelatedData(processInstanceEntity, deleteReason);
    } else {
        deleteChildExecutions(processInstanceEntity, deleteReason);
        deleteExecutionAndRelatedData(processInstanceEntity, deleteReason);
    }

    if (getEventDispatcher().isEnabled()) {
    	if (!cancel) {
    		getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.PROCESS_COMPLETED, processInstanceEntity));
    	} else {
    		getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createProcessCancelledEvent(processInstanceEntity, deleteReason));
    	}
    }

    // TODO: what about delete reason?
    getHistoryManager().recordProcessInstanceEnd(processInstanceEntity.getId(), deleteReason, currentFlowElementId);
    processInstanceEntity.setDeleted(true);
  }

  @Override
  public void deleteChildExecutions(ExecutionEntity executionEntity, String deleteReason) {

    // The children of an execution for a tree. For correct deletions
    // (taking care of foreign keys between child-parent)
    // the leafs of this tree must be deleted first before the parents elements.

    List<? extends ExecutionEntity> childExecutions = collectChildren(executionEntity);
    for (int i = childExecutions.size() - 1; i>= 0; i--) {
      ExecutionEntity childExecutionEntity = childExecutions.get(i);
      if (!childExecutionEntity.isEnded()) {
        deleteExecutionAndRelatedData(childExecutionEntity, deleteReason);
      }
    }

  }

  @Override
  public void cancelChildExecutions(ExecutionEntity executionEntity, String deleteReason) {

    // The children of an execution for a tree. For correct deletions
    // (taking care of foreign keys between child-parent)
    // the leafs of this tree must be deleted first before the parents elements.

    List<? extends ExecutionEntity> childExecutions = collectChildren(executionEntity);
    for (int i = childExecutions.size() - 1; i>= 0; i--) {
      ExecutionEntity childExecutionEntity = childExecutions.get(i);
      if (!childExecutionEntity.isEnded()) {
        cancelExecutionAndRelatedData(childExecutionEntity, deleteReason);
      }
    }

  }

  public List<ExecutionEntity> collectChildren(ExecutionEntity executionEntity) {
    List<ExecutionEntity> childExecutions = new ArrayList<ExecutionEntity>();
    collectChildren(executionEntity, childExecutions);
    return childExecutions;
  }

  protected void collectChildren(ExecutionEntity executionEntity, List<ExecutionEntity> collectedChildExecution) {
    List<ExecutionEntity> childExecutions = (List<ExecutionEntity>) executionEntity.getExecutions();
    if (childExecutions != null && childExecutions.size() > 0) {
      for (ExecutionEntity childExecution : childExecutions) {
        if (!childExecution.isDeleted()) {
          collectedChildExecution.add(childExecution);
          collectChildren(childExecution, collectedChildExecution);
        }
      }
    }

    ExecutionEntity subProcessInstance = executionEntity.getSubProcessInstance();
    if (subProcessInstance != null && !subProcessInstance.isDeleted()) {
      collectedChildExecution.add(subProcessInstance);
      collectChildren(subProcessInstance, collectedChildExecution);
    }
  }

  @Override
  public ExecutionEntity findFirstScope(ExecutionEntity executionEntity) {
    ExecutionEntity currentExecutionEntity = executionEntity;
    while (currentExecutionEntity != null) {
      if (currentExecutionEntity.isScope()) {
        return currentExecutionEntity;
      }

      ExecutionEntity parentExecutionEntity = currentExecutionEntity.getParent();
      if (parentExecutionEntity == null) {
        parentExecutionEntity = currentExecutionEntity.getSuperExecution();
      }
      currentExecutionEntity = parentExecutionEntity;
    }
    return null;
  }

  @Override
  public ExecutionEntity findFirstMultiInstanceRoot(ExecutionEntity executionEntity) {
    ExecutionEntity currentExecutionEntity = executionEntity;
    while (currentExecutionEntity != null) {
      if (currentExecutionEntity.isMultiInstanceRoot()) {
        return currentExecutionEntity;
      }

      ExecutionEntity parentExecutionEntity = currentExecutionEntity.getParent();
      if (parentExecutionEntity == null) {
        parentExecutionEntity = currentExecutionEntity.getSuperExecution();
      }
      currentExecutionEntity = parentExecutionEntity;
    }
    return null;
  }

  private void deleteExecutionEntity(ExecutionEntity executionEntity, String deleteReason) {
    // To start, deactivate the current incoming execution
    executionEntity.setEnded(true);
    executionEntity.setActive(false);

    boolean enableExecutionRelationshipCounts = isExecutionRelatedEntityCountEnabled(executionEntity);

    if (executionEntity.getId().equals(executionEntity.getProcessInstanceId())
        && (!enableExecutionRelationshipCounts
            || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getIdentityLinkCount() > 0))) {
      IdentityLinkEntityManager identityLinkEntityManager = getIdentityLinkEntityManager();
      Collection<IdentityLinkEntity> identityLinks = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(executionEntity.getProcessInstanceId());
      for (IdentityLinkEntity identityLink : identityLinks) {
        identityLinkEntityManager.delete(identityLink);
      }
    }

    // Get variables related to execution and delete them
    if (!enableExecutionRelationshipCounts ||
        (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getVariableCount() > 0)) {
      Collection<VariableInstance> executionVariables = executionEntity.getVariableInstancesLocal().values();
      for (VariableInstance variableInstance : executionVariables) {
        if (variableInstance instanceof VariableInstanceEntity) {
          VariableInstanceEntity variableInstanceEntity = (VariableInstanceEntity) variableInstance;

          VariableInstanceEntityManager variableInstanceEntityManager = getVariableInstanceEntityManager();
          variableInstanceEntityManager.delete(variableInstanceEntity);
          if (variableInstanceEntity.getByteArrayRef() != null && variableInstanceEntity.getByteArrayRef().getId() != null) {
            getByteArrayEntityManager().deleteByteArrayById(variableInstanceEntity.getByteArrayRef().getId());
          }
        }
      }
    }

    // Delete jobs

    if (!enableExecutionRelationshipCounts
        || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getTimerJobCount() > 0)) {
      TimerJobEntityManager timerJobEntityManager = getTimerJobEntityManager();
      Collection<TimerJobEntity> timerJobsForExecution = timerJobEntityManager.findJobsByExecutionId(executionEntity.getId());
      for (TimerJobEntity job : timerJobsForExecution) {
        timerJobEntityManager.delete(job);
        if (getEventDispatcher().isEnabled()) {
          getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
        }
      }
    }

    if (!enableExecutionRelationshipCounts
        || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getJobCount() > 0)) {
      JobEntityManager jobEntityManager = getJobEntityManager();
      Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(executionEntity.getId());
      for (JobEntity job : jobsForExecution) {
        getJobEntityManager().delete(job);
        if (getEventDispatcher().isEnabled()) {
          getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
        }
      }
    }

    if (!enableExecutionRelationshipCounts
        || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getSuspendedJobCount() > 0)) {
      SuspendedJobEntityManager suspendedJobEntityManager = getSuspendedJobEntityManager();
      Collection<SuspendedJobEntity> suspendedJobsForExecution = suspendedJobEntityManager.findJobsByExecutionId(executionEntity.getId());
      for (SuspendedJobEntity job : suspendedJobsForExecution) {
        suspendedJobEntityManager.delete(job);
        if (getEventDispatcher().isEnabled()) {
          getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
        }
      }
    }

    if (!enableExecutionRelationshipCounts
        || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getDeadLetterJobCount() > 0)) {
      DeadLetterJobEntityManager deadLetterJobEntityManager = getDeadLetterJobEntityManager();
      Collection<DeadLetterJobEntity> deadLetterJobsForExecution = deadLetterJobEntityManager.findJobsByExecutionId(executionEntity.getId());
      for (DeadLetterJobEntity job : deadLetterJobsForExecution) {
        deadLetterJobEntityManager.delete(job);
        if (getEventDispatcher().isEnabled()) {
          getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
        }
      }
    }

    // Delete event subscriptions
    if (!enableExecutionRelationshipCounts
        || (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getEventSubscriptionCount() > 0)) {
      EventSubscriptionEntityManager eventSubscriptionEntityManager = getEventSubscriptionEntityManager();
      List<EventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findEventSubscriptionsByExecution(executionEntity.getId());
      for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
        eventSubscriptionEntityManager.delete(eventSubscription);
      }
    }

  }

  private void deleteUserTask(ExecutionEntity executionEntity, String deleteReason){
      boolean enableExecutionRelationshipCounts = isExecutionRelatedEntityCountEnabled(executionEntity);

      if (!enableExecutionRelationshipCounts ||
          (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getTaskCount() > 0)) {
          TaskEntityManager taskEntityManager = getTaskEntityManager();
          Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
          for (TaskEntity taskEntity : tasksForExecution) {
              taskEntityManager.deleteTask(taskEntity, deleteReason, false, false);
          }
      }
  }

    private void cancelUserTask(ExecutionEntity executionEntity, String deleteReason){
        boolean enableExecutionRelationshipCounts = isExecutionRelatedEntityCountEnabled(executionEntity);

        if (!enableExecutionRelationshipCounts ||
            (enableExecutionRelationshipCounts && ((CountingExecutionEntity) executionEntity).getTaskCount() > 0)) {
            TaskEntityManager taskEntityManager = getTaskEntityManager();
            Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
            for (TaskEntity taskEntity : tasksForExecution) {
                taskEntityManager.deleteTask(taskEntity, deleteReason, false, true);
            }
        }
    }

  private void deleteDataForExecution(ExecutionEntity executionEntity, String deleteReason) {
      deleteExecutionEntity(executionEntity,deleteReason);
      deleteUserTask(executionEntity, deleteReason);
  }

  private void cancelDataForExecution(ExecutionEntity executionEntity, String deleteReason) {
      boolean isActive = executionEntity.isActive();

      deleteExecutionEntity(executionEntity,deleteReason);
      cancelUserTask(executionEntity, deleteReason);

      if (isActive &&
          executionEntity.getCurrentFlowElement() != null &&
          !(executionEntity.getCurrentFlowElement() instanceof UserTask) &&
          !(executionEntity.getCurrentFlowElement() instanceof SequenceFlow)) {
          getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createActivityCancelledEvent(executionEntity, deleteReason));
      }
    }

    // OTHER METHODS

  @Override
  public void updateProcessInstanceLockTime(String processInstanceId) {
    Date expirationTime = getClock().getCurrentTime();
    int lockMillis = getAsyncExecutor().getAsyncJobLockTimeInMillis();

    GregorianCalendar lockCal = new GregorianCalendar();
    lockCal.setTime(expirationTime);
    lockCal.add(Calendar.MILLISECOND, lockMillis);
    Date lockDate = lockCal.getTime();

    executionDataManager.updateProcessInstanceLockTime(processInstanceId, lockDate, expirationTime);
  }

  @Override
  public void clearProcessInstanceLockTime(String processInstanceId) {
    executionDataManager.clearProcessInstanceLockTime(processInstanceId);
  }

  @Override
  public String updateProcessInstanceBusinessKey(ExecutionEntity executionEntity, String businessKey) {
    if (executionEntity.isProcessInstanceType() && businessKey != null) {
      executionEntity.setBusinessKey(businessKey);
      getHistoryManager().updateProcessBusinessKeyInHistory(executionEntity);

      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, executionEntity));
      }

      return businessKey;
    }
    return null;
  }

  public ExecutionDataManager getExecutionDataManager() {
    return executionDataManager;
  }

  public void setExecutionDataManager(ExecutionDataManager executionDataManager) {
    this.executionDataManager = executionDataManager;
  }

}
