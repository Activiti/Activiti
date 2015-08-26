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
package com.activiti.rest.editor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.ModelHistory;
import com.activiti.model.common.BaseRestActionRepresentation;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.model.editor.ReviveModelResultRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.BadRequestException;
import com.codahale.metrics.annotation.Timed;

@RestController
public class ModelHistoryResource extends AbstractModelHistoryResource {

    @RequestMapping(value = "/rest/models/{modelId}/history",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public ResultListDataRepresentation getModelHistoryCollection(@PathVariable Long modelId, 
            @RequestParam(value="includeLatestVersion", required=false) Boolean includeLatestVersion) {

    	return super.getModelHistoryCollection(modelId, includeLatestVersion);
      
    }
    
    @RequestMapping(value = "/rest/models/{modelId}/history/{modelHistoryId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public ModelRepresentation getProcessModelHistory(@PathVariable Long modelId, @PathVariable Long modelHistoryId) {
       return super.getProcessModelHistory(modelId, modelHistoryId);
    }
    
    @RequestMapping(value = "/rest/models/{modelId}/history/{modelHistoryId}",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public ReviveModelResultRepresentation executeProcessModelHistoryAction(@PathVariable Long modelId, 
            @PathVariable Long modelHistoryId, @RequestBody(required=true) BaseRestActionRepresentation action) {
        
        // In order to execute actions on a historic process model, write permission is needed
        ModelHistory modelHistory = getModelHistory(modelId, modelHistoryId, true, true);
        
        if("useAsNewVersion".equals(action.getAction())) {
            return modelService.reviveProcessModelHistory(modelHistory, SecurityUtils.getCurrentUserObject(), action.getComment());
        } else {
            throw new BadRequestException("Invalid action to execute on model history " + modelHistoryId + ": " + action.getAction());
        }
    }
}
