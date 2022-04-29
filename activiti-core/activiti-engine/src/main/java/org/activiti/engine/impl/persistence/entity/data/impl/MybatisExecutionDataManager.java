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

package org.activiti.engine.impl.persistence.entity.data.impl;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.impl.ExecutionQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.cfg.PerformanceSettings;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.SingleCachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.ExecutionDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionByProcessInstanceMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionsByParentExecutionIdAndActivityIdEntityMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionsByParentExecutionIdEntityMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionsByProcessInstanceIdEntityMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionsByRootProcessInstanceMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ExecutionsWithSameRootProcessInstanceIdMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.InactiveExecutionsByProcInstMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.InactiveExecutionsInActivityAndProcInstMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.InactiveExecutionsInActivityMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.ProcessInstancesByProcessDefinitionMatcher;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.SubProcessInstanceExecutionBySuperExecutionIdMatcher;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;


public class MybatisExecutionDataManager extends AbstractDataManager<ExecutionEntity> implements ExecutionDataManager {

  protected PerformanceSettings performanceSettings;

  protected CachedEntityMatcher<ExecutionEntity> executionsByParentIdMatcher
      = new ExecutionsByParentExecutionIdEntityMatcher();

  protected CachedEntityMatcher<ExecutionEntity> executionsByProcessInstanceIdMatcher
      = new ExecutionsByProcessInstanceIdEntityMatcher();

  protected SingleCachedEntityMatcher<ExecutionEntity> subProcessInstanceBySuperExecutionIdMatcher
      = new SubProcessInstanceExecutionBySuperExecutionIdMatcher();

  protected CachedEntityMatcher<ExecutionEntity> executionsWithSameRootProcessInstanceIdMatcher
      = new ExecutionsWithSameRootProcessInstanceIdMatcher();

  protected CachedEntityMatcher<ExecutionEntity> inactiveExecutionsInActivityAndProcInstMatcher
      = new InactiveExecutionsInActivityAndProcInstMatcher();

  protected CachedEntityMatcher<ExecutionEntity> inactiveExecutionsByProcInstMatcher
      = new InactiveExecutionsByProcInstMatcher();

  protected CachedEntityMatcher<ExecutionEntity> inactiveExecutionsInActivityMatcher
      = new InactiveExecutionsInActivityMatcher();

  protected CachedEntityMatcher<ExecutionEntity> executionByProcessInstanceMatcher
      = new ExecutionByProcessInstanceMatcher();

  protected CachedEntityMatcher<ExecutionEntity> executionsByRootProcessInstanceMatcher
      = new ExecutionsByRootProcessInstanceMatcher();

  protected CachedEntityMatcher<ExecutionEntity> executionsByParentExecutionIdAndActivityIdEntityMatcher
      = new ExecutionsByParentExecutionIdAndActivityIdEntityMatcher();

  protected CachedEntityMatcher<ExecutionEntity> processInstancesByProcessDefinitionMatcher
    = new ProcessInstancesByProcessDefinitionMatcher();

