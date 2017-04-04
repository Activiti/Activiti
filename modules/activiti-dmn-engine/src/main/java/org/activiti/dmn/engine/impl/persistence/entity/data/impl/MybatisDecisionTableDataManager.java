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
package org.activiti.dmn.engine.impl.persistence.entity.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.DecisionTableQueryImpl;
import org.activiti.dmn.engine.impl.Page;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityImpl;
import org.activiti.dmn.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.DecisionTableDataManager;

/**
 * @author Joram Barrez
 */
public class MybatisDecisionTableDataManager extends AbstractDataManager<DecisionTableEntity> implements DecisionTableDataManager {
  
  public MybatisDecisionTableDataManager(DmnEngineConfiguration dmnEngineConfiguration) {
    super(dmnEngineConfiguration);
  }

  @Override
  public Class<? extends DecisionTableEntity> getManagedEntityClass() {
    return DecisionTableEntityImpl.class;
  }
  
  @Override
  public DecisionTableEntity create() {
    return new DecisionTableEntityImpl();
  }
  
  @Override
  public DecisionTableEntity findLatestDecisionTableByKey(String decisionTableKey) {
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectLatestDecisionTableByKey", decisionTableKey);
  }

  @Override
  public DecisionTableEntity findLatestDecisionTableByKeyAndTenantId(String decisionTableKey, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("decisionTableKey", decisionTableKey);
    params.put("tenantId", tenantId);
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectLatestDecisionTableByKeyAndTenantId", params);
  }
  
  @Override
  public DecisionTableEntity findLatestDecisionTableByKeyAndParentDeploymentId(String decisionTableKey, String parentDeploymentId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("decisionTableKey", decisionTableKey);
    params.put("parentDeploymentId", parentDeploymentId);
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectLatestDecisionTableByKeyAndParentDeploymentId", params);
  }
  
  @Override
  public DecisionTableEntity findLatestDecisionTableByKeyParentDeploymentIdAndTenantId(String decisionTableKey, String parentDeploymentId, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("decisionTableKey", decisionTableKey);
    params.put("parentDeploymentId", parentDeploymentId);
    params.put("tenantId", tenantId);
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectLatestDecisionTableByKeyParentDeploymentIdAndTenantId", params);
  }

  @Override
  public void deleteDecisionTablesByDeploymentId(String deploymentId) {
    getDbSqlSession().delete("deleteDecisionTablesByDeploymentId", deploymentId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DmnDecisionTable> findDecisionTablesByQueryCriteria(DecisionTableQueryImpl decisionTableQuery, Page page) {
    return getDbSqlSession().selectList("selectDecisionTablesByQueryCriteria", decisionTableQuery, page);
  }

  @Override
  public long findDecisionTableCountByQueryCriteria(DecisionTableQueryImpl decisionTableQuery) {
    return (Long) getDbSqlSession().selectOne("selectDecisionTableCountByQueryCriteria", decisionTableQuery);
  }

  @Override
  public DecisionTableEntity findDecisionTableByDeploymentAndKey(String deploymentId, String decisionTableKey) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionTableKey", decisionTableKey);
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectDecisionTableByDeploymentAndKey", parameters);
  }

  @Override
  public DecisionTableEntity findDecisionTableByDeploymentAndKeyAndTenantId(String deploymentId, String decisionTableKey, String tenantId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("deploymentId", deploymentId);
    parameters.put("decisionTableKey", decisionTableKey);
    parameters.put("tenantId", tenantId);
    return (DecisionTableEntity) getDbSqlSession().selectOne("selectDecisionTableByDeploymentAndKeyAndTenantId", parameters);
  }
  
  @Override
  public DecisionTableEntity findDecisionTableByKeyAndVersion(String decisionTableKey, Integer decisionTableVersion) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("decisionTableKey", decisionTableKey);
    params.put("decisionTableVersion", decisionTableVersion);
    List<DecisionTableEntity> results = getDbSqlSession().selectList("selectDecisionTablesByKeyAndVersion", params);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiDmnException("There are " + results.size() + " decision tables with key = '" + decisionTableKey + "' and version = '" + decisionTableVersion + "'.");
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public DecisionTableEntity findDecisionTableByKeyAndVersionAndTenantId(String decisionTableKey, Integer decisionTableVersion, String tenantId) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("decisionTableKey", decisionTableKey);
    params.put("decisionTableVersion", decisionTableVersion);
    params.put("tenantId", tenantId);
    List<DecisionTableEntity> results = getDbSqlSession().selectList("selectDecisionTablesByKeyAndVersionAndTenantId", params);
    if (results.size() == 1) {
      return results.get(0);
    } else if (results.size() > 1) {
      throw new ActivitiDmnException("There are " + results.size() + " decision tables with key = '" + decisionTableKey + "' and version = '" + decisionTableVersion + "'.");
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<DmnDecisionTable> findDecisionTablesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectDecisionTableByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDecisionTableCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectDecisionTableCountByNativeQuery", parameterMap);
  }

  @Override
  public void updateDecisionTableTenantIdForDeployment(String deploymentId, String newTenantId) {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("tenantId", newTenantId);
    getDbSqlSession().update("updateDecisionTableTenantIdForDeploymentId", params);
  }
  
}
