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

@RestController
public class AppDefinitionResource extends AbstractAppDefinitionResource {
	
    private static final Logger logger = LoggerFactory.getLogger(AppDefinitionResource.class);
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.GET, produces = "application/json")
    public AppDefinitionRepresentation getAppDefinition(@PathVariable("modelId") Long modelId) {
        Model model = getModel(modelId, true, false);
        return createAppDefinitionRepresentation(model);
    }
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}/history/{modelHistoryId}",
            method = RequestMethod.GET,
            produces = "application/json")
    public AppDefinitionRepresentation getAppDefinitionHistory(@PathVariable Long modelId, @PathVariable Long modelHistoryId) {
        ModelHistory model = getModelHistory(modelId, modelHistoryId, true, false);
        return createAppDefinitionRepresentation(model);
    }
    
    @RequestMapping(value = "/rest/app-definitions/{modelId}", method = RequestMethod.PUT, produces = "application/json")
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
    public AppDefinitionUpdateResultRepresentation publishAppDefinition(@PathVariable("modelId") Long modelId, 
            @RequestBody AppDefinitionPublishRepresentation publishModel) {
        
    	return super.publishAppDefinition(modelId, publishModel);	
    	
    }
    
    @RequestMapping(value="/rest/app-definitions/{modelId}/export", method = RequestMethod.GET)
    public void exportAppDefinition(HttpServletResponse response, @PathVariable Long modelId) throws IOException {  
        super.exportAppDefinition(response, modelId);
    }
    
    @Transactional
    @RequestMapping(value="/rest/app-definitions/{modelId}/import", method = RequestMethod.POST, produces = "application/json")
    public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @PathVariable Long modelId, @RequestParam("file") MultipartFile file) {  
        return super.importAppDefinitionNewVersion(request, file, modelId);
    }
    
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
    
    @Transactional
    @RequestMapping(value="/rest/app-definitions/import", method = RequestMethod.POST, produces = "application/json")
    public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, @RequestParam("file") MultipartFile file) {  
    	return super.importAppDefinition(request, file);
    }
    
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
