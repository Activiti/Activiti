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
package org.activiti.app.model.runtime;

import org.activiti.app.model.common.AbstractRepresentation;

public class AppDefinitionRepresentation extends AbstractRepresentation {

  private String defaultAppId; // Set for default apps (kickstart, tasks, idm, analytics,...)
  private String name;
  private String description;
  private Long modelId;
  private String theme;
  private String icon;
  private String deploymentId;
  private String deploymentKey;
  private Long tenantId;

  public static AppDefinitionRepresentation createDefaultAppDefinitionRepresentation(String id) {
    AppDefinitionRepresentation appDefinitionRepresentation = new AppDefinitionRepresentation();
    appDefinitionRepresentation.setDefaultAppId(id);
    return appDefinitionRepresentation;
  }

  public String getDefaultAppId() {
    return defaultAppId;
  }

  public void setDefaultAppId(String defaultAppId) {
    this.defaultAppId = defaultAppId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getModelId() {
    return modelId;
  }

  public void setModelId(Long modelId) {
    this.modelId = modelId;
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public String getDeploymentKey() {
    return deploymentKey;
  }

  public void setDeploymentKey(String deploymentKey) {
    this.deploymentKey = deploymentKey;
  }

  public Long getTenantId() {
    return tenantId;
  }

  public void setTenantId(Long tenantId) {
    this.tenantId = tenantId;
  }
}
