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
import com.activiti.service.activiti.DecisionTableService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 * @author Bassam Al-Sarori
 */
@RestController
public class DecisionTablesClientResource extends AbstractClientResource {

    @Autowired
    protected DecisionTableService clientService;

    /**
     * GET list of deployed decision tables.
     */
    @RequestMapping(value="/rest/activiti/decision-tables", method = RequestMethod.GET, produces = "application/json")
    public JsonNode listDecisionTables(HttpServletRequest request) {
        ServerConfig serverConfig = retrieveServerConfig();
        Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);
        return clientService.listDecisionTables(serverConfig, parameterMap);
    }
    
    /**
     * GET process definition's list of deployed decision tables.
     */
    @RequestMapping(value = "/rest/activiti/process-definition-decision-tables/{processDefinitionId}", method = RequestMethod.GET, produces = "application/json")
    public JsonNode getProcessDefinitionDecisionTables(@PathVariable String processDefinitionId, HttpServletRequest request) {
        return clientService.getProcessDefinitionDecisionTables(retrieveServerConfig(), processDefinitionId);
    }
}