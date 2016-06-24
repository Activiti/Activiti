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
package com.activiti.model.runtime;

import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.model.common.AbstractRepresentation;

public class AppDefinitionRepresentation extends AbstractRepresentation {

    private Long id;
    private String defaultAppId; // Set for default apps (kickstart, tasks, idm, analytics,...)
    private String name;
    private String description;
    private Long modelId;
    private String theme;
    private String icon;
    private String deploymentId;
    private Long tenantId;
    
    public AppDefinitionRepresentation() {
    }

    public AppDefinitionRepresentation(RuntimeAppDefinition appDefinition) {
        this.id = appDefinition.getId();
        this.name = appDefinition.getName();
        this.description = appDefinition.getDescription();
        this.modelId = appDefinition.getModelId();
        this.deploymentId = appDefinition.getDeploymentId();
        this.tenantId = appDefinition.getTenantId();
    }
    
    public static AppDefinitionRepresentation createDefaultAppDefinitionRepresentation(String id) {
    	AppDefinitionRepresentation appDefinitionRepresentation = new AppDefinitionRepresentation();
    	appDefinitionRepresentation.setDefaultAppId(id);
    	return appDefinitionRepresentation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
