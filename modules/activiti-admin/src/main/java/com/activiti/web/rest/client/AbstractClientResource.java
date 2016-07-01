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
package com.activiti.web.rest.client;

import com.activiti.domain.ServerConfig;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.service.activiti.AppVersionClientService;
import com.activiti.service.activiti.EndpointUserProfileService;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractClientResource {

	private static final String SERVER_ID = "serverId";

	@Autowired
	protected ServerConfigRepository configRepository;

    @Autowired
    protected AppVersionClientService appVersionClientService;

    @Autowired
    protected EndpointUserProfileService endpointUserProfileService;
    
    @Autowired
    protected Environment env;

	protected ServerConfig retrieveServerConfig() {
		List<ServerConfig> serverConfigs = configRepository.findAll();

        if (serverConfigs == null) {
            throw new BadRequestException("No server config found");
        }

		if (serverConfigs.size() > 1) {
		    throw new BadRequestException("Only one server config allowed");
		}


        return serverConfigs.get(0);
	}

	protected Map<String, String[]> getRequestParametersWithoutServerId(HttpServletRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		Map<String, String[]> resultMap = new HashMap<String, String[]>();
		resultMap.putAll(parameterMap);
		resultMap.remove(SERVER_ID);
		return resultMap;
	}

 }
