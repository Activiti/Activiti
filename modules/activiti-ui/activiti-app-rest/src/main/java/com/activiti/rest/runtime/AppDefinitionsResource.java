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
package com.activiti.rest.runtime;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.runtime.AppDefinitionRepresentation;
import com.activiti.model.runtime.RuntimeAppDefinitionSaveRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.service.runtime.PermissionService;
import com.activiti.service.runtime.RuntimeAppDefinitionInternalService;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST controller for managing the app definitions.
 */
@RestController
public class AppDefinitionsResource extends AbstractAppDefinitionsResource {
    
    private final Logger logger = LoggerFactory.getLogger(AppDefinitionsResource.class);

    @Inject
    protected RuntimeAppDefinitionInternalService runtimeAppDefinitionService;
    
    @Inject
    protected ObjectMapper objectMapper;
    
    @Inject
    protected PermissionService permissionService;
    
	@RequestMapping(value = "/rest/runtime/app-definitions", method = RequestMethod.GET)
    @Timed
    public ResultListDataRepresentation getAppDefinitions() {
	    return super.getAppDefinitions();
    }
	
	@RequestMapping(value = "/rest/runtime/app-definitions/model/{modelId}", method = RequestMethod.GET)
    @Timed
    public AppDefinitionRepresentation getAppDefinitionForModel(@PathVariable("modelId") Long modelId) {
        AppDefinitionRepresentation result = null;
        RuntimeAppDefinition appDefinition = runtimeAppDefinitionService.getDefinitionForModelAndUser(modelId, SecurityUtils.getCurrentUserObject());
        if (appDefinition != null) {
            result = new AppDefinitionRepresentation(appDefinition);
        }
        return result;
    }
	
	@RequestMapping(value = "/rest/runtime/app-definitions", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
    @Timed
    public void deployAppDefinitions(@RequestBody RuntimeAppDefinitionSaveRepresentation saveObject) {
	    super.deployAppDefinitions(saveObject);
	}
	
	@RequestMapping(value = "/rest/runtime/app-definitions/{appDefinitionId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.OK)
    @Timed
	public void deleteAppDefinition(@PathVariable("appDefinitionId") Long appDefinitionId) {
	    RuntimeAppDefinition appDefinition = runtimeAppDefinitionService.getRuntimeAppDefinition(appDefinitionId);
	    
	    if (appDefinition == null) {
	        throw new NotFoundException("No app definition is found with id: " + appDefinitionId);
	    }

	    // Check permissions
	    User currentUser = SecurityUtils.getCurrentUserObject();
	    if (!permissionService.hasReadPermissionOnRuntimeApp(currentUser, appDefinition.getId())) {
	        throw new NotPermittedException("You are not allowed to access app definition with id: " + appDefinitionId);
	    }
	    
	    // Delete the connection between user and app definition. In case the service call returns
	    // null, the connection was already gone - no need to throw exception as the final result is the same
	    runtimeAppDefinitionService.deleteAppDefinitionForUser(currentUser, appDefinition);
	}
	
	@RequestMapping(value = "/rest/runtime/app-definitions/{appDefinitionId}", method = RequestMethod.GET)
    @Timed
    public AppDefinitionRepresentation getAppDefinition(@PathVariable("appDefinitionId") Long appDefinitionId) {
        RuntimeAppDefinition runtimeAppDefinition = runtimeAppDefinitionService.getRuntimeAppDefinition(appDefinitionId);
        
        if(runtimeAppDefinition == null) {
            throw new NotFoundException("No app definition is found with id: " + appDefinitionId);
        }

        // Check permissions
        if(!permissionService.hasReadPermissionOnRuntimeApp(SecurityUtils.getCurrentUserObject(), runtimeAppDefinition.getId())) {
            throw new NotPermittedException("You are not allowed to access app definition with id: " + appDefinitionId);
        }
        return createRepresentation(runtimeAppDefinition);
    }
	
}
