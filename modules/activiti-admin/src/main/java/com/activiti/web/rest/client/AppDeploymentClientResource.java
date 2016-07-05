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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.AppService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.activiti.web.rest.exception.InternalServerErrorException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST controller for getting APP details
 */
@RestController
public class AppDeploymentClientResource extends AbstractClientResource {

	@Autowired
	protected AppService clientService;

	@RequestMapping(value = "/rest/activiti/apps/{appId}", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getAppDefinition(@PathVariable String appId) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getAppDefinition(serverConfig, appId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/apps/{appId}", method = RequestMethod.DELETE)
    public void deleteAppDeployment(HttpServletResponse response, @PathVariable String appId) {
        clientService.deleteAppDeployment(retrieveServerConfig(), response, appId);
    }
	
	@RequestMapping(value = "/rest/activiti/app", method = RequestMethod.GET)
    public void getAppDefinitionByDeployment(HttpServletRequest request, HttpServletResponse respone) {
        clientService.getAppDefinitionByDeployment(retrieveServerConfig(), respone, getRequestParametersWithoutServerId(request));
    }

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/process-definitions/{deploymentId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getProcessDefinitionsForDeploymentId(@PathVariable String deploymentId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getProcessDefinitionsForDeploymentId(serverConfig, deploymentId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/decision-tables/{dmnDeploymentId}", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getDecisionTablesForDeploymentId(@PathVariable String dmnDeploymentId) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getDecisionDefinitionsForDeploymentId(serverConfig, dmnDeploymentId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

    //todo: should not be path variable but request
    @RequestMapping(value = "/rest/activiti/apps/forms/{appDeploymentId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getFormsForAppDeploymentId(@PathVariable String appDeploymentId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getFormsForAppDeploymentId(serverConfig, appDeploymentId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value="/rest/activiti/apps/export/{deploymentId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode exportApp(HttpServletRequest request,@PathVariable String deploymentId, HttpServletResponse httpResponse){
        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.exportApp(serverConfig, deploymentId, httpResponse);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not download app: " + e.getMessage());
        }
    }
    
    @RequestMapping(value="/rest/activiti/apps/redeploy/{deploymentId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode redeployApp(HttpServletRequest request, HttpServletResponse httpResponse, @PathVariable String deploymentId){
        ServerConfig serverConfig = retrieveServerConfig();
        ServerConfig targetServerConfig = retrieveServerConfig();
        try {
            return clientService.redeployApp(httpResponse, serverConfig, targetServerConfig, deploymentId);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not redeploy app: " + e.getMessage());
        }
    }
    
    @RequestMapping(value="/rest/activiti/apps/redeploy/{deploymentId}/{replaceAppId}",method=RequestMethod.GET, produces = "application/json")
    public JsonNode redeployApp(HttpServletRequest request, HttpServletResponse httpResponse, @PathVariable String deploymentId, @PathVariable String replaceAppId){
        ServerConfig serverConfig = retrieveServerConfig();
        ServerConfig targetServerConfig = retrieveServerConfig();
        try {
            return clientService.redeployReplaceApp(httpResponse, serverConfig, targetServerConfig, deploymentId, replaceAppId);
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not redeploy app: " + e.getMessage());
        }
    }
}
