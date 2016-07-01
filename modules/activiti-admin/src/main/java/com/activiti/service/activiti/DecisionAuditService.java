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

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for invoking Activiti REST services.
 */
@Service
public class DecisionAuditService {

	private final Logger log = LoggerFactory.getLogger(DecisionAuditService.class);

	@Autowired
	protected ActivitiClientService clientUtil;

	public JsonNode listDecisionAudits(ServerConfig serverConfig, Map<String, String[]> parameterMap) {
	    URIBuilder builder = clientUtil.createUriBuilder("/enterprise/decisions/audits");

		for (String name : parameterMap.keySet()) {
			builder.addParameter(name, parameterMap.get(name)[0]);
		}
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, builder.toString()));
		return clientUtil.executeRequest(get, serverConfig);
	}

	public JsonNode getDecisionAudit(ServerConfig serverConfig, String decisionAuditId) {
		HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "/enterprise/decisions/audits/" + decisionAuditId));
		return clientUtil.executeRequest(get, serverConfig);
	}
	
	public JsonNode getProcessInstanceDecisionAudit(ServerConfig serverConfig, String processInstanceId) {
        HttpGet get = new HttpGet(clientUtil.getServerUrl(serverConfig, "/enterprise/process-instances/" + processInstanceId + "/decision-tasks"));
        return clientUtil.executeRequest(get, serverConfig);
    }
}
