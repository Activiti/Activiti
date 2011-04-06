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

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.impl.repository.ResourceEntity;


/**
 * @author Tom Baeyens
 */
public class ResourceManager extends AbstractManager {

  public void insertResource(ResourceEntity resource) {
    getPersistenceSession().insert(resource);
  }

  public void deleteResourcesByDeploymentId(String deploymentId) {
    getPersistenceSession().delete("deleteResourcesByDeploymentId", deploymentId);
  }
  
  public ResourceEntity findResourceByDeploymentIdAndResourceName(String deploymentId, String resourceName) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("deploymentId", deploymentId);
    params.put("resourceName", resourceName);
    return (ResourceEntity) getPersistenceSession().selectOne("selectResourceByDeploymentIdAndResourceName", params);
  }
  
  @SuppressWarnings("unchecked")
  public List<ResourceEntity> findResourcesByDeploymentId(String deploymentId) {
    return getPersistenceSession().selectList("selectResourcesByDeploymentId", deploymentId);
  }
  

}
