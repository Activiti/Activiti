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

package org.activiti.dmn.engine.impl.persistence.entity;

import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.api.DmnDeployment;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.DmnDeploymentQueryImpl;
import org.activiti.dmn.engine.impl.Page;
import org.activiti.dmn.engine.impl.persistence.entity.data.DataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.DmnDeploymentDataManager;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DmnDeploymentEntityManagerImpl extends AbstractEntityManager<DmnDeploymentEntity> implements DmnDeploymentEntityManager {

  protected DmnDeploymentDataManager deploymentDataManager;
  
  public DmnDeploymentEntityManagerImpl(DmnEngineConfiguration dmnEngineConfiguration, DmnDeploymentDataManager deploymentDataManager) {
    super(dmnEngineConfiguration);
    this.deploymentDataManager = deploymentDataManager;
  }
  
  @Override
  protected DataManager<DmnDeploymentEntity> getDataManager() {
    return deploymentDataManager;
  }
  
  @Override
  public void insert(DmnDeploymentEntity deployment) {
    super.insert(deployment);

    for (ResourceEntity resource : deployment.getResources().values()) {
      resource.setDeploymentId(deployment.getId());
      getResourceEntityManager().insert(resource);
    }
  }

  @Override
  public void deleteDeployment(String deploymentId) {
    deleteDecisionTablesForDeployment(deploymentId);
    getResourceEntityManager().deleteResourcesByDeploymentId(deploymentId);
    delete(findById(deploymentId));
  }
  
  protected void deleteDecisionTablesForDeployment(String deploymentId) {
    getDecisionTableEntityManager().deleteDecisionTablesByDeploymentId(deploymentId);
  }
  
  protected DecisionTableEntity findLatestDecisionTable(DmnDecisionTable decisionTable) {
    DecisionTableEntity latestDecisionTable = null;
    if (decisionTable.getTenantId() != null && !DmnEngineConfiguration.NO_TENANT_ID.equals(decisionTable.getTenantId())) {
      latestDecisionTable = getDecisionTableEntityManager()
          .findLatestDecisionTableByKeyAndTenantId(decisionTable.getKey(), decisionTable.getTenantId());
    } else {
      latestDecisionTable = getDecisionTableEntityManager().findLatestDecisionTableByKey(decisionTable.getKey());
    }
    return latestDecisionTable;
  }

  @Override
  public DmnDeploymentEntity findLatestDeploymentByName(String deploymentName) {
    return deploymentDataManager.findLatestDeploymentByName(deploymentName);
  }

  @Override
  public long findDeploymentCountByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery) {
    return deploymentDataManager.findDeploymentCountByQueryCriteria(deploymentQuery);
  }

  @Override
  public List<DmnDeployment> findDeploymentsByQueryCriteria(DmnDeploymentQueryImpl deploymentQuery, Page page) {
    return deploymentDataManager.findDeploymentsByQueryCriteria(deploymentQuery, page);
  }

  @Override
  public List<String> getDeploymentResourceNames(String deploymentId) {
    return deploymentDataManager.getDeploymentResourceNames(deploymentId);
  }

  @Override
  public List<DmnDeployment> findDeploymentsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return deploymentDataManager.findDeploymentsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  @Override
  public long findDeploymentCountByNativeQuery(Map<String, Object> parameterMap) {
    return deploymentDataManager.findDeploymentCountByNativeQuery(parameterMap);
  }


  public DmnDeploymentDataManager getDeploymentDataManager() {
    return deploymentDataManager;
  }


  public void setDeploymentDataManager(DmnDeploymentDataManager deploymentDataManager) {
    this.deploymentDataManager = deploymentDataManager;
  }
  
}
