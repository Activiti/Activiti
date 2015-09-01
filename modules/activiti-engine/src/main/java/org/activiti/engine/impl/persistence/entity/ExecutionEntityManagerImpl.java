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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.CachedPersistentObjectMatcher;
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

  @Override
  public Class<ExecutionEntity> getManagedPersistentObject() {
    return ExecutionEntity.class;
  }

  // FIND METHODS

  @Override
  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(final String superExecutionId) {
    return findByQuery("selectSubProcessInstanceBySuperExecutionId", superExecutionId, new CachedPersistentObjectMatcher<ExecutionEntity>() {

      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getSuperExecutionId() != null && superExecutionId.equals(executionEntity.getSuperExecutionId());
      }
      
    });
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(final String parentExecutionId) {
    return getList("selectExecutionsByParentExecutionId", parentExecutionId, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getParentId() != null && entity.getParentId().equals(parentExecutionId);
      }
    }, true);
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByProcessInstanceId(final String processInstanceId) {
    return getList("selectChildExecutionsByProcessInstanceId", processInstanceId, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && executionEntity.getParentId() != null;
      }
    }, true);
  }

  @Override
  public List<ExecutionEntity> findExecutionsByParentExecutionAndActivityIds(final String parentExecutionId, final Collection<String> activityIds) {
    
    Map<String, Object> parameters = new HashMap<String, Object>(2);
    parameters.put("parentExecutionId", parentExecutionId);
    parameters.put("activityIds", activityIds);
    
    return getList("selectExecutionsByParentExecutionAndActivityIds", parameters, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getParentId() != null && executionEntity.getParentId().equals(parentExecutionId)
            && executionEntity.getActivityId() != null && activityIds.contains(executionEntity.getActivityId());
      }
      
    }, true);
  }

  @Override
  public long findExecutionCountByQueryCriteria(ExecutionQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(ExecutionQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }
  
  @Override
  public long findProcessInstanceCountByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return getDbSqlSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery);
  }
  
  @Override
  public ExecutionTree findExecutionTree(final String rootProcessInstanceId) {
    List<ExecutionEntity> executions = getList("selectExecutionsByRootProcessInstanceId", rootProcessInstanceId, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getRootProcessInstanceId() != null && entity.getRootProcessInstanceId().equals(rootProcessInstanceId);
      }
    }, true); 
    return ExecutionTreeUtil.buildExecutionTree(executions);
  }
  
  /**
   * Does not fetches the whole tree, but stops at the process instance 
   * (i.e. does not treat the id as the root process instance id and fetches everything for that root process instance id) 
   */
  protected ExecutionTree findExecutionTreeInCurrentProcessInstance(final String processInstanceId) {
    List<ExecutionEntity> executions = getList("selectExecutionsByProcessInstanceId", processInstanceId, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getProcessInstanceId() != null && entity.getProcessInstanceId().equals(processInstanceId);
      }
    }, true); 
    return ExecutionTreeUtil.buildExecutionTreeForProcessInstance(executions);
}

  @Override
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceAndVariablesByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    // paging doesn't work for combining process instances and variables due
    // to an outer join, so doing it in-memory
    if (executionQuery.getFirstResult() < 0 || executionQuery.getMaxResults() <= 0) {
      return Collections.EMPTY_LIST;
    }

    int firstResult = executionQuery.getFirstResult();
    int maxResults = executionQuery.getMaxResults();

    // setting max results, limit to 20000 results for performance reasons
    executionQuery.setMaxResults(20000);
    executionQuery.setFirstResult(0);

    List<ProcessInstance> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter("selectProcessInstanceWithVariablesByQueryCriteria", executionQuery,
        executionQuery.getFirstResult(), executionQuery.getMaxResults());

    if (instanceList != null && !instanceList.isEmpty()) {
      if (firstResult > 0) {
        if (firstResult <= instanceList.size()) {
          int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
          return instanceList.subList(firstResult, toIndex);
        } else {
          return Collections.EMPTY_LIST;
        }
      } else {
        int toIndex = Math.min(maxResults, instanceList.size());
        return instanceList.subList(0, toIndex);
      }
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityRef);
    parameters.put("parentExecutionId", parentExecutionId);

    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parameters);
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByActivityId(final String activityId) {
    HashMap<String, Object> params = new HashMap<String, Object>(2);
    params.put("activityId", activityId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsInActivity", params, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity entity) {
        return !entity.isActive() && entity.getActivityId() != null && entity.getActivityId().equals(activityId);
      }
    }, true);
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByProcessInstanceId(final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(2);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsForProcessInstance", params, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && !executionEntity.isActive();
      }
    }, true);
  }
  
  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByActivityIdAndProcessInstanceId(final String activityId, final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(3);
    params.put("activityId", activityId);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsInActivityAndProcessInstance", params, new CachedPersistentObjectMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && !executionEntity.isActive() &&
            executionEntity.getActivityId() != null && executionEntity.getActivityId().equals(activityId);
      }
    }, true);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<Execution> findExecutionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findExecutionCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByNativeQuery", parameterMap);
  }

  // CREATE METHODS

  @Override
  public ExecutionEntity createProcessInstanceExecution(String processDefinitionId, String businessKey, String tenantId, String initiatorVariableName) {

    ExecutionEntity processInstanceExecution = new ExecutionEntity();
    processInstanceExecution.setProcessDefinitionId(processDefinitionId);
    processInstanceExecution.setBusinessKey(businessKey);
    processInstanceExecution.setScope(true); // process instance is always a scope for all child executions

    // Inherit tenant id (if any)
    if (tenantId != null) {
      processInstanceExecution.setTenantId(tenantId);
    }

    // Store in database
    getExecutionEntityManager().insert(processInstanceExecution, false);

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
    ExecutionEntity childExecution = new ExecutionEntity();

    // Inherit tenant id (if any)
    if (parentExecutionEntity.getTenantId() != null) {
      childExecution.setTenantId(parentExecutionEntity.getTenantId());
    }

    // Insert the child execution
    insert(childExecution, false);

    // manage the bidirectional parent-child relation
    parentExecutionEntity.getExecutions().add(childExecution);
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
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateExecutionTenantIdForDeployment", params);
  }

  // DELETE METHODS

  @Override
  @SuppressWarnings("unchecked")
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade) {
    List<String> processInstanceIds = getDbSqlSession().selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

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

    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("id", processInstanceId);
    params.put("lockTime", lockCal.getTime());
    params.put("expirationTime", expirationTime);

    int result = getDbSqlSession().update("updateProcessInstanceLockTime", params);
    if (result == 0) {
      throw new ActivitiOptimisticLockingException("Could not lock process instance");
    }
  }

  @Override
  public void clearProcessInstanceLockTime(String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("id", processInstanceId);

    getDbSqlSession().update("clearProcessInstanceLockTime", params);
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

}
