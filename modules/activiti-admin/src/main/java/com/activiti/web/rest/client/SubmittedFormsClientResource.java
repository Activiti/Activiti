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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.SubmittedFormService;
import com.activiti.service.activiti.exception.ActivitiServiceException;
import com.activiti.web.rest.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Bassam Al-Sarori
 */
@RestController
public class SubmittedFormsClientResource extends AbstractClientResource {

    @Autowired
    protected SubmittedFormService clientService;

    @RequestMapping(value = "/rest/activiti/submitted-forms", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listSubmittedForms(HttpServletRequest request) {
        JsonNode resultNode = null;
        ServerConfig serverConfig = retrieveServerConfig();
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

        try {
            resultNode = clientService.listSubmittedForms(serverConfig, parameterMap);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }

        return resultNode;
    }
    
    @RequestMapping(value = "/rest/activiti/form-submitted-forms/{formId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listFomrSubmittedForms(HttpServletRequest request, @PathVariable String formId) {
        ServerConfig serverConfig = retrieveServerConfig();

        try {
            return clientService.listFormSubmittedForms(serverConfig, formId, getRequestParametersWithoutServerId(request));
        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/task-submitted-form/{taskId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getTaskSubmittedForm(@PathVariable String taskId) {
        ServerConfig serverConfig = retrieveServerConfig();

        try {
            return clientService.getTaskSubmittedForm(serverConfig, taskId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }

    }
    
    @RequestMapping(value = "/rest/activiti/process-submitted-forms/{processId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getProcessSubmittedForms(@PathVariable String processId) {
        ServerConfig serverConfig = retrieveServerConfig();

        try {
            return clientService.getProcessSubmittedForms(serverConfig, processId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    @RequestMapping(value = "/rest/activiti/submitted-forms/{submittedFormId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getSubmittedForm(@PathVariable String submittedFormId) {
        ServerConfig serverConfig = retrieveServerConfig();

        try {
            return clientService.getSubmittedForm(serverConfig, submittedFormId);

        } catch (ActivitiServiceException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}