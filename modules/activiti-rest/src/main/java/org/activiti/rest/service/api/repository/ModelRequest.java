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

package org.activiti.rest.service.api.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Frederik Heremans
 */
public class ModelRequest {
  
  protected String name;
  protected String key; 
  protected String category;
  protected Integer version;
  protected String metaInfo;
  protected String deploymentId;
  protected String tenantId;
  
  protected boolean nameChanged;
  protected boolean keyChanged;
  protected boolean categoryChanged;
  protected boolean versionChanged;
  protected boolean metaInfoChanged;
  protected boolean deploymentChanged;
  protected boolean tenantChanged;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
    this.nameChanged = true;
  }
  
  public String getKey() {
    return key;
  }
  
  public void setKey(String key) {
    this.key = key;
    this.keyChanged = true;
  }
  
  public String getCategory() {
    return category;
  }
  
  public void setCategory(String category) {
    this.category = category;
    this.categoryChanged = true;
  }
  
  public Integer getVersion() {
    return version;
  }
  
  public void setVersion(Integer version) {
    this.version = version;
    this.versionChanged = true;
  }
  
  public String getMetaInfo() {
    return metaInfo;
  }
  
  public void setMetaInfo(String metaInfo) {
    this.metaInfo = metaInfo;
    this.metaInfoChanged = true;
  }
  
  public String getDeploymentId() {
    return deploymentId;
  } 
  
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    this.deploymentChanged = true;
  }
  
  public void setTenantId(String tenantId) {
  	tenantChanged = true;
	  this.tenantId = tenantId;
  }
  
  public String getTenantId() {
	  return tenantId;
  }
  
  @JsonIgnore
  public boolean isCategoryChanged() {
    return categoryChanged;
  }
  @JsonIgnore
  public boolean isKeyChanged() {
    return keyChanged;
  }
  @JsonIgnore
  public boolean isMetaInfoChanged() {
    return metaInfoChanged;
  }
  @JsonIgnore
  public boolean isNameChanged() {
    return nameChanged;
  }
  @JsonIgnore
  public boolean isVersionChanged() {
    return versionChanged;
  }
  @JsonIgnore
  public boolean isDeploymentChanged() {
    return deploymentChanged;
  }
  @JsonIgnore
  public boolean isTenantIdChanged() {
	  return tenantChanged;
  }
}
