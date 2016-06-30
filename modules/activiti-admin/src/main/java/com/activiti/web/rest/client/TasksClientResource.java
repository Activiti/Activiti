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
import com.activiti.service.activiti.TaskService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing the current user's account.
 */
@RestController
public class TasksClientResource extends AbstractClientResource {

	@Autowired
	protected TaskService clientService;

	/**
	 * GET /rest/authenticate -> check if the user is authenticated, and return
	 * its login.
	 */
	@RequestMapping(value = "/rest/activiti/tasks", method = RequestMethod.POST, produces = "application/json")
	public JsonNode listTasks(@RequestBody ObjectNode requestNode) {
		ServerConfig serverConfig = retrieveServerConfig();
		JsonNode resultNode;
		try {
			resultNode = clientService.listTasks(serverConfig, requestNode);
		} catch (ActivitiServiceException e) {
			throw new BadRequestException(e.getMessage());
		}

		if(resultNode == null) {
			throw new BadRequestException("Empty result returned from activiti");
		}
		return resultNode;
	}
}