  public MybatisExecutionDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
    this.performanceSettings = processEngineConfiguration.getPerformanceSettings();
  }

  @Override
  public Class<? extends ExecutionEntity> getManagedEntityClass() {
    return ExecutionEntityImpl.class;
  }

  @Override
  public ExecutionEntity create() {
    return ExecutionEntityImpl.createWithEmptyRelationshipCollections();
  }

  @Override
  public ExecutionEntity findById(String entityId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      return findByIdAndFetchExecutionTree(entityId);
    } else {
      return super.findById(entityId);
    }
  }

  protected ExecutionEntity findByIdAndFetchExecutionTree(final String executionId) {

    // If it's in the cache, the tree must have been fetched before
    ExecutionEntity cachedEntity = getEntityCache().findInCache(getManagedEntityClass(), executionId);
    if (cachedEntity != null) {
      return cachedEntity;
    }

    // Fetches execution tree. This will store them in the cache.
    List<ExecutionEntity> executionEntities = getList("selectExecutionsWithSameRootProcessInstanceId", executionId,
        executionsWithSameRootProcessInstanceIdMatcher, true);

    for (ExecutionEntity executionEntity : executionEntities) {
      if (executionId.equals(executionEntity.getId())) {
        return executionEntity;
      }
    }
    return null;
  }

  @Override
  public ExecutionEntity findSubProcessInstanceBySuperExecutionId(final String superExecutionId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(superExecutionId);
    }

    return getEntity("selectSubProcessInstanceBySuperExecutionId",
        superExecutionId,
        subProcessInstanceBySuperExecutionIdMatcher,
        !performanceSettings.isEnableEagerExecutionTreeFetching());
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByParentExecutionId(final String parentExecutionId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(parentExecutionId);
      return getListFromCache(executionsByParentIdMatcher, parentExecutionId);
    } else {
      return getList("selectExecutionsByParentExecutionId", parentExecutionId, executionsByParentIdMatcher, true);
    }
  }

  @Override
  public List<ExecutionEntity> findChildExecutionsByProcessInstanceId(final String processInstanceId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(processInstanceId);
      return getListFromCache(executionsByProcessInstanceIdMatcher, processInstanceId);
    } else {
      return getList("selectChildExecutionsByProcessInstanceId", processInstanceId, executionsByProcessInstanceIdMatcher, true);
    }
  }

  @Override
  public List<ExecutionEntity> findExecutionsByParentExecutionAndActivityIds(final String parentExecutionId, final Collection<String> activityIds) {
    Map<String, Object> parameters = new HashMap<String, Object>(2);
    parameters.put("parentExecutionId", parentExecutionId);
    parameters.put("activityIds", activityIds);

    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(parentExecutionId);
      return getListFromCache(executionsByParentExecutionIdAndActivityIdEntityMatcher, parameters);
    } else {
      return getList("selectExecutionsByParentExecutionAndActivityIds", parameters, executionsByParentExecutionIdAndActivityIdEntityMatcher, true);
    }
  }

  @Override
  public List<ExecutionEntity> findExecutionsByRootProcessInstanceId(final String rootProcessInstanceId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(rootProcessInstanceId);
      return getListFromCache(executionsByRootProcessInstanceMatcher, rootProcessInstanceId);
    } else {
      return getList("selectExecutionsByRootProcessInstanceId", rootProcessInstanceId, executionsByRootProcessInstanceMatcher, true);
    }
  }

  @Override
  public List<ExecutionEntity> findExecutionsByProcessInstanceId(final String processInstanceId) {
    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(processInstanceId);
      return getListFromCache(executionByProcessInstanceMatcher, processInstanceId);
    } else {
      return getList("selectExecutionsByProcessInstanceId", processInstanceId, executionByProcessInstanceMatcher, true);
    }
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByProcessInstanceId(final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(2);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);

    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(processInstanceId);
      return getListFromCache(inactiveExecutionsByProcInstMatcher, params);
    } else {
      return getList("selectInactiveExecutionsForProcessInstance", params, inactiveExecutionsByProcInstMatcher, true);
    }
  }

  @Override
  public Collection<ExecutionEntity> findInactiveExecutionsByActivityIdAndProcessInstanceId(final String activityId, final String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>(3);
    params.put("activityId", activityId);
    params.put("processInstanceId", processInstanceId);
    params.put("isActive", false);

    if (performanceSettings.isEnableEagerExecutionTreeFetching()) {
      findByIdAndFetchExecutionTree(processInstanceId);
      return getListFromCache(inactiveExecutionsInActivityAndProcInstMatcher, params);
    } else {
      return getList("selectInactiveExecutionsInActivityAndProcessInstance", params, inactiveExecutionsInActivityAndProcInstMatcher, true);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> findProcessInstanceIdsByProcessDefinitionId(String processDefinitionId) {
    return getDbSqlSession().selectList("selectProcessInstanceIdsByProcessDefinitionId", processDefinitionId, false);
  }

  @Override
  public long findExecutionCountByQueryCriteria(ExecutionQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectExecutionCountByQueryCriteria", executionQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ExecutionEntity> findExecutionsByQueryCriteria(ExecutionQueryImpl executionQuery, Page page) {
    return getDbSqlSession().selectList("selectExecutionsByQueryCriteria", executionQuery, page, !performanceSettings.isEnableEagerExecutionTreeFetching()); // False -> executions should not be cached if using executionTreeFetching
  }

  @Override
  public long findProcessInstanceCountByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return (Long) getDbSqlSession().selectOne("selectProcessInstanceCountByQueryCriteria", executionQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ProcessInstance> findProcessInstanceByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    return getDbSqlSession().selectList("selectProcessInstanceByQueryCriteria", executionQuery, !performanceSettings.isEnableEagerExecutionTreeFetching()); // False -> executions should not be cached if using executionTreeFetching
  }

  @Override
  public List<ProcessInstance> findProcessInstanceAndVariablesByQueryCriteria(ProcessInstanceQueryImpl executionQuery) {
    // paging doesn't work for combining process instances and variables due
    // to an outer join, so doing it in-memory
    if (executionQuery.getFirstResult() < 0 || executionQuery.getMaxResults() <= 0) {
      return emptyList();
    }

    int firstResult = executionQuery.getFirstResult();
    int maxResults = executionQuery.getMaxResults();

    // setting max results, limit to 20000 results for performance reasons
    if (executionQuery.getProcessInstanceVariablesLimit() != null) {
      executionQuery.setMaxResults(executionQuery.getProcessInstanceVariablesLimit());
    } else {
      executionQuery.setMaxResults(getProcessEngineConfiguration().getExecutionQueryLimit());
    }
    executionQuery.setFirstResult(0);

    List<ProcessInstance> instanceList = getDbSqlSession().selectListWithRawParameterWithoutFilter("selectProcessInstanceWithVariablesByQueryCriteria", executionQuery,
        executionQuery.getFirstResult(), executionQuery.getMaxResults());

    if (instanceList != null && !instanceList.isEmpty()) {
      if (firstResult > 0) {
        if (firstResult <= instanceList.size()) {
          int toIndex = firstResult + Math.min(maxResults, instanceList.size() - firstResult);
          return instanceList.subList(firstResult, toIndex);
        } else {
          return emptyList();
        }
      } else {
        int toIndex = Math.min(maxResults, instanceList.size());
        return instanceList.subList(0, toIndex);
      }
    }
    return emptyList();
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

  @Override
  public void updateExecutionTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateExecutionTenantIdForDeployment", params);
  }

  @Override
  public void updateProcessInstanceLockTime(String processInstanceId, Date lockDate, Date expirationTime) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("id", processInstanceId);
    params.put("lockTime", lockDate);
    params.put("expirationTime", expirationTime);

    int result = getDbSqlSession().update("updateProcessInstanceLockTime", params);
    if (result == 0) {
      throw new ActivitiOptimisticLockingException("Could not lock process instance");
    }
  }

  @Override
  public void updateAllExecutionRelatedEntityCountFlags(boolean newValue) {
    getDbSqlSession().update("updateExecutionRelatedEntityCountEnabled", newValue);
  }

  @Override
  public void clearProcessInstanceLockTime(String processInstanceId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("id", processInstanceId);
    getDbSqlSession().update("clearProcessInstanceLockTime", params);
  }

}
