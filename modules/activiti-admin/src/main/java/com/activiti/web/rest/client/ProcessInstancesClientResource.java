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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ProcessInstanceService;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ProcessInstancesClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(ProcessInstancesClientResource.class);

    @Autowired
    protected ProcessInstanceService clientService;
    
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET  /rest/authenticate -> check if the user is authenticated, and return its login.
     */
    @RequestMapping(value = "/rest/activiti/process-instances",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json")
    public JsonNode listProcessInstances(@RequestBody ObjectNode bodyNode) {
        log.debug("REST request to get a list of process instances");
        
        JsonNode resultNode = null;
        try {
            ServerConfig serverConfig = retrieveServerConfig();
	        resultNode = clientService.listProcesInstances(bodyNode, serverConfig);
			
		} catch (Exception e) {
			log.error("Error processing process instance list request", e);
			throw new BadRequestException(e.getMessage());
		}
        
        return resultNode;
    }
}
