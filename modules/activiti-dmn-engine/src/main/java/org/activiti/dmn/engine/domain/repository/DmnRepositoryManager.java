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
package org.activiti.dmn.engine.domain.repository;

import javax.persistence.EntityManagerFactory;

import org.activiti.dmn.engine.DmnEngineConfiguration;

public class DmnRepositoryManager {

    protected DmnDeploymentRepository deploymentRepository;
    protected DmnDeploymentResourceRepository deploymentResourceRepository;
    protected DmnDecisionTableRepository decisionTableRepository;
    
    public DmnRepositoryManager(DmnEngineConfiguration engineConfig) {
        EntityManagerFactory entityManagerFactory = engineConfig.getEntityManagerFactory();
        deploymentRepository = new DmnDeploymentRepository(entityManagerFactory);
        deploymentResourceRepository = new DmnDeploymentResourceRepository(entityManagerFactory);
        decisionTableRepository = new DmnDecisionTableRepository(entityManagerFactory);
    }
    
    public DmnDeploymentRepository getDeploymentRepository() {
        return deploymentRepository;
    }
    
    public DmnDeploymentResourceRepository getDeploymentResourceRepository() {
        return deploymentResourceRepository;
    }
    
    public DmnDecisionTableRepository getDecisionTableRepository() {
        return decisionTableRepository;
    }
}
