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
package org.activiti.dmn.engine.impl.deployer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.DmnEngineConfiguration;
import org.activiti.dmn.engine.domain.entity.DmnDecisionTable;
import org.activiti.dmn.engine.domain.entity.DmnDeployment;
import org.activiti.dmn.engine.domain.entity.DmnDeploymentResource;
import org.activiti.dmn.engine.domain.repository.DmnDecisionTableRepository;
import org.activiti.dmn.engine.domain.repository.DmnDeploymentResourceRepository;
import org.activiti.dmn.engine.impl.DeploymentSettings;
import org.activiti.dmn.engine.impl.deploy.DecisionTableCacheEntry;
import org.activiti.dmn.engine.impl.deploy.Deployer;
import org.activiti.dmn.engine.impl.parser.DmnParse;
import org.activiti.dmn.engine.impl.parser.DmnParseFactory;
import org.activiti.dmn.model.Decision;
import org.activiti.dmn.model.DmnDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class DmnDeployer implements Deployer {

    private static final Logger log = LoggerFactory.getLogger(DmnDeployer.class);

    public static final String[] DMN_RESOURCE_SUFFIXES = new String[] { "dmn" };
    
    protected DmnEngineConfiguration dmnEngineConfig;
    protected DmnParseFactory dmnParseFactory;
    
    public DmnDeployer(DmnParseFactory dmnParseFactory, DmnEngineConfiguration dmnEngineConfig) {
        this.dmnParseFactory = dmnParseFactory;
        this.dmnEngineConfig = dmnEngineConfig;
    }

    public void deploy(DmnDeployment deployment, Map<String, DmnDeploymentResource> resourceMap, Map<String, Object> deploymentSettings) {
        log.debug("Processing deployment {}", deployment.getName());

        List<DmnDecisionTable> decisions = new ArrayList<DmnDecisionTable>();
        Map<String, Decision> decisionModels = new HashMap<String, Decision>();
        Map<String, DmnDefinition> dmnModels = new HashMap<String, DmnDefinition>();
        
        for (DmnDeploymentResource resource : resourceMap.values()) {

            log.info("Processing resource {}", resource.getName());
            if (isDmnResource(resource.getName())) {
                byte[] bytes = resource.getResourceBytes();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

                DmnParse dmnParse = dmnParseFactory.createParse().sourceInputStream(inputStream)
                        .setSourceSystemId(resource.getName())
                        .deployment(deployment)
                        .name(resource.getName());

                if (deploymentSettings != null) {

                    // Schema validation if needed
                    if (deploymentSettings.containsKey(DeploymentSettings.IS_DMN_XSD_VALIDATION_ENABLED)) {
                        dmnParse.setValidateSchema((Boolean) deploymentSettings.get(DeploymentSettings.IS_DMN_XSD_VALIDATION_ENABLED));
                    }

                } else {
                    // On redeploy, we assume it is validated at the first deploy
                    dmnParse.setValidateSchema(true);
                }

                dmnParse.execute(dmnEngineConfig);

                for (DmnDecisionTable decision : dmnParse.getDecisions()) {
                    decision.setResourceName(resource.getName());

                    if (deployment.getTenantId() != null) {
                        decision.setTenantId(deployment.getTenantId()); // decision inherits the tenant id
                    }

                    decisions.add(decision);

                    decisionModels.put(decision.getKey(), dmnParse.getDmnDefinition().getDrgElementById(decision.getKey()));
                    dmnModels.put(decision.getKey(), dmnParse.getDmnDefinition());
                }
            }
        }

        // check if there are decisions with the same key to prevent database unique index violation
        List<String> keyList = new ArrayList<String>();
        for (DmnDecisionTable decision : decisions) {
            if (keyList.contains(decision.getKey())) {
                throw new ActivitiDmnException("The deployment contains decisions with the same key (decision id attribute), this is not allowed");
            }
            keyList.add(decision.getKey());
        }

        for (DmnDecisionTable decision : decisions) {
            DmnDecisionTableRepository decisionTableRepository = dmnEngineConfig.getDmnRepositoryManager().getDecisionTableRepository();
            if (deployment.isNew()) {
                int decisionVersion;

                DmnDecisionTable latestDecision = null;
                if (decision.getTenantId() != null && !DmnEngineConfiguration.NO_TENANT_ID.equals(decision.getTenantId())) {
                    latestDecision = decisionTableRepository.findLatestDecisionTableByKeyAndTenantId(decision.getKey(), decision.getTenantId());
                } else {
                    latestDecision = decisionTableRepository.findLatestDecisionTableByKey(decision.getKey());
                }

                if (latestDecision != null) {
                    decisionVersion = latestDecision.getVersion() + 1;
                } else {
                    decisionVersion = 1;
                }

                decision.setVersion(decisionVersion);
                decision.setDeploymentId(deployment.getId());

                decisionTableRepository.saveObject(decision);
                
                DmnDeploymentResourceRepository resourceRepository = dmnEngineConfig.getDmnRepositoryManager().getDeploymentResourceRepository();
                for (DmnDeploymentResource resource : resourceMap.values()) {
                    if (resource.getName().equals(decision.getResourceName())) {
                        resource.setDeployment(deployment.getId());
                        resourceRepository.saveObject(resource);
                    }
                }

            } else {
                Long deploymentId = deployment.getId();
                decision.setDeploymentId(deploymentId);

                DmnDecisionTable persistedDecision = null;
                if (decision.getTenantId() == null || DmnEngineConfiguration.NO_TENANT_ID.equals(decision.getTenantId())) {
                    persistedDecision = decisionTableRepository.findDecisionTableByDeploymentAndKey(deploymentId, decision.getKey());
                } else {
                    persistedDecision = decisionTableRepository.findDecisionTableByDeploymentAndKeyAndTenantId(deploymentId,
                            decision.getKey(), decision.getTenantId());
                }

                if (persistedDecision != null) {
                    decision.setId(persistedDecision.getId());
                    decision.setVersion(persistedDecision.getVersion());
                }
            }

            // Add to cache
            DecisionTableCacheEntry cacheEntry = new DecisionTableCacheEntry(decision, dmnModels.get(decision.getKey()), decisionModels.get(decision.getKey()));
            dmnEngineConfig.getDecisionCache().add(decision.getId(), cacheEntry);
        }
    }

    protected boolean isDmnResource(String resourceName) {
        for (String suffix : DMN_RESOURCE_SUFFIXES) {
            if (resourceName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
    public void setDmnEngineConfiguration(DmnEngineConfiguration dmnEngineConfig) {
        this.dmnEngineConfig = dmnEngineConfig;
    }

    public void setDmnParseFactory(DmnParseFactory dmnParseFactory) {
        this.dmnParseFactory = dmnParseFactory;
    }
}
