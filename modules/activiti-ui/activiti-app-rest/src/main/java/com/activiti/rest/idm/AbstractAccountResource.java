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
