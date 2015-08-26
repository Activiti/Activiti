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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.AppDefinitionServiceRepresentation;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.codahale.metrics.annotation.Timed;

@RestController
public class RuntimeAppDefinitionsResource {
	
	@Inject
	protected AppDefinitionService appDefinitionService;
	
	@Inject
	protected RuntimeAppDefinitionService runtimeAppDefinitionService;

	
	@RequestMapping(value = "/rest/editor/app-definitions", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public ResultListDataRepresentation getAppDefinitions() {
	    User user = SecurityUtils.getCurrentUserObject();
	    List<AppDefinitionServiceRepresentation> appDefinitions = appDefinitionService.getDeployableAppDefinitions(user);
		    
	    List<RuntimeAppDefinition> selectedAppDefinitions = runtimeAppDefinitionService.getDefinitionsForUser(user);
	    List<Long> selectedAppDefIds = new ArrayList<Long>();
	    for (RuntimeAppDefinition appDef : selectedAppDefinitions) {
            selectedAppDefIds.add(appDef.getModelId());
        }
	    
	    List<AppDefinitionServiceRepresentation> resultList = new ArrayList<AppDefinitionServiceRepresentation>();
	    for (AppDefinitionServiceRepresentation appDef : appDefinitions) {
            if (selectedAppDefIds.contains(appDef.getId()) == false) {
                resultList.add(appDef);
            }
        }
	    
		ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
		return result;
	}
}
