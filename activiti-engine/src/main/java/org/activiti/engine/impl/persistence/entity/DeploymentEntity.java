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

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.api.internal.Internal;
import org.activiti.engine.repository.Deployment;

/**


 */
@Internal
public interface DeploymentEntity extends Deployment, Entity {

  void addResource(ResourceEntity resource);

  Map<String, ResourceEntity> getResources();

  void addDeployedArtifact(Object deployedArtifact);

  <T> List<T> getDeployedArtifacts(Class<T> clazz);

  void setName(String name);

  void setCategory(String category);
  
  void setKey(String key);

  void setTenantId(String tenantId);

  void setResources(Map<String, ResourceEntity> resources);

  void setDeploymentTime(Date deploymentTime);

  boolean isNew();

  void setNew(boolean isNew);

  String getEngineVersion();

  void setEngineVersion(String engineVersion);

  Integer getVersion();

  void setVersion(Integer version);

  String getProjectReleaseVersion();

  void setProjectReleaseVersion(String projectReleaseVersion);

}
