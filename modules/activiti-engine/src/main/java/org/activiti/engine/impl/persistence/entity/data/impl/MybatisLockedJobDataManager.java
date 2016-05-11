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
package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.JobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.LockedJobEntity;
import org.activiti.engine.impl.persistence.entity.LockedJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.LockedJobDataManager;

/**
 * @author Vasile Dirla
 */
public class MybatisLockedJobDataManager extends AbstractDataManager<LockedJobEntity> implements LockedJobDataManager {

  public MybatisLockedJobDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends LockedJobEntity> getManagedEntityClass() {
    return LockedJobEntityImpl.class;
  }

  @Override
  public LockedJobEntity create() {
    return new LockedJobEntityImpl();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByLockOwner(String lockOwner, int start, int maxNrOfJobs) {
    return getDbSqlSession().selectList("selectJobsByLockOwner", lockOwner, start, maxNrOfJobs);
  }

  @Override
  public List<LockedJobEntity> findJobsByExecutionId(final String executionId) {
    return getList("selectLockedJobsByExecutionId", executionId, new CachedEntityMatcher<LockedJobEntity>() {

      @Override
      public boolean isRetained(LockedJobEntity jobEntity) {
        return jobEntity.getExecutionId() != null && jobEntity.getExecutionId().equals(executionId);
      }
    }, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findExclusiveJobsToExecute(String processInstanceId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("pid", processInstanceId);
    params.put("now", getClock().getCurrentTime());
    return getDbSqlSession().selectList("selectExclusiveJobsToExecute", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByQueryCriteria(JobQueryImpl jobQuery, Page page) {
    return getDbSqlSession().selectList("selectLockedJobByQueryCriteria", jobQuery, page);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionIds(String jobHandlerType, List<String> processDefinitionIds) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("handlerType", jobHandlerType);

    if (processDefinitionIds != null && processDefinitionIds.size() > 0) {
      params.put("processDefinitionIds", processDefinitionIds);
    }
    return getDbSqlSession().selectList("selectJobsByTypeAndProcessDefinitionIds", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    return getDbSqlSession().selectList("selectLockedJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
    Map<String, String> params = new HashMap<String, String>(3);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    params.put("tenantId", tenantId);
    return getDbSqlSession().selectList("selectLockedJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionId", processDefinitionId);
    return getDbSqlSession().selectList("selectLockedJobByTypeAndProcessDefinitionId", params);
  }

  @Override
  public long findJobCountByQueryCriteria(JobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectLockedJobCountByQueryCriteria", jobQuery);
  }

  @Override
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateLockedJobTenantIdForDeployment", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<LockedJobEntity> selectExpiredJobs(long maxLockDuration, Page page) {
    Date acceptedLockTime = new Date(getProcessEngineConfiguration().getClock().getCurrentTime().getTime() - maxLockDuration);
    return getDbSqlSession().selectList("selectLockedExpiredJobs",acceptedLockTime,  page);
  }

}
