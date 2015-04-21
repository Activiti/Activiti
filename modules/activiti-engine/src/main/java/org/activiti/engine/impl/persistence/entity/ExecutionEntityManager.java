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
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ExecutionEntityManager extends AbstractEntityManager<ExecutionEntity> {

    @Override
    public Class<ExecutionEntity> getManagedPersistentObject() {
        return ExecutionEntity.class;
    }

    
    // FIND METHODS
    
    
    public ExecutionEntity findSubProcessInstanceBySuperExecutionId(String superExecutionId) {
        return (ExecutionEntity) getDbSqlSession().selectOne("selectSubProcessInstanceBySuperExecutionId", superExecutionId);
    }

    @SuppressWarnings("unchecked")
    public List<ExecutionEntity> findChildExecutionsByParentExecutionId(String parentExecutionId) {
        return getDbSqlSession().selectList("selectExecutionsByParentExecutionId", parentExecutionId);
    }

    @SuppressWarnings("unchecked")
    public Collection<ExecutionEntity> findChildExecutionsByProcessInstanceId(final String processInstanceId) {
        return getList("selectExecutionsByProcessInstanceId", processInstanceId, new CachedEntityMatcher<ExecutionEntity>() {
            @Override
            public boolean isRetained(ExecutionEntity executionEntity) {
                return executionEntity.getProcessInstanceId() != null 
                		&& executionEntity.getProcessInstanceId().equals(processInstanceId)
                		&& executionEntity.getParentId() != null;
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
        HashMap<String, String> params = new HashMap<String, String>(1);
        params.put("activityId", activityId);
        return getList("selectInactiveExecutionsInActivity", params, new CachedEntityMatcher<ExecutionEntity>() {
            public boolean isRetained(ExecutionEntity entity) {
                return !entity.isActive() && entity.getActivityId() != null && entity.getActivityId().equals(activityId);
            }
        });
    }

    public Collection<ExecutionEntity> getInactiveExecutionsForProcessInstance(final String processInstanceId) {
        HashMap<String, String> params = new HashMap<String, String>(1);
        params.put("processInstanceId", processInstanceId);
        return getList("selectInactiveExecutionsForProcessInstance", params, new CachedEntityMatcher<ExecutionEntity>() {
            public boolean isRetained(ExecutionEntity executionEntity) {
                return executionEntity.getProcessInstanceId() != null && executionEntity.getProcessInstanceId().equals(processInstanceId) && !executionEntity.isActive();
            }
        });
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
    
    
    public ExecutionEntity createProcessInstanceExecution(String processDefinitionId, String businessKey, String tenantId,  String initiatorVariableName) {

        ExecutionEntity processInstanceExecution = new ExecutionEntity();
        processInstanceExecution.setProcessDefinitionId(processDefinitionId);
        processInstanceExecution.setBusinessKey(businessKey);
        processInstanceExecution.setScope(true); // process instance is always a scope for all child executions

        // Inherit tenant id (if any)
        if (tenantId != null) {
            processInstanceExecution.setTenantId(tenantId);
        }

        String authenticatedUserId = Authentication.getAuthenticatedUserId();
        if (initiatorVariableName != null) {
            processInstanceExecution.setVariable(initiatorVariableName, authenticatedUserId);
        }
        if (authenticatedUserId != null) {
            processInstanceExecution.addIdentityLink(authenticatedUserId, null, IdentityLinkType.STARTER);
        }

        // Store in database
        Context.getCommandContext().getExecutionEntityManager().insert(processInstanceExecution);
        
        // Need to be after insert, cause we need the id
        processInstanceExecution.setProcessInstanceId(processInstanceExecution.getId());

        // Fire events
        if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processInstanceExecution));
        }

        return processInstanceExecution;
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

        CommandContext commandContext = Context.getCommandContext();
        commandContext.getTaskEntityManager().deleteTasksByProcessInstanceId(execution.getId(), deleteReason, deleteHistory);

        // delete the execution BEFORE we delete the history, otherwise we will
        // produce orphan HistoricVariableInstance instances
        execution.deleteCascade(deleteReason);

        if (deleteHistory) {
            commandContext.getHistoricProcessInstanceEntityManager().deleteHistoricProcessInstanceById(execution.getId());
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
