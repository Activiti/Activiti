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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.FormService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
public class FormClientResource extends AbstractClientResource {

    @Autowired
    protected FormService clientService;

    @RequestMapping(value = "/rest/activiti/forms/{formId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getForm(@PathVariable String formId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getForm(serverConfig, formId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @RequestMapping(value = "/rest/activiti/forms/{formId}/editorJson", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getEditorJsonForForm(@PathVariable String formId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getEditorJsonForForm(serverConfig, formId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/process-definition-start-form/{processDefinitionId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getProcessDefinitionStartForm(@PathVariable String processDefinitionId) throws BadRequestException {

        ServerConfig serverConfig = retrieveServerConfig();
        try {
            return clientService.getProcessDefinitionStartForm(serverConfig, processDefinitionId);
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

}