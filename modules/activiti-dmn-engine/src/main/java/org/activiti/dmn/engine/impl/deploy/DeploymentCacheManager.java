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
package org.activiti.dmn.engine.impl.deploy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.domain.entity.DmnDecisionTable;
import org.activiti.dmn.engine.domain.entity.DmnDeployment;
import org.activiti.dmn.engine.domain.entity.DmnDeploymentResource;
import org.activiti.dmn.engine.domain.repository.DmnRepositoryManager;
import org.activiti.dmn.model.DmnDefinition;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DeploymentCacheManager {

    protected DmnEngineConfiguration engineConfig;
    protected DeploymentCache<DecisionTableCacheEntry> decisionCache;
    
    public DeploymentCacheManager(DeploymentCache<DecisionTableCacheEntry> decisionCache, DmnEngineConfiguration engineConfig) {
        this.decisionCache = decisionCache;
        this.engineConfig = engineConfig;
    }

    public DmnDecisionTable findDeployedDecisionById(Long decisionId) {
        if (decisionId == null) {
            throw new ActivitiDmnException("Invalid decision id : null");
        }

        // first try the cache
        DecisionTableCacheEntry cacheEntry = decisionCache.get(decisionId);
        DmnDecisionTable decision = cacheEntry != null ? cacheEntry.getDecisionTable() : null;

        if (decision == null) {
            decision = engineConfig.getDmnRepositoryManager().getDecisionTableRepository().getObjectById(decisionId);
            if (decision == null) {
                throw new ActivitiDmnObjectNotFoundException("no deployed decision found with id '" + decisionId + "'");
            }
            decision = resolveDecision(decision).getDecisionTable();
        }
        return decision;
    }

    public DmnDecisionTable findDeployedLatestDecisionByKey(String decisionKey) {
        DmnDecisionTable decision = engineConfig.getDmnRepositoryManager().getDecisionTableRepository().findLatestDecisionTableByKey(decisionKey);

        if (decision == null) {
            throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionKey + "'");
        }
        decision = resolveDecision(decision).getDecisionTable();
        return decision;
    }

    public DmnDecisionTable findDeployedLatestDecisionByKeyAndTenantId(String decisionKey, String tenantId) {
        DmnDecisionTable decision = engineConfig.getDmnRepositoryManager().getDecisionTableRepository().findLatestDecisionTableByKeyAndTenantId(decisionKey, tenantId);
        
        if (decision == null) {
            throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key '" + decisionKey + "' for tenant identifier '" + tenantId + "'");
        }
        decision = resolveDecision(decision).getDecisionTable();
        return decision;
    }

    public DmnDecisionTable findDeployedDecisionByKeyAndVersion(String decisionKey, int decisionVersion) {
        DmnDecisionTable decision = engineConfig.getDmnRepositoryManager().getDecisionTableRepository().findDecisionTableByKeyAndVersion(decisionKey, decisionVersion);
        
        if (decision == null) {
            throw new ActivitiDmnObjectNotFoundException("no decisions deployed with key = '" + decisionKey + "' and version = '" + decisionVersion + "'");
        }
        
        decision = resolveDecision(decision).getDecisionTable();
        return decision;
    }

    /**
     * Resolving the decision will fetch the DMN, parse it and store the
     * {@link DmnDefinition} in memory.
     */
    public DecisionTableCacheEntry resolveDecision(DmnDecisionTable decision) {
        Long decisionId = decision.getId();
        Long deploymentId = decision.getDeploymentId();

        DecisionTableCacheEntry cachedDecision = decisionCache.get(decisionId);

        if (cachedDecision == null) {
            DmnRepositoryManager repositoryManager = engineConfig.getDmnRepositoryManager();
            DmnDeployment deployment = repositoryManager.getDeploymentRepository().getObjectById(deploymentId);
            List<DmnDeploymentResource> resources = repositoryManager.getDeploymentResourceRepository().findResourcesByDeploymentId(deploymentId);
            Map<String, DmnDeploymentResource> resourceMap = new HashMap<String, DmnDeploymentResource>();
            if (resources != null && resources.size() > 0) {
                for (DmnDeploymentResource resource : resources) {
                    resourceMap.put(resource.getName(), resource);
                }
            }
            deployment.setNew(false);
            engineConfig.getDmnDeployer().deploy(deployment, resourceMap, null);
            cachedDecision = decisionCache.get(decisionId);

            if (cachedDecision == null) {
                throw new ActivitiDmnException("deployment '" + deploymentId + "' didn't put decision '" + decisionId + "' in the cache");
            }
        }
        return cachedDecision;
    }

    public void removeDeploymentFromCache(Long deploymentId) {
        List<DmnDecisionTable> decisions = engineConfig.getDmnRepositoryManager().getDecisionTableRepository().findDecisionTablesByDeploymentId(deploymentId);

        for (DmnDecisionTable decision : decisions) {
            decisionCache.remove(decision.getId());
        }
    }

    public DeploymentCache<DecisionTableCacheEntry> getDecisionCache() {
        return decisionCache;
    }

    public void setDecisionCache(DeploymentCache<DecisionTableCacheEntry> decisionCache) {
        this.decisionCache = decisionCache;
    }
}
