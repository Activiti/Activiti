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

package org.activiti.engine.impl.persistence.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.Deployment;
import org.activiti.impl.persistence.PersistentObject;


/**
 * @author Tom Baeyens
 */
public class DeploymentEntity implements Serializable, Deployment, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String name;
  protected Map<String, ResourceEntity> resources;
  protected Date deploymentTime;
  
  public ResourceEntity getResource(String resourceName) {
    return getResources().get(resourceName);
  }

  public void addResource(ResourceEntity resource) {
    if (resources==null) {
      resources = new HashMap<String, ResourceEntity>();
    }
    resources.put(resource.getName(), resource);
  }

  public Object getPersistentState() {
    // properties of this entity are immutable
    // so always the same value is returned
    // so never will an update be issued for a DeploymentEntity
    return DeploymentEntity.class;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public Map<String, ResourceEntity> getResources() {
    return resources;
  }
  
  public void setResources(Map<String, ResourceEntity> resources) {
    this.resources = resources;
  }
  
  public Date getDeploymentTime() {
    return deploymentTime;
  }
  
  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }
}
