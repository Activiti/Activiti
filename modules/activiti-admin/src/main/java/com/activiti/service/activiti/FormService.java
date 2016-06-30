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
package com.activiti.service.activiti;

import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.ServerConfig;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class FormService {

	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listForms(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("enterprise/forms");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getForm(ServerConfig serverConfig, String formId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "enterprise/forms/" + formId));
		return clientUtil.executeRequest(get, serverConfig);
	}

    public JsonNode getEditorJsonForForm(ServerConfig serverConfig, String formId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "enterprise/forms/" + formId + "/editorJson"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public JsonNode getProcessDefinitionStartForm(ServerConfig serverConfig, String processDefinitionId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "enterprise/process-definitions/" + processDefinitionId + "/start-form"));
        return clientUtil.executeRequest(get, serverConfig);
    }
    
    public JsonNode getProcessDefinitionForms(ServerConfig serverConfig, String processDefinitionId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "enterprise/process-definitions/" + processDefinitionId + "/forms"));
        return clientUtil.executeRequest(get, serverConfig);
    }
}
