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
import com.activiti.service.activiti.DecisionTableService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yvo Swillens
 */
@RestController
public class DecisionTableClientResource extends AbstractClientResource {

    private final Logger log = LoggerFactory.getLogger(DecisionTableClientResource.class);

    @Autowired
    protected DecisionTableService clientService;

    @RequestMapping(value = "/rest/activiti/decision-tables/{decisionTableId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getDecisionTable(@PathVariable String decisionTableId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getDecisionTable(serverConfig, decisionTableId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/decision-tables/{decisionTableId}/editorJson", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getEditorJsonForDecisionTable(@PathVariable String decisionTableId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getEditorJsonForDecisionTable(serverConfig, decisionTableId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}