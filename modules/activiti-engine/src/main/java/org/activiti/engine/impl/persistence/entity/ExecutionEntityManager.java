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

import java.util.ArrayList;
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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityExecution;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
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
public class ExecutionEntityManager extends AbstractEntityManager<ExecutionEntity> {
  
  private static final Logger logger = LoggerFactory.getLogger(ExecutionEntityManager.class);

  @Override
  public Class<ExecutionEntity> getManagedPersistentObject() {
    return ExecutionEntity.class;
  }

  // FIND METHODS

  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(final String superExecutionId) {
    return getEntity(ExecutionEntity.class, "selectSubProcessInstanceBySuperExecutionId", superExecutionId, new CachedEntityMatcher<ExecutionEntity>() {

      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getSuperExecutionId() != null && superExecutionId.equals(executionEntity.getSuperExecutionId());
      }
      
    });
  }

  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(final String parentExecutionId) {
    return getList("selectExecutionsByParentExecutionId", parentExecutionId, new CachedEntityMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getParentId() != null && entity.getParentId().equals(parentExecutionId);
      }
    });
  }

  public List<ExecutionEntity> findChildExecutionsByProcessInstanceId(final String processInstanceId) {
    return getList("selectChildExecutionsByProcessInstanceId", processInstanceId, new CachedEntityMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && executionEntity.getParentId() != null;
      }
    });
  }

  public ExecutionEntity findExecutionById(final String executionId) {
    return (ExecutionEntity) getEntity(ExecutionEntity.class, executionId, new CachedEntityMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getId().equals(executionId);
      }
    });
  }
  
  public List<ExecutionEntity> findExecutionsByParentExecutionAndActivityIds(final String parentExecutionId, final Collection<String> activityIds) {
    
    Map<String, Object> parameters = new HashMap<String, Object>(2);
    parameters.put("parentExecutionId", parentExecutionId);
    parameters.put("activityIds", activityIds);
    
    return getList("selectExecutionsByParentExecutionAndActivityIds", parameters, new CachedEntityMatcher<ExecutionEntity>() {
      
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getParentId() != null && executionEntity.getParentId().equals(parentExecutionId)
            && executionEntity.getActivityId() != null && activityIds.contains(executionEntity.getActivityId());
      }
      
    });
  }

  public long findExecutionCountByQueryCriteria(ExecutionQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(ExecutionQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page);
  }
  
  public long findProcessInstanceCountByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return getDbSqlSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery);
  }
  
  public ExecutionTree findExecutionTree(final String processInstanceId) {
      List<ExecutionEntity> executions = getList("selectExecutionsByRootProcessInstanceId", processInstanceId, new CachedEntityMatcher<ExecutionEntity>() {
        @Override
        public boolean isRetained(ExecutionEntity entity) {
          return entity.getRootProcessInstanceId() != null && entity.getRootProcessInstanceId().equals(processInstanceId);
        }
      }); 
      return ExecutionTreeUtil.buildExecutionTree(executions);
  }
  
  public ExecutionTree findExecutionTreeInCurrentProcessInstance(final String processInstanceId) {
    List<ExecutionEntity> executions = getList("selectExecutionsByProcessInstanceId", processInstanceId, new CachedEntityMatcher<ExecutionEntity>() {
      @Override
      public boolean isRetained(ExecutionEntity entity) {
        return entity.getProcessInstanceId() != null && entity.getProcessInstanceId().equals(processInstanceId);
      }
    }); 
    return ExecutionTreeUtil.buildExecutionTreeForProcessInstance(executions);
}

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

  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findEventScopeExecutionsByActivityId(String activityRef, String parentExecutionId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("activityId", activityRef);
    parameters.put("parentExecutionId", parentExecutionId);

    return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parameters);
  }

  public Collection<ExecutionEntity> getInactiveExecutionsInActivity(final String activityId) {
    HashMap<String, Object> params = new HashMap<String, Object>(2);
    params.put("activityId", activityId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsInActivity", params, new CachedEntityMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity entity) {
        return !entity.isActive() && entity.getActivityId() != null && entity.getActivityId().equals(activityId);
      }
    });
  }

  public Collection<ExecutionEntity> getInactiveExecutionsForProcessInstance(final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(2);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsForProcessInstance", params, new CachedEntityMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && !executionEntity.isActive();
      }
    });
  }
  
  public Collection<ExecutionEntity> getInactiveExecutionsInActivityAndForProcessInstance(final String activityId, final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(3);
    params.put("activityId", activityId);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);
    return getList("selectInactiveExecutionsInActivityAndProcessInstance", params, new CachedEntityMatcher<ExecutionEntity>() {
      public boolean isRetained(ExecutionEntity executionEntity) {
        return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && !executionEntity.isActive() &&
            executionEntity.getActivityId() != null && executionEntity.getActivityId().equals(activityId);
      }
    });
  }
  
  public List<String> findActiveActivityIds(ExecutionEntity executionEntity) {
    List<String> activeActivityIds = new ArrayList<String>();
    collectActiveActivityIds(executionEntity, activeActivityIds);
    return activeActivityIds;
  }

  protected void collectActiveActivityIds(ExecutionEntity executionEntity, List<String> activeActivityIds) {
    if (executionEntity.isActive() && executionEntity.getActivityId() != null) {
      activeActivityIds.add(executionEntity.getActivityId());
    }
    
    for (ExecutionEntity childExecution : executionEntity.getExecutions()) {
      collectActiveActivityIds(childExecution, activeActivityIds);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Execution> findExecutionsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectExecutionByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findExecutionCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByNativeQuery", parameterMap);
  }

  // CREATE METHODS

  @Override
  public void insert(ExecutionEntity entity, boolean fireCreateEvent) {
    super.insert(entity, fireCreateEvent);
  }

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
    Context.getCommandContext().getExecutionEntityManager().insert(processInstanceExecution, false);

    // Need to be after insert, cause we need the id
    String authenticatedUserId = Authentication.getAuthenticatedUserId();
    if (initiatorVariableName != null) {
      processInstanceExecution.setVariable(initiatorVariableName, authenticatedUserId);
    }
    
    processInstanceExecution.setProcessInstanceId(processInstanceExecution.getId());
    processInstanceExecution.setRootProcessInstanceId(processInstanceExecution.getId());
    if (authenticatedUserId != null) {
      processInstanceExecution.addIdentityLink(authenticatedUserId, null, IdentityLinkType.STARTER);
    }

    // Fire events
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processInstanceExecution));
    }

    return processInstanceExecution;
  }
  
  public ExecutionEntity createChildExecution(ActivityExecution parentExecutionEntity) {
    return createChildExecution( (ExecutionEntity) parentExecutionEntity);
  }
  
  /**
   * Creates a new execution. properties processDefinition, processInstance and activity will be initialized.
   */
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

    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, childExecution));
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, childExecution));
    }

    return childExecution;
  }

  // UPDATE METHODS

  public void updateExecutionTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateExecutionTenantIdForDeployment", params);
  }

  // DELETE METHODS

  @SuppressWarnings("unchecked")
  public void deleteProcessInstancesByProcessDefinition(String processDefinitionId, String deleteReason, boolean cascade) {
    List<String> processInstanceIds = getDbSqlSession().selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId);

    for (String processInstanceId : processInstanceIds) {
      deleteProcessInstance(processInstanceId, deleteReason, cascade);
    }

    if (cascade) {
      Context.getCommandContext().getHistoricProcessInstanceEntityManager().deleteHistoricProcessInstanceByProcessDefinitionId(processDefinitionId);
    }
  }

  /**
   * This method should be deleted in favor of the newer deleteProcessInstanceExecutionEntity method
   */
  public void deleteProcessInstance(String processInstanceId, String deleteReason) {
    deleteProcessInstance(processInstanceId, deleteReason, false);
  }

  public void deleteProcessInstance(String processInstanceId, String deleteReason, boolean cascade) {
    ExecutionEntity execution = findExecutionById(processInstanceId);

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
    
    IdentityLinkEntityManager identityLinkEntityManager = Context.getCommandContext().getIdentityLinkEntityManager();
    List<IdentityLinkEntity> identityLinkEntities = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(execution.getId());
    for (IdentityLinkEntity identityLinkEntity : identityLinkEntities) {
      identityLinkEntityManager.delete(identityLinkEntity);
    }

    CommandContext commandContext = Context.getCommandContext();
    commandContext.getTaskEntityManager().deleteTasksByProcessInstanceId(execution.getId(), deleteReason, deleteHistory);

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
    
    deleteExecutionAndRelatedData(execution);
    execution.setDeleteReason(deleteReason);

    if (deleteHistory) {
      commandContext.getHistoricProcessInstanceEntityManager().deleteHistoricProcessInstanceById(execution.getId());
    }
  }
  
  public void deleteExecutionAndRelatedData(ExecutionEntity executionEntity) {
    deleteExecutionAndRelatedData(executionEntity, null);
  }

  public void deleteExecutionAndRelatedData(ExecutionEntity executionEntity, String deleteReason) {
    deleteExecutionAndRelatedData(executionEntity, deleteReason, false);
  }
  
  public void deleteExecutionAndRelatedData(ExecutionEntity executionEntity, String deleteReason, boolean cancel) {
    deleteDataRelatedToExecution(executionEntity, deleteReason, cancel);
    delete(executionEntity);
  }
  
  public void deleteProcessInstanceExecutionEntity(String processInstanceId, String currentFlowElementId, String deleteReason) {
    deleteProcessInstanceExecutionEntity(processInstanceId, currentFlowElementId, deleteReason, false, false);
  }
  
  public void deleteProcessInstanceExecutionEntity(String processInstanceId, String currentFlowElementId, String deleteReason, boolean cancel) {
    deleteProcessInstanceExecutionEntity(processInstanceId, currentFlowElementId, deleteReason, false, true);
  }
  
  public void deleteProcessInstanceExecutionEntity(String processInstanceId, 
      String currentFlowElementId, String deleteReason, boolean cascade, boolean cancel) {

    ExecutionEntity processInstanceEntity = findExecutionById(processInstanceId);
    
    if (processInstanceEntity == null) {
      throw new ActivitiObjectNotFoundException("No process instance found for id '" + processInstanceId + "'", ProcessInstance.class);
    }
    
    deleteProcessInstanceExecutionEntity(processInstanceEntity, currentFlowElementId, deleteReason, cascade, cancel);
  }
  
  public void deleteProcessInstanceExecutionEntity(ExecutionEntity processInstanceEntity, 
      String currentFlowElementId, String deleteReason, boolean cascade, boolean cancel) {
    deleteProcessInstanceExecutionEntity(processInstanceEntity, currentFlowElementId, deleteReason, cascade, cancel, true);
  }
  
  public void deleteProcessInstanceExecutionEntity(ExecutionEntity processInstanceEntity, 
      String currentFlowElementId, String deleteReason, boolean cascade, boolean cancel, boolean fireEvent) {
    
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
        deleteExecutionAndRelatedData(childExecution);
      }
    }
    
    deleteChildExecutions(processInstanceEntity, deleteReason, cancel);
    deleteExecutionAndRelatedData(processInstanceEntity, deleteReason, cancel);
    
    if (Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          ActivitiEventBuilder.createEntityEvent(ActivitiEventType.PROCESS_COMPLETED, processInstanceEntity));
    }

    // TODO: what about delete reason?
    Context.getCommandContext().getHistoryManager().recordProcessInstanceEnd(processInstanceEntity.getId(), deleteReason, currentFlowElementId);
    
    processInstanceEntity.setDeleted(true);
  }
  
  public void deleteChildExecutions(ExecutionEntity executionEntity) {
    deleteChildExecutions(executionEntity, null);
  }

  public void deleteChildExecutions(ExecutionEntity executionEntity, String deleteReason) {
    deleteChildExecutions(executionEntity, deleteReason, false);
  }
    
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
  
  public void deleteDataRelatedToExecution(ExecutionEntity executionEntity, boolean cancel) {
    deleteDataRelatedToExecution(executionEntity, null, cancel);
  }
  
  public void deleteDataRelatedToExecution(ExecutionEntity executionEntity) {
    deleteDataRelatedToExecution(executionEntity, null, false);
  }

  public void deleteDataRelatedToExecution(ExecutionEntity executionEntity, String deleteReason, boolean cancel) {

    CommandContext commandContext = Context.getCommandContext();
    
    // To start, deactivate the current incoming execution
    executionEntity.setEnded(true);
    executionEntity.setActive(false);
    
    if (executionEntity.getId().equals(executionEntity.getProcessInstanceId())) {
      IdentityLinkEntityManager identityLinkEntityManager = commandContext.getIdentityLinkEntityManager();
      Collection<IdentityLinkEntity> identityLinks = identityLinkEntityManager.findIdentityLinksByProcessInstanceId(executionEntity.getProcessInstanceId());
      for (IdentityLinkEntity identityLink : identityLinks) {
        identityLinkEntityManager.delete(identityLink);
      }
    }

    // Get variables related to execution and delete them
    VariableInstanceEntityManager variableInstanceEntityManager = commandContext.getVariableInstanceEntityManager();
    Collection<VariableInstanceEntity> executionVariables = variableInstanceEntityManager.findVariableInstancesByExecutionId(executionEntity.getId());
    for (VariableInstanceEntity variableInstanceEntity : executionVariables) {
      variableInstanceEntityManager.delete(variableInstanceEntity);
      if (variableInstanceEntity.getByteArrayRef() != null) {
        commandContext.getByteArrayEntityManager().deleteByteArrayById(variableInstanceEntity.getByteArrayRef().getId());
      }
    }

    // Delete current user tasks
    TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
    Collection<TaskEntity> tasksForExecution = taskEntityManager.findTasksByExecutionId(executionEntity.getId());
    for (TaskEntity taskEntity : tasksForExecution) {
      taskEntityManager.deleteTask(taskEntity, deleteReason, false, cancel);
    }

    // Delete jobs
    JobEntityManager jobEntityManager = commandContext.getJobEntityManager();
    Collection<JobEntity> jobsForExecution = jobEntityManager.findJobsByExecutionId(executionEntity.getId());
    for (JobEntity job : jobsForExecution) {
      job.delete(); // TODO: should be moved to entitymanager!
      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
        Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.JOB_CANCELED, job));
      }
//      jobEntityManager.delete(job, false); // false -> jobs fire the events themselves TODO: is this right?
    }

    // Delete event subscriptions
    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = eventSubscriptionEntityManager.findEventSubscriptionsByExecution(executionEntity.getId());
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      eventSubscriptionEntityManager.deleteEventSubscription(eventSubscription);
    }
  }

  // OTHER METHODS

  public void updateProcessInstanceLockTime(String processInstanceId) {
    CommandContext commandContext = Context.getCommandContext();
    Date expirationTime = commandContext.getProcessEngineConfiguration().getClock().getCurrentTime();
    int lockMillis = commandContext.getProcessEngineConfiguration().getAsyncExecutor().getAsyncJobLockTimeInMillis();
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

  public void clearProcessInstanceLockTime(String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("id", processInstanceId);

    getDbSqlSession().update("clearProcessInstanceLockTime", params);
  }

}
