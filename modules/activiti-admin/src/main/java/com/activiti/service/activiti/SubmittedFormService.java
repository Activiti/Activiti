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
public class SubmittedFormService {


	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listSubmittedForms(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/submitted-forms");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode listFormSubmittedForms(ServerConfig serverConfig, String formId, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/form-submitted-forms/" + formId);
        
	    for (String name : parameterMap.keySet()) {
            builder.addParameter(name, parameterMap.get(name)[0]);
        }
	    
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
        return clientUtil.executeRequest(get, serverConfig);
    }
	
	public JsonNode getSubmittedForm(ServerConfig serverConfig, String submittedFormId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "/enterprise/submitted-forms/" + submittedFormId));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getTaskSubmittedForm(ServerConfig serverConfig, String taskId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "/enterprise/task-submitted-form/" + taskId));
        return clientUtil.executeRequest(get, serverConfig);
    }
	
	public JsonNode getProcessSubmittedForms(ServerConfig serverConfig, String processId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "/enterprise/process-submitted-forms/" + processId));
        return clientUtil.executeRequest(get, serverConfig);
    }

}
