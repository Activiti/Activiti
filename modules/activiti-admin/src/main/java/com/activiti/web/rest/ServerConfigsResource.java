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
package com.activiti.web.rest;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.AppVersionClientService;
import com.activiti.service.activiti.EndpointUserProfileService;
import com.activiti.service.activiti.ServerConfigService;
import com.activiti.web.rest.dto.ServerConfigRepresentation;
import com.activiti.web.rest.exception.BadRequestException;

/**
 * REST controller for managing the server configs.
 *
 * @author trademak
 * @author jbarrez
 * @author yvoswillens
 */
@RestController
public class ServerConfigsResource {

	@Autowired
	private ServerConfigService serverConfigService;

    @Autowired
    private AppVersionClientService appVersionClientService;

    @Autowired
    private EndpointUserProfileService endpointUserProfileService;
    
    @Autowired
    protected Environment env;

	@RequestMapping(value = "/rest/server-configs", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<ServerConfigRepresentation> getServers() {
        return serverConfigService.findAll();
	}

    @RequestMapping(value = "/rest/server-configs/default", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public ServerConfigRepresentation getDefaultServerConfig() {
	    return new ServerConfigRepresentation(serverConfigService.getDefaultServerConfig());
	}

	@RequestMapping(value = "/rest/server-configs/{serverId}", method = RequestMethod.PUT, produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	public void updateServer(@PathVariable Long serverId, @RequestBody ServerConfigRepresentation configRepresentation) {

	    ServerConfig config = serverConfigService.findOne(serverId);
	    
	    if (config == null) {
	        throw new BadRequestException("Server with id '" + serverId
                    + "' does not exist");
	    }
	    
	    boolean updatePassword = false;
	    if (StringUtils.isNotEmpty(configRepresentation.getPassword())) {
	        config.setPassword(configRepresentation.getPassword());
	        updatePassword = true;
	    } else {
	        configRepresentation.setPassword(serverConfigService.getServerConfigDecryptedPassword(config));
	    }
		
        config.setContextRoot(configRepresentation.getContextRoot());
        config.setDescription(configRepresentation.getDescription());
        config.setName(configRepresentation.getName());
        config.setPort(configRepresentation.getServerPort());
        config.setRestRoot(configRepresentation.getRestRoot());
        config.setServerAddress(configRepresentation.getServerAddress());
        config.setUserName(configRepresentation.getUserName());

        serverConfigService.save(config, updatePassword);
	}
	
	protected ServerConfigRepresentation createServerConfigRepresentation(ServerConfig serverConfig) {
        ServerConfigRepresentation serverRepresentation = new ServerConfigRepresentation();
        serverRepresentation.setId(serverConfig.getId());
        serverRepresentation.setName(serverConfig.getName());
        serverRepresentation.setDescription(serverConfig.getDescription());
        serverRepresentation.setServerAddress(serverConfig.getServerAddress());
        serverRepresentation.setServerPort(serverConfig.getPort());
        serverRepresentation.setContextRoot(serverConfig.getContextRoot());
        serverRepresentation.setRestRoot(serverConfig.getRestRoot());
        serverRepresentation.setUserName(serverConfig.getUserName());
        return serverRepresentation;
    }
}
