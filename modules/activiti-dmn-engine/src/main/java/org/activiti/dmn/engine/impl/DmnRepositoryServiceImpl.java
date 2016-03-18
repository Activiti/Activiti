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
package org.activiti.dmn.engine.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.dmn.engine.ActivitiDmnObjectNotFoundException;
import org.activiti.dmn.engine.DmnRepositoryService;
import org.activiti.dmn.engine.domain.entity.DmnDecisionTable;
import org.activiti.dmn.engine.domain.entity.DmnDeployment;
import org.activiti.dmn.engine.domain.entity.DmnDeploymentResource;
import org.activiti.dmn.engine.domain.repository.DmnDecisionTableRepository;
import org.activiti.dmn.engine.domain.repository.DmnDeploymentRepository;
import org.activiti.dmn.engine.domain.repository.DmnDeploymentResourceRepository;
import org.activiti.dmn.engine.domain.repository.DmnRepositoryManager;
import org.activiti.dmn.engine.impl.repository.DmnDeploymentBuilderImpl;
import org.activiti.dmn.engine.repository.DmnDeploymentBuilder;
import org.activiti.dmn.model.DmnDefinition;

/**
 * @author Tijs Rademakers
 */
public class DmnRepositoryServiceImpl extends ServiceImpl implements DmnRepositoryService {

    public DmnDeploymentBuilder createDeployment() {
        return new DmnDeploymentBuilderImpl(this);
    }

    public DmnDeployment getDeployment(Long deploymentId) {
        return getDeploymentRepository().getObjectById(deploymentId);
    }
    
    public DmnDeployment deploy(DmnDeploymentBuilderImpl deploymentBuilder) {
        DmnDeployment deployment = deploymentBuilder.getDeployment();
        deployment.setDeployTime(new Date());
        getDeploymentRepository().saveObject(deployment);

        // Deployment settings
        Map<String, Object> deploymentSettings = new HashMap<String, Object>();
        deploymentSettings.put(DeploymentSettings.IS_DMN_XSD_VALIDATION_ENABLED, deploymentBuilder.isDmnXsdValidationEnabled());

        // Actually deploy
        engineConfig.getDmnDeployer().deploy(deployment, deploymentBuilder.getResourceMap(), deploymentSettings);

        return deployment;
    }

    public void deleteDeployment(Long deploymentId) {
        DmnRepositoryManager repositoryManager = engineConfig.getDmnRepositoryManager();
        DmnDeploymentRepository deploymentRepo = repositoryManager.getDeploymentRepository();
        DmnDeploymentResourceRepository deploymentResourceRepo = repositoryManager.getDeploymentResourceRepository();
        DmnDecisionTableRepository decisionTableRepo = repositoryManager.getDecisionTableRepository();

        DmnDeployment deployment = deploymentRepo.getObjectById(deploymentId);
        if (deployment == null) {
            throw new ActivitiDmnObjectNotFoundException("Deployment not found with id " + deploymentId);
        }

        List<DmnDeploymentResource> resources = deploymentResourceRepo.findResourcesByDeploymentId(deploymentId);
        if (resources != null && resources.isEmpty() == false) {
            for (DmnDeploymentResource resource : resources) {
                deploymentResourceRepo.removeObject(resource);
            }
        }

        List<DmnDecisionTable> decisions = decisionTableRepo.findDecisionTablesByDeploymentId(deploymentId);
        if (decisions != null && decisions.isEmpty() == false) {
            for (DmnDecisionTable decision : decisions) {
                decisionTableRepo.removeObject(decision);
            }
        }

        deploymentRepo.removeObject(deployment);
    }


    public List<DmnDecisionTable> getDecisionTablesByDeploymentId(long deploymentId) {
        DmnRepositoryManager repositoryManager = engineConfig.getDmnRepositoryManager();
        DmnDecisionTableRepository dmnDecisionTableRepository = repositoryManager.getDecisionTableRepository();

        return dmnDecisionTableRepository.findDecisionTablesByDeploymentId(deploymentId);
    }


    public void setDeploymentCategory(Long deploymentId, String category) {

    }

    public List<String> getDeploymentResourceNames(Long deploymentId) {
        return null;
    }

    public InputStream getResourceAsStream(Long deploymentId, String resourceName) {
        DmnRepositoryManager repositoryManager = engineConfig.getDmnRepositoryManager();
        DmnDeploymentResourceRepository dmnDeploymentResourceRepository = repositoryManager.getDeploymentResourceRepository();
        DmnDeploymentResource deploymentResource = dmnDeploymentResourceRepository.findResourcesByDeploymentIdAndResourceName(deploymentId, resourceName);

        return new ByteArrayInputStream(deploymentResource.getResourceBytes());
    }

    public void changeDeploymentTenantId(Long deploymentId, String newTenantId) {

    }

    public InputStream getDecisionModel(Long decisionDefinitionId) {
        return null;
    }

    public DmnDecisionTable getDecisionDefinition(Long decisionDefinitionId) {
        return null;
    }

    public DmnDefinition getDmnModel(Long decisionDefinitionId) {
        return null;
    }

    @Override
    public DmnDecisionTable findLatestDecisionTableByKeyAndTenantId(String decisionTableKey, String tenantIdForUser) {
      return getDecisionTableRepository().findLatestDecisionTableByKeyAndTenantId(decisionTableKey, tenantIdForUser);
    }

    @Override
    public List<DmnDecisionTable> getDecisionTablesByTenantId(String tenantId) {
        return getDecisionTableRepository().findDecisionTablesByTenantId(tenantId);
    }

    @Override
    public List<DmnDecisionTable> getDecisionTables() {
        return getDecisionTableRepository().findAll();
    }

    @Override
    public DmnDecisionTable getDecisionTable(Long decisionTableId) {
        return getDecisionTableRepository().getObjectById(decisionTableId);
    }
    
    @Override
    public List<DmnDecisionTable> findDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId, String sortBy, String order, Integer start, Integer size) {
        return getDecisionTableRepository().findDecisionTables(nameLike, keyLike, tenantIdLike, deploymentId, sortBy, order, start, size);
    }
    
    @Override
    public Long countDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId) {
        return getDecisionTableRepository().countDecisionTables(nameLike, keyLike, tenantIdLike, deploymentId);
    }
    
    @Override
    public DmnDecisionTable findDecisionTableByDeploymentAndKeyAndTenantId(Long deploymentId, String decisionTableKey, String tenantId) {
        return getDecisionTableRepository().findDecisionTableByDeploymentAndKeyAndTenantId(deploymentId, decisionTableKey, tenantId);
    }
}
