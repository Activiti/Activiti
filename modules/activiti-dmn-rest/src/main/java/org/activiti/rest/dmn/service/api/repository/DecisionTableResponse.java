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
package org.activiti.rest.dmn.service.api.repository;

import org.activiti.dmn.api.DmnDecisionTable;

/**
 * @author Yvo Swillens
 */
public class DecisionTableResponse {

  protected String id;
  protected String url;
  protected String category;
  protected String name;
  protected String key;
  protected String description;
  protected int version;
  protected String resourceName;
  protected String deploymentId;
  protected String tenantId;

  public DecisionTableResponse(DmnDecisionTable decisionTable) {
    setId(decisionTable.getId());
    setCategory(decisionTable.getCategory());
    setName(decisionTable.getName());
    setKey(decisionTable.getKey());
    setDescription(decisionTable.getDescription());
    setVersion(decisionTable.getVersion());
    setResourceName(decisionTable.getResourceName());
    setDeploymentId(decisionTable.getDeploymentId());
    setTenantId(decisionTable.getTenantId());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }
}
