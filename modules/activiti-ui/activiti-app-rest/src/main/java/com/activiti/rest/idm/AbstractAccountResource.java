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
package com.activiti.rest.idm;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;

import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.model.editor.LightAppRepresentation;
import com.activiti.model.idm.UserRepresentation;
import com.activiti.security.ActivitiAppUser;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAccountResource {
    
    private final Logger logger = LoggerFactory.getLogger(AbstractAccountResource.class);
    
    @Autowired
    private RuntimeAppDefinitionService runtimeAppDefinitionService;
    
    @Autowired
    private ObjectMapper objectMapper;

    public UserRepresentation getAccount(HttpServletResponse response, Boolean includeApps) {
    	ActivitiAppUser user = SecurityUtils.getCurrentActivitiAppUser();
        UserRepresentation userRepresentation = convert(user);

        if (includeApps != null && includeApps) {
            List<RuntimeAppDefinition> appDefinitions = runtimeAppDefinitionService.getDefinitionsForUser(user.getUserObject());
            for (RuntimeAppDefinition runtimeAppDefinition : appDefinitions) {
                LightAppRepresentation appRepresentation = new LightAppRepresentation();
                appRepresentation.setId(runtimeAppDefinition.getId());
                appRepresentation.setName(runtimeAppDefinition.getName());
                appRepresentation.setDescription(runtimeAppDefinition.getDescription());
                try {
                    AppDefinition appDefinition = objectMapper.readValue(runtimeAppDefinition.getDefinition(), AppDefinition.class);
                    appRepresentation.setTheme(appDefinition.getTheme());
                    appRepresentation.setIcon(appDefinition.getIcon());
                } catch (Exception e) {
                    logger.error("Error reading app definition", e);
                }
                userRepresentation.getApps().add(appRepresentation);
            }
        }
        
        return userRepresentation;
    }
    
    protected UserRepresentation convert(ActivitiAppUser activitiAppUser) {
    	UserRepresentation userRepresentation = new UserRepresentation(activitiAppUser.getUserObject(), true, true);
    	
    	List<String> capabilities = new ArrayList<String>(activitiAppUser.getAuthorities().size());
    	for (GrantedAuthority grantedAuthority : activitiAppUser.getAuthorities()) {
        	capabilities.add(grantedAuthority.getAuthority());
        }
    	userRepresentation.setCapabilities(capabilities);
    	return userRepresentation;
    }
    
}
