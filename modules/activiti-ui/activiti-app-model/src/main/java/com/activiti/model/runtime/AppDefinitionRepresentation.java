/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
