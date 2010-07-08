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
package org.activiti.impl.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.Deployment;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.interceptor.CommandContext;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentImpl implements Serializable, Deployment {

  private static final long serialVersionUID = 1L;
  
  protected String id;

  protected String name;

  protected boolean resourcesInitialized = false;
  protected Map<String, ByteArrayImpl> resources;
  
  protected Date deploymentTime;
  
  protected boolean isNew = false;;

  public void setResources(Map<String, ByteArrayImpl> resources) {
    this.resources = resources;
  }

  public DeploymentImpl() {
  }
  
  /** constructor for new deployments {@link DeploymentBuilderImpl#deployment} */
  public static DeploymentImpl create() {
    DeploymentImpl deployment = new DeploymentImpl();
    deployment.isNew = true;
    deployment.resourcesInitialized = true;
    return deployment;
  }
  
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
  
  public Date getDeploymentTime() {
    return deploymentTime;
  }
  
  public void setDeploymentTime(Date deploymentTime) {
    this.deploymentTime = deploymentTime;
  }

  public boolean isNew() {
    return isNew;
  }

  public Map<String, ByteArrayImpl> getResources() {
    if (!resourcesInitialized) {
      List<ByteArrayImpl> resourceList = CommandContext
        .getCurrent()
        .getPersistenceSession()
        .findDeploymentResources(id);
      resources = new HashMap<String, ByteArrayImpl>();
      for (ByteArrayImpl resource : resourceList) {
        resources.put(resource.getName(), resource);
      }
    }
    return resources;
  }
  
  public ByteArrayImpl getResource(String resourceName) {
  	return resources.get(resourceName);
  }
  
}
