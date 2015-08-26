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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.AppDefinitionPublishRepresentation;
import com.activiti.model.editor.AppDefinitionRepresentation;
import com.activiti.model.editor.AppDefinitionSaveRepresentation;
import com.activiti.model.editor.AppDefinitionUpdateResultRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;

@RestController
public class AppDefinitionResource extends AbstractAppDefinitionResource {
	
    private static final Logger logger = LoggerFactory.getLogger(AppDefinitionResource.class);
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public AppDefinitionRepresentation getAppDefinition(@PathVariable("modelId") Long modelId) {
        Model model = getModel(modelId, true, false);
        return createAppDefinitionRepresentation(model);
    }
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}/history/{modelHistoryId}",
            method = RequestMethod.GET,
            produces = "application/json")
    @Timed
    public AppDefinitionRepresentation getAppDefinitionHistory(@PathVariable Long modelId, @PathVariable Long modelHistoryId) {
        ModelHistory model = getModelHistory(modelId, modelHistoryId, true, false);
        return createAppDefinitionRepresentation(model);
    }
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.PUT, produces = "application/json")
    @Timed
    public AppDefinitionUpdateResultRepresentation updateAppDefinition(@PathVariable("modelId") Long modelId, 
    		@RequestBody AppDefinitionSaveRepresentation updatedModel) {
    	
        AppDefinitionUpdateResultRepresentation result = new AppDefinitionUpdateResultRepresentation();
        
        User user = SecurityUtils.getCurrentUserObject();
        
        Model model = getModel(modelId, true, true);

        model.setName(updatedModel.getAppDefinition().getName());
        model.setDescription(updatedModel.getAppDefinition().getDescription());
        String editorJson = null;
        try {
            editorJson = objectMapper.writeValueAsString(updatedModel.getAppDefinition().getDefinition());
        } catch (Exception e) {
            logger.error("Error while processing app definition json " + modelId, e);
            throw new InternalServerErrorException("App definition could not be saved " + modelId);
        }
        
        model = modelService.saveModel(model, editorJson, null, false, null, user);
        
        if (updatedModel.isPublish()) {
        	return super.publishAppDefinition(modelId, new AppDefinitionPublishRepresentation(null, updatedModel.getForce()));
        } else {
	        AppDefinitionRepresentation appDefinition = new AppDefinitionRepresentation(model);
	        appDefinition.setDefinition(updatedModel.getAppDefinition().getDefinition());
	        result.setAppDefinition(appDefinition);
	        return result;
        }
    }
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}/publish", method = RequestMethod.POST, produces = "application/json")
    @Timed
    public AppDefinitionUpdateResultRepresentation publishAppDefinition(@PathVariable("modelId") Long modelId, 
            @RequestBody AppDefinitionPublishRepresentation publishModel) {
        
    	return super.publishAppDefinition(modelId, publishModel);	
    	
    }
    
    @Timed
    @RequestMapping(value="/rest/app-definitions/{modelId}/export", method = RequestMethod.GET)
    public void exportAppDefinition(HttpServletResponse response, @PathVariable Long modelId) throws IOException {  
        super.exportAppDefinition(response, modelId);
    }
    
    @Timed
    @Transactional
    @RequestMapping(value="/rest/app-definitions/{modelId}/import", method = RequestMethod.POST, produces = "application/json")
    public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @PathVariable Long modelId, @RequestParam("file") MultipartFile file) {  
        return super.importAppDefinitionNewVersion(request, file, modelId);
    }
    
    @Timed
    @Transactional
    @RequestMapping(value="/rest/app-definitions/{modelId}/text/import", method = RequestMethod.POST)
    public String importAppDefinitionText(HttpServletRequest request, @PathVariable Long modelId, @RequestParam("file") MultipartFile file) {  
        
        AppDefinitionRepresentation appDefinitionRepresentation = super.importAppDefinitionNewVersion(request, file, modelId);
        String appDefinitionRepresentationJson = null;
        try {
            appDefinitionRepresentationJson = objectMapper.writeValueAsString(appDefinitionRepresentation);
        } catch (Exception e) {
            logger.error("Error while App Definition representation json", e);
            throw new InternalServerErrorException("App definition could not be saved");
        }

        return appDefinitionRepresentationJson;
    }
    
    @Timed
    @Transactional
    @RequestMapping(value="/rest/app-definitions/import", method = RequestMethod.POST, produces = "application/json")
    public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @RequestParam("file") MultipartFile file) {  
    	return super.importAppDefinition(request, file);
    }
    
    @Timed
    @Transactional
    @RequestMapping(value="/rest/app-definitions/text/import", method = RequestMethod.POST)
    public String importAppDefinitionText(HttpServletRequest request, @RequestParam("file") MultipartFile file) {  
        AppDefinitionRepresentation appDefinitionRepresentation = super.importAppDefinition(request, file);
        String appDefinitionRepresentationJson = null;
        try {
            appDefinitionRepresentationJson = objectMapper.writeValueAsString(appDefinitionRepresentation);
        } catch (Exception e) {
            logger.error("Error while App Definition representation json", e);
            throw new InternalServerErrorException("App definition could not be saved");
        }

        return appDefinitionRepresentationJson;
    }
}
