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

package org.activiti.engine.impl.persistence.entity;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.ExecutionDataManager;
import org.activiti.engine.impl.util.tree.ExecutionTree;
import org.activiti.engine.impl.util.tree.ExecutionTreeNode;
import org.activiti.engine.impl.util.tree.ExecutionTreeUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
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
    super.delete(entity);
    entity.setDeleted(true);
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
  public ExecutionTree findExecutionTree(String rootProcessInstanceId) {
    return ExecutionTreeUtil.buildExecutionTree(executionDataManager.findExecutionsByRootProcessInstanceId(rootProcessInstanceId));
  }
  
  /**
   * Does not fetches the whole tree, but stops at the process instance 
   * (i.e. does not treat the id as the root process instance id and fetches everything for that root process instance id) 
   */
  protected ExecutionTree findExecutionTreeInCurrentProcessInstance(final String processInstanceId) {
    return ExecutionTreeUtil.buildExecutionTreeForProcessInstance(executionDataManager.findExecutionsByProcessInstanceId(processInstanceId));
  }

  @Override
  public List<ProcessInstance> findProcessInstanceAndVariablesByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return executionDataManager.findProcessInstanceAndVariablesByQueryCriteria(executionQuery);
  }

  @Override
  public List<ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    return executionDataManager.findEventScopeExecutionsByActivityId(activityRef, parentExecutionId);
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByActivityId(final String activityId) {
    return executionDataManager.findInactiveExecutionsByActivityId(activityId);
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
  public ExecutionEntity createProcessInstanceExecution(String processDefinitionId, String businessKey, String tenantId, String initiatorVariableName) {

    ExecutionEntity processInstanceExecution = executionDataManager.create();
    processInstanceExecution.setProcessDefinitionId(processDefinitionId);
    processInstanceExecution.setBusinessKey(businessKey);
    processInstanceExecution.setScope(true); // process instance is always a scope for all child executions

    // Inherit tenant id (if any)
    if (tenantId != null) {
      processInstanceExecution.setTenantId(tenantId);
    }

    // Store in database
    insert(processInstanceExecution, false);

    // Need to be after insert, cause we need the id
    String authenticatedUserId = Authentication.getAuthenticatedUserId();
    if (initiatorVariableName != null) {
      processInstanceExecution.setVariable(initiatorVariableName, authenticatedUserId);
    }
    
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
  
  /**
   * Creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   */
  @Override
  public ExecutionEntity createChildExecution(ExecutionEntity parentExecutionEntity) {
    
    // create the new child execution
    ExecutionEntity childExecution = executionDataManager.create();

    // Inherit tenant id (if any)
    if (parentExecutionEntity.getTenantId() != null) {
      childExecution.setTenantId(parentExecutionEntity.getTenantId());
    }

    // Insert the child execution
    insert(childExecution, false);

    // manage the bidirectional parent-child relation
    parentExecutionEntity.addChildExecution(childExecution);
    childExecution.setParent(parentExecutionEntity);

    // initialize the new execution
    childExecution.setProcessDefinitionId(parentExecutionEntity.getProcessDefinitionId());
    childExecution.setProcessInstanceId(parentExecutionEntity.getProcessInstanceId() != null 
        ? parentExecutionEntity.getProcessInstanceId() : parentExecutionEntity.getId());
    childExecution.setRootProcessInstanceId(parentExecutionEntity.getRootProcessInstanceId());
    childExecution.setScope(false);

    if (logger.isDebugEnabled()) {
      logger.debug("Child execution {} created with parent {}", childExecution, parentExecutionEntity.getId());
    }

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, childExecution));
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, childExecution));
    }

    return childExecution;
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

  private void deleteProcessInstanceCascade(ExecutionEntity execution, String deleteReason, boolean deleteHistory) {
    for (ExecutionEntity subExecutionEntity : execution.getExecutions()) {
      if (subExecutionEntity.getSubProcessInstance() != null) {
        deleteProcessInstanceCascade(subExecutionEntity.getSubProcessInstance(), deleteReason, deleteHistory);
      }
    }
    
    IdentityLinkEntityManager identityLinkEntityManager = getIdentityLinkEntityManager();
    List<IdentityLinkEntity> identityLinkEntities = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(execution.getId());
    for (IdentityLinkEntity identityLinkEntity : identityLinkEntities) {
      identityLinkEntityManager.delete(identityLinkEntity);
    }

    getTaskEntityManager().deleteTasksByProcessInstanceId(execution.getId(), deleteReason, deleteHistory);

    // delete the execution BEFORE we delete the history, otherwise we will
    // produce orphan HistoricVariableInstance instances
    
    ExecutionTree executionTree = findExecutionTreeInCurrentProcessInstance(execution.getProcessInstanceId());
    ExecutionTreeNode executionTreeNode = null;
    if (executionTree.getRoot() != null) {
      executionTreeNode = executionTree.getTreeNode(execution.getId());
    }
    
    if (executionTreeNode == null) {
      return;
    }
    
    Iterator<ExecutionTreeNode> iterator = executionTreeNode.leafsFirstIterator();
    while (iterator.hasNext()) {
      ExecutionEntity childExecutionEntity = iterator.next().getExecutionEntity();
      deleteExecutionAndRelatedData(childExecutionEntity, deleteReason, false);
    }
    
    deleteExecutionAndRelatedData(execution, deleteReason, false);

    if (deleteHistory) {
      getHistoricProcessInstanceEntityManager().delete(execution.getId());
    }
  }
  
  @Override
  public void deleteExecutionAndRelatedData(ExecutionEntity executionEntity, String deleteReason, boolean cancel) {
    deleteDataRelatedToExecution(executionEntity, deleteReason, cancel);
    delete(executionEntity);
  }
  
  @Override
  public void deleteProcessInstanceExecutionEntity(String processInstanceId, 
      String currentFlowElementId, String deleteReason, boolean cascade, boolean cancel, boolean fireEvent) {
    
    ExecutionEntity processInstanceEntity = findById(processInstanceId);
    
    if (processInstanceEntity == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id '" + processInstanceId + "'", ProcessInstance.class);
    }
    
    if (processInstanceEntity.isDeleted()) {
      return;
    }
    
    for (ExecutionEntity subExecutionEntity : processInstanceEntity.getExecutions()) {
      if (subExecutionEntity.getSubProcessInstance() != null) {
        deleteProcessInstanceCascade(subExecutionEntity.getSubProcessInstance(), deleteReason, cascade);
      }
    }

    // delete event scope executions
    for (ExecutionEntity childExecution : processInstanceEntity.getExecutions()) {
      if (childExecution.isEventScope()) {
        deleteExecutionAndRelatedData(childExecution, null, false);
      }
    }
    
    deleteChildExecutions(processInstanceEntity, deleteReason, cancel);
    deleteExecutionAndRelatedData(processInstanceEntity, deleteReason, cancel);
    
    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.PROCESS_COMPLETED, processInstanceEntity));
    }

    // TODO: what about delete reason?
    getHistoryManager().recordProcessInstanceEnd(processInstanceEntity.getId(), deleteReason, currentFlowElementId);
    
    processInstanceEntity.setDeleted(true);
  }
  
  @Override
  public void deleteChildExecutions(ExecutionEntity executionEntity, String deleteReason, boolean cancel) {

    // The children of an execution for a tree. For correct deletions
    // (taking care of foreign keys between child-parent)
    // the leafs of this tree must be deleted first before the parents elements.
    
    ExecutionTree executionTree = findExecutionTree(executionEntity.getRootProcessInstanceId());
    ExecutionTreeNode executionTreeNode = executionTree.getTreeNode(executionEntity.getId());
    
    if (executionTreeNode == null) {
      return;
    }
    
    Iterator<ExecutionTreeNode> iterator = executionTreeNode.leafsFirstIterator();
    while (iterator.hasNext()) {
      ExecutionEntity childExecutionEntity = iterator.next().getExecutionEntity();
      if (childExecutionEntity.isActive() 
          && !childExecutionEntity.isEnded()
          && !executionTreeNode.getExecutionEntity().getId().equals(childExecutionEntity.getId())) { // Not the root of the tree is deleted here
        deleteExecutionAndRelatedData(childExecutionEntity, deleteReason, cancel);
      }
    }

  }
  
  @Override
  public void deleteDataRelatedToExecution(ExecutionEntity executionEntity, String deleteReason, boolean cancel) {

    // To start, deactivate the current incoming execution
    executionEntity.setEnded(true);
    executionEntity.setActive(false);
    
    if (executionEntity.getId().equals(executionEntity.getProcessInstanceId())) {
      IdentityLinkEntityManager identityLinkEntityManager = getIdentityLinkEntityManager();
      Collection<IdentityLinkEntity> identityLinks = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(executionEntity.getProcessInstanceId());
      for (IdentityLinkEntity identityLink : identityLinks) {
        identityLinkEntityManager.delete(identityLink);
      }
    }

    // Get variables related to execution and delete them
    VariableInstanceEntityManager variableInstanceEntityManager = getVariableInstanceEntityManager();
    Collection<VariableInstanceEntity> executionVariables = variableInstanceEntityManager.findVariableInstancesByExecutionId(executionEntity.getId());
    for (VariableInstanceEntity variableInstanceEntity : executionVariables) {
      variableInstanceEntityManager.delete(variableInstanceEntity);
      if (variableInstanceEntity.getByteArrayRef() != null && variableInstanceEntity.getByteArrayRef().getId() != null) {
        getByteArrayEntityManager().deleteByteArrayById(variableInstanceEntity.getByteArrayRef().getId());
      }
    }

    // Delete current user tasks
    TaskEntityManager taskEntityManager = getTaskEntityManager();
    Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
    for (TaskEntity taskEntity : tasksForExecution) {
      taskEntityManager.deleteTask(taskEntity, deleteReason, false, cancel);
    }

    // Delete jobs
    JobEntityManager jobEntityManager = getJobEntityManager();
    Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(executionEntity.getId());
    for (JobEntity job : jobsForExecution) {
      getJobEntityManager().delete(job);
      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
      }
//      jobEntityManager.delete(job, false); // false -> jobs fire the events themselves TODO: is this right?
    }

    // Delete event subscriptions
    EventSubscriptionEntityManager eventSubscriptionEntityManager = getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findEventSubscriptionsByExecution(executionEntity.getId());
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      eventSubscriptionEntityManager.delete(eventSubscription);
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
