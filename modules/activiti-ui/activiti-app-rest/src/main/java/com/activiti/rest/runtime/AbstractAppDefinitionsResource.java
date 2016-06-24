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
package com.activiti.rest.runtime;

import java.util.ArrayList;
import java.util.List;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.runtime.AppDefinitionRepresentation;
import com.activiti.model.runtime.RuntimeAppDefinitionSaveRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.activiti.service.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAppDefinitionsResource {
    
    private final Logger logger = LoggerFactory.getLogger(AbstractAppDefinitionsResource.class);

    @Autowired
    protected RuntimeAppDefinitionService runtimeAppDefinitionService;
    
    @Autowired
    protected DeploymentService deploymentService;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    protected static final AppDefinitionRepresentation kickstartAppDefinitionRepresentation 
    	= AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("kickstart");
    
    protected static final AppDefinitionRepresentation taskAppDefinitionRepresentation 
    	= AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("tasks");
    
    protected static final AppDefinitionRepresentation idmAppDefinitionRepresentation 
    	= AppDefinitionRepresentation.createDefaultAppDefinitionRepresentation("identity");
    
	protected ResultListDataRepresentation getAppDefinitions() {
		List<AppDefinitionRepresentation> resultList = new ArrayList<AppDefinitionRepresentation>();
		
		// Default app: kickstart
		resultList.add(kickstartAppDefinitionRepresentation);

		// Default app: tasks and IDM (available for all)
		resultList.add(taskAppDefinitionRepresentation);
		resultList.add(idmAppDefinitionRepresentation);

		// Custom apps
		List<RuntimeAppDefinition> appDefinitions = runtimeAppDefinitionService.getDefinitionsForUser(SecurityUtils.getCurrentUserObject());
        for (RuntimeAppDefinition runtimeAppDefinition : appDefinitions) {
            resultList.add(createRepresentation(runtimeAppDefinition));
        }
        
		ResultListDataRepresentation result = new ResultListDataRepresentation(resultList);
		return result;
	}
	
	protected AppDefinitionRepresentation createDefaultAppDefinition(String id) {
		AppDefinitionRepresentation app = new AppDefinitionRepresentation();
		
		return app;
	}
	
	protected AppDefinitionRepresentation createRepresentation(RuntimeAppDefinition runtimeAppDefinition) {
	    AppDefinitionRepresentation resultAppDef = new AppDefinitionRepresentation(runtimeAppDefinition);
        try {
            JsonNode appDefNode = objectMapper.readTree(runtimeAppDefinition.getDefinition());
            if (appDefNode != null) {
                if (appDefNode.get("theme") != null) {
                    resultAppDef.setTheme(appDefNode.get("theme").asText());
                }
                if (appDefNode.get("icon") != null) {
                    resultAppDef.setIcon(appDefNode.get("icon").asText());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error reading app definition " + runtimeAppDefinition.getId(), e);
        }
        return resultAppDef;
	}
	
    protected void deployAppDefinitions(RuntimeAppDefinitionSaveRepresentation saveObject) {
	    if (saveObject != null && CollectionUtils.isNotEmpty(saveObject.getAppDefinitions())) {
	        List<Long> appDefinitionIds = new ArrayList<Long>();
	        for (AppDefinitionRepresentation definition : saveObject.getAppDefinitions()) {
	            appDefinitionIds.add(definition.getId());
            }
	        try {
	            deploymentService.deployAppDefinitions(appDefinitionIds, SecurityUtils.getCurrentUserObject());
	        } catch (Exception e) {
	            logger.error("Error deploying app definitions", e);
                throw new BadRequestException("Error deploying app definitions");
	        }
	    }
	}
	
}
