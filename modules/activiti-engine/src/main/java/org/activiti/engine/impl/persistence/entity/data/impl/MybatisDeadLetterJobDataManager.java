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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.DeadLetterJobQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntity;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.DeadLetterJobDataManager;
import org.activiti.engine.runtime.Job;

/**
 * @author Tijs Rademakers
 */
public class MybatisDeadLetterJobDataManager extends AbstractDataManager<DeadLetterJobEntity> implements DeadLetterJobDataManager {

  public MybatisDeadLetterJobDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends DeadLetterJobEntity> getManagedEntityClass() {
    return DeadLetterJobEntityImpl.class;
  }

  @Override
  public DeadLetterJobEntity create() {
    return new DeadLetterJobEntityImpl();
  }
  
  @Override
  public void delete(DeadLetterJobEntity entity) {
    getDbSqlSession().delete(entity);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(DeadLetterJobQueryImpl jobQuery, Page page) {
    String query = "selectDeadLetterJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @Override
  public long findJobCountByQueryCriteria(DeadLetterJobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeadLetterJobCountByQueryCriteria", jobQuery);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<DeadLetterJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionId", processDefinitionId);
    return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionId", params);

  }

  @Override
  public List<DeadLetterJobEntity> findJobsByExecutionId(final String executionId) {
    return getList("selectDeadLetterJobsByExecutionId", executionId, new CachedEntityMatcher<DeadLetterJobEntity>() {

      @Override
      public boolean isRetained(DeadLetterJobEntity jobEntity) {
        return jobEntity.getExecutionId() != null && jobEntity.getExecutionId().equals(executionId);
      }
    }, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DeadLetterJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DeadLetterJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
    Map<String, String> params = new HashMap<String, String>(3);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    params.put("tenantId", tenantId);
    return getDbSqlSession().selectList("selectDeadLetterJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }

  @Override
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateDeadLetterJobTenantIdForDeployment", params);
  }

}
