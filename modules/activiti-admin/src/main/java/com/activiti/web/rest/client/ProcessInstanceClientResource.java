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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ProcessInstanceService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class ProcessInstanceClientResource extends AbstractClientResource {

	@Autowired
	protected ProcessInstanceService clientService;

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getTask(@PathVariable String processInstanceId, @RequestParam(required=false, defaultValue="false") boolean runtime) throws BadRequestException {

		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getProcessInstance(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/tasks", method = RequestMethod.GET)
	public JsonNode getSubtasks(@PathVariable String processInstanceId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getTasks(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables", method = RequestMethod.GET)
	public JsonNode getVariables(@PathVariable String processInstanceId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getVariables(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    public void updateVariable(@PathVariable String processInstanceId, @PathVariable String variableName, @RequestBody ObjectNode body) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig();
        try {
            clientService.updateVariable(serverConfig, processInstanceId, variableName, body);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void createVariable(@PathVariable String processInstanceId, @RequestBody ObjectNode body) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig();
        try {
            clientService.createVariable(serverConfig, processInstanceId, body);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }


    @RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/variables/{variableName}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteVariable(@PathVariable String processInstanceId, @PathVariable String variableName) throws BadRequestException {
        ServerConfig serverConfig = retrieveServerConfig();
        try {
            clientService.deleteVariable(serverConfig, processInstanceId, variableName);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/subprocesses", method = RequestMethod.GET)
	public JsonNode getSubProcesses(@PathVariable String processInstanceId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getSubProcesses(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/jobs", method = RequestMethod.GET)
	public JsonNode getJobs(@PathVariable String processInstanceId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getJobs(serverConfig, processInstanceId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void executeAction(@PathVariable String processInstanceId, @RequestBody JsonNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			clientService.executeAction(serverConfig, processInstanceId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
}
