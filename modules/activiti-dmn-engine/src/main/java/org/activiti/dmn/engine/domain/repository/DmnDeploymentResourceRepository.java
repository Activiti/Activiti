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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.activiti.dmn.engine.domain.entity.DmnDeploymentResource;

public class DmnDeploymentResourceRepository extends BaseRepository<DmnDeploymentResource> {

    public DmnDeploymentResourceRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, DmnDeploymentResource.class);
    }
    
    public List<DmnDeploymentResource> findResourcesByDeploymentId(Long deploymentId) {
        String query = "SELECT d FROM DmnDeploymentResource d WHERE d.deploymentId = :deploymentId";
        return getQueryResult(query, "deploymentId", deploymentId);
    }

    public DmnDeploymentResource findResourcesByDeploymentIdAndResourceName(Long deploymentId, String resourceName) {
        String query = "SELECT d FROM DmnDeploymentResource d WHERE d.deploymentId = :deploymentId and d.name = :name";
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("deploymentId", deploymentId);
        parameterMap.put("name", resourceName);
        return getSingleResult(query, parameterMap);
    }
}
