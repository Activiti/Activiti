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
package org.activiti.dmn.engine;

import java.io.InputStream;
import java.util.List;

import org.activiti.dmn.engine.domain.entity.DmnDecisionTable;
import org.activiti.dmn.engine.domain.entity.DmnDeployment;
import org.activiti.dmn.engine.repository.DmnDeploymentBuilder;
import org.activiti.dmn.model.DmnDefinition;

/**
 * Service providing access to the repository of process definitions and deployments.
 *
 * @author Tijs Rademakers
 * @author Yvo Swillens
 */
public interface DmnRepositoryService {

    DmnDeploymentBuilder createDeployment();

    DmnDeployment getDeployment(Long deploymentId);

    void deleteDeployment(Long deploymentId);

    void setDeploymentCategory(Long deploymentId, String category);

    List<String> getDeploymentResourceNames(Long deploymentId);

    InputStream getResourceAsStream(Long deploymentId, String resourceName);

    InputStream getDecisionModel(Long decisionDefinitionId);

    DmnDecisionTable getDecisionDefinition(Long decisionDefinitionId);

    DmnDefinition getDmnModel(Long decisionDefinitionId);

    DmnDecisionTable findLatestDecisionTableByKeyAndTenantId(String decisionTableKey, String tenantIdForUser);

    List<DmnDecisionTable> getDecisionTablesByDeploymentId(long deploymentId);

    List<DmnDecisionTable> getDecisionTables();

    List<DmnDecisionTable> getDecisionTablesByTenantId(String tenantId);

    DmnDecisionTable getDecisionTable(Long decisionTableId);
    
    DmnDecisionTable findDecisionTableByDeploymentAndKeyAndTenantId(Long deploymentId, String decisionTableKey, String tenantId);
    
    List<DmnDecisionTable> findDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId, String sortBy, String order, Integer start, Integer size);
    
    Long countDecisionTables(String nameLike, String keyLike, String tenantIdLike, Long deploymentId);
}
