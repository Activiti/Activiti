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
package org.activiti.form.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.form.engine.FormEngineConfiguration;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class FormEntityImpl implements FormEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected String description;
  protected String key;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String parentDeploymentId;
  protected String resourceName;
  protected String tenantId = FormEngineConfiguration.NO_TENANT_ID;
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("category", this.category);
    return persistentState;
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getParentDeploymentId() {
    return parentDeploymentId;
  }

  public void setParentDeploymentId(String parentDeploymentId) {
    this.parentDeploymentId = parentDeploymentId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
  
  public String toString() {
    return "FormEntity[" + id + "]";
  }

}
