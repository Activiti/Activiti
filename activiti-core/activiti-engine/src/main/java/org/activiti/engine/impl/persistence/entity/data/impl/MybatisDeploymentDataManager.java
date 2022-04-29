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

import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.DeploymentQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.DeploymentDataManager;
import org.activiti.engine.repository.Deployment;


public class MybatisDeploymentDataManager extends AbstractDataManager<DeploymentEntity> implements DeploymentDataManager {

  public MybatisDeploymentDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends DeploymentEntity> getManagedEntityClass() {
    return DeploymentEntityImpl.class;
  }

  @Override
  public DeploymentEntity create() {
    return new DeploymentEntityImpl();
  }

  @Override
  public DeploymentEntity findLatestDeploymentByName(String deploymentName) {
    List<?> list = getDbSqlSession().selectList("selectDeploymentsByName", deploymentName, 0, 1);
    if (list != null && !list.isEmpty()) {
      return (DeploymentEntity) list.get(0);
    }
    return null;
  }

  @Override
  public long findDeploymentCountByQueryCriteria(DeploymentQueryImpl deploymentQuery) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByQueryCriteria", deploymentQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByQueryCriteria(DeploymentQueryImpl deploymentQuery, Page page) {
    final String query = "selectDeploymentsByQueryCriteria";
    return getDbSqlSession().selectList(query, deploymentQuery, page);
  }

  @Override
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return getDbSqlSession().getSqlSession().selectList("selectResourceNamesByDeploymentId", deploymentId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Deployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectDeploymentByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectDeploymentCountByNativeQuery", parameterMap);
  }

  @Override
  public Deployment selectLatestDeployment(String deploymentName){
    return (Deployment) getDbSqlSession().selectOne("selectLatestDeployment", deploymentName);
  }

}
