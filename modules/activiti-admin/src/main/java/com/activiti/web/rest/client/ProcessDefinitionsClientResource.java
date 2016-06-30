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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ProcessDefinitionService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class ProcessDefinitionsClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(ProcessDefinitionsClientResource.class);

    @Autowired
    protected ProcessDefinitionService clientService;

    /**
     * GET  /rest/authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/rest/activiti/process-definitions",
            method = RequestMethod.GET,
            produces = "application/json")
    public JsonNode listProcessDefinitions(HttpServletRequest request) {
        log.debug("REST request to get a list of process definitions");
        
        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig();
    	Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
    	
    	try {
    		resultNode = clientService.listProcesDefinitions(serverConfig, parameterMap, true);
	        
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    	
        return resultNode;
    }
}
