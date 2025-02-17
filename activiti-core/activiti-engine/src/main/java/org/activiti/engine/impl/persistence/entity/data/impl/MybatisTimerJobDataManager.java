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

package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.TimerJobQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.TimerJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.cachematcher.TimerJobsByExecutionIdMatcher;
import org.activiti.engine.runtime.Job;

/**


 */
public class MybatisTimerJobDataManager extends AbstractDataManager<TimerJobEntity> implements TimerJobDataManager {

  protected CachedEntityMatcher<TimerJobEntity> timerJobsByExecutionIdMatcher = new TimerJobsByExecutionIdMatcher();

  public MybatisTimerJobDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends TimerJobEntity> getManagedEntityClass() {
    return TimerJobEntityImpl.class;
  }

  @Override
  public TimerJobEntity create() {
    return new TimerJobEntityImpl();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Job> findJobsByQueryCriteria(TimerJobQueryImpl jobQuery, Page page) {
    String query = "selectTimerJobByQueryCriteria";
    return getDbSqlSession().selectList(query, jobQuery, page);
  }

  @Override
  public long findJobCountByQueryCriteria(TimerJobQueryImpl jobQuery) {
    return (Long) getDbSqlSession().selectOne("selectTimerJobCountByQueryCriteria", jobQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findTimerJobsToExecute(Page page) {
    Date now = getClock().getCurrentTime();
    return getDbSqlSession().selectList("selectTimerJobsToExecute", now, page);
  }

  @Override
    public List<TimerJobEntity> findTimerStartEvents() {
        return  getDbSqlSession().selectList("selectTimerStartEvents");
    }

  @Override
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionId(String jobHandlerType, String processDefinitionId) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionId", processDefinitionId);
    return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionId", params);

  }

  @Override
  public List<TimerJobEntity> findJobsByExecutionId(final String executionId) {
    return getList("selectTimerJobsByExecutionId", executionId, timerJobsByExecutionIdMatcher, true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findJobsByProcessInstanceId(final String processInstanceId) {
    return getDbSqlSession().selectList("selectTimerJobsByProcessInstanceId", processInstanceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyNoTenantId(String jobHandlerType, String processDefinitionKey) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionKeyNoTenantId", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<TimerJobEntity> findJobsByTypeAndProcessDefinitionKeyAndTenantId(String jobHandlerType, String processDefinitionKey, String tenantId) {
    Map<String, String> params = new HashMap<String, String>(3);
    params.put("handlerType", jobHandlerType);
    params.put("processDefinitionKey", processDefinitionKey);
    params.put("tenantId", tenantId);
    return getDbSqlSession().selectList("selectTimerJobByTypeAndProcessDefinitionKeyAndTenantId", params);
  }

  @Override
  public void updateJobTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateTimerJobTenantIdForDeployment", params);
  }

}
