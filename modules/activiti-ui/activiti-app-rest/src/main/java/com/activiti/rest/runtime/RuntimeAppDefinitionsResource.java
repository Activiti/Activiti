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

@RestController
public class RuntimeAppDefinitionsResource {
	
	@Inject
	protected AppDefinitionService appDefinitionService;
	
	@Inject
	protected RuntimeAppDefinitionService runtimeAppDefinitionService;

	
	@RequestMapping(value = "/rest/editor/app-definitions", method = RequestMethod.GET, produces = "application/json")
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
