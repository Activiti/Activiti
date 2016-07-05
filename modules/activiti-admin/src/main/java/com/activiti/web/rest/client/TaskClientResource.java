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
import com.activiti.service.activiti.TaskService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TaskClientResource extends AbstractClientResource {
	
	@Autowired
	protected TaskService clientService;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return
	 * its login.
	 */
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getTask(@PathVariable String taskId, @RequestParam(required=false, defaultValue="false") boolean runtime) throws BadRequestException {
		
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getTask(serverConfig, taskId, runtime);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.DELETE)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable String taskId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			clientService.deleteTask(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void executeTaskAction(@PathVariable String taskId, @RequestBody ObjectNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			clientService.executeTaskAction(serverConfig, taskId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void updateTask(@PathVariable String taskId, @RequestBody ObjectNode actionBody) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			clientService.updateTask(serverConfig, taskId, actionBody);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/subtasks", method = RequestMethod.GET)
	public JsonNode getSubtasks(@PathVariable String taskId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getSubTasks(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/variables", method = RequestMethod.GET)
	public JsonNode getVariables(@PathVariable String taskId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getVariables(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/activiti/tasks/{taskId}/identitylinks", method = RequestMethod.GET)
	public JsonNode getIdentityLinks(@PathVariable String taskId) throws BadRequestException {
		ServerConfig serverConfig = retrieveServerConfig();
		try {
			return clientService.getIdentityLinks(serverConfig, taskId);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
	
}
