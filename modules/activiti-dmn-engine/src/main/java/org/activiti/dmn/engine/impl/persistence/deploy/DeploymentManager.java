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
package org.activiti.dmn.engine.impl.persistence.deploy;

import java.util.List;
import java.util.Map;

import org.activiti.dmn.api.DmnDecisionTable;
import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.impl.DecisionTableQueryImpl;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntity;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.dmn.model.DmnDefinition;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DeploymentManager {

  protected DmnEngineConfiguration engineConfig;
  protected DeploymentCache<DecisionTableCacheEntry> decisionCache;
  
  protected List<Deployer> deployers;
  protected DecisionTableEntityManager decisionTableEntityManager;
  protected DmnDeploymentEntityManager deploymentEntityManager;
  
  public DeploymentManager(DeploymentCache<DecisionTableCacheEntry> decisionCache, DmnEngineConfiguration engineConfig) {
    this.decisionCache = decisionCache;
    this.engineConfig = engineConfig;
  }
  
  public void deploy(DmnDeploymentEntity deployment) {
    deploy(deployment, null);
  }

  public void deploy(DmnDeploymentEntity deployment, Map<String, Object> deploymentSettings) {
    for (Deployer deployer : deployers) {
      deployer.deploy(deployment, deploymentSettings);
    }
  }

  public DecisionTableEntity findDeployedDecisionById(String decisionId) {
    if (decisionId == null) {
      throw new ActivitiDmnException("Invalid decision id : null");
    }

    // first try the cache
    DecisionTableCacheEntry cacheEntry = decisionCache.get(decisionId);
    DecisionTableEntity decisionTable = cacheEntry != null ? cacheEntry.getDecisionTableEntity() : null;

    if (decisionTable == null) {
      decisionTable = engineConfig.getDecisionTableEntityManager().findById(decisionId);
      if (decisionTable == null) {
        throw new ActivitiDmnObjectNotFoundException("no deployed decision found with id '" + decisionId + "'");
      }
      decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    }
    return decisionTable;
  }

  public DecisionTableEntity findDeployedLatestDecisionByKey(String decisionKey) {
    DecisionTableEntity decisionTable = decisionTableEntityManager.findLatestDecisionTableByKey(decisionKey);

    if (decisionTable == null) {
      throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionKey + "'");
    }
    decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    return decisionTable;
  }

  public DecisionTableEntity findDeployedLatestDecisionByKeyAndTenantId(String decisionKey, String tenantId) {
    DecisionTableEntity decisionTable = decisionTableEntityManager.findLatestDecisionTableByKeyAndTenantId(decisionKey, tenantId);
    
    if (decisionTable == null) {
      throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionKey + "' for tenant identifier '" + tenantId + "'");
    }
    decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    return decisionTable;
  }
  
  public DecisionTableEntity findDeployedLatestDecisionByKeyAndParentDeploymentId(String decisionTableKey, String parentDeploymentId) {
    DecisionTableEntity decisionTable = decisionTableEntityManager.findLatestDecisionTableByKeyAndParentDeploymentId(decisionTableKey, parentDeploymentId);
    
    if (decisionTable == null) {
      throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionTableKey + 
          "' for parent deployment id '" + parentDeploymentId + "'");
    }
    decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    return decisionTable;
  }
  
  public DecisionTableEntity findDeployedLatestDecisionByKeyParentDeploymentIdAndTenantId(String decisionTableKey, 
      String parentDeploymentId, String tenantId) {
    
    DecisionTableEntity decisionTable = decisionTableEntityManager.findLatestDecisionTableByKeyParentDeploymentIdAndTenantId(
        decisionTableKey, parentDeploymentId, tenantId);
    
    if (decisionTable == null) {
      throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionTableKey + 
          "' for parent deployment id '" + parentDeploymentId + "' and tenant identifier " + tenantId);
    }
    decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    return decisionTable;
  }

  public DecisionTableEntity findDeployedDecisionByKeyAndVersionAndTenantId(String decisionKey, int decisionVersion, String tenantId) {
    DecisionTableEntity decisionTable = decisionTableEntityManager.findDecisionTableByKeyAndVersionAndTenantId(decisionKey, decisionVersion, tenantId);
    
    if (decisionTable == null) {
      throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key = '" + decisionKey + "' and version = '" + decisionVersion + "'");
    }
    
    decisionTable = resolveDecisionTable(decisionTable).getDecisionTableEntity();
    return decisionTable;
  }

  /**
   * Resolving the decision will fetch the DMN, parse it and store the
   * {@link DmnDefinition} in memory.
   */
  public DecisionTableCacheEntry resolveDecisionTable(DmnDecisionTable decision) {
    String decisionId = decision.getId();
    String deploymentId = decision.getDeploymentId();

    DecisionTableCacheEntry cachedDecision = decisionCache.get(decisionId);

    if (cachedDecision == null) {
      DmnDeploymentEntity deployment = engineConfig.getDeploymentEntityManager().findById(deploymentId);
      List<ResourceEntity> resources = engineConfig.getResourceEntityManager().findResourcesByDeploymentId(deploymentId);
      for (ResourceEntity resource : resources) {
        deployment.addResource(resource);
      }
      
      deployment.setNew(false);
      deploy(deployment, null);
      cachedDecision = decisionCache.get(decisionId);

      if (cachedDecision == null) {
        throw new ActivitiDmnException("deployment '" + deploymentId + "' didn't put decision '" + decisionId + "' in the cache");
      }
    }
    return cachedDecision;
  }

  public void removeDeployment(String deploymentId) {

    DmnDeploymentEntity deployment = deploymentEntityManager.findById(deploymentId);
    if (deployment == null) {
      throw new ActivitiDmnObjectNotFoundException("Could not find a deployment with id '" + deploymentId + "'.");
    }

    // Remove any process definition from the cache
    List<DmnDecisionTable> decisionTables = new DecisionTableQueryImpl().deploymentId(deploymentId).list();
    
    // Delete data
    deploymentEntityManager.deleteDeployment(deploymentId);

    for (DmnDecisionTable decisionTable : decisionTables) {
      decisionCache.remove(decisionTable.getId());
    }
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }

  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public DeploymentCache<DecisionTableCacheEntry> getDecisionCache() {
      return decisionCache;
  }

  public void setDecisionCache(DeploymentCache<DecisionTableCacheEntry> decisionCache) {
      this.decisionCache = decisionCache;
  }

  public DecisionTableEntityManager getDecisionTableEntityManager() {
    return decisionTableEntityManager;
  }

  public void setDecisionTableEntityManager(DecisionTableEntityManager decisionTableEntityManager) {
    this.decisionTableEntityManager = decisionTableEntityManager;
  }

  public DmnDeploymentEntityManager getDeploymentEntityManager() {
    return deploymentEntityManager;
  }

  public void setDeploymentEntityManager(DmnDeploymentEntityManager deploymentEntityManager) {
    this.deploymentEntityManager = deploymentEntityManager;
  }
}
