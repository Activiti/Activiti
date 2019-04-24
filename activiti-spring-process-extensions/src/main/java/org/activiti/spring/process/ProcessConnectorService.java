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

package org.activiti.spring.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.types.VariableType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ProcessConnectorService {

    private final ObjectMapper objectMapper;
    private final RepositoryService repositoryService;
    
    //deploymentId => List <ConnectorDefinition>
     private Map<String, List<ConnectorDefinition>> processConnectorDefinitionsMap = new HashMap<>();
    
    
    public ProcessConnectorService(ObjectMapper objectMapper, 
                                   RepositoryService repositoryService) {

        this.objectMapper = objectMapper;
        this.repositoryService = repositoryService;
    }
    
    private List<ConnectorDefinition> getConnectorDefinitionsForDeploymentId(String deploymentId) {
        if (deploymentId == null) return null;
            
        List<ConnectorDefinition> connectorDefinitions = processConnectorDefinitionsMap.get(deploymentId);
        
        if (connectorDefinitions != null) {
            return connectorDefinitions;
        }
           
        connectorDefinitions = new ArrayList<>();
        List <String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
        
        if (resourceNames != null && !resourceNames.isEmpty()) {
            
            List <String> connectorNames = resourceNames.stream()
                                                    .filter(s -> s.contains("\\connectors\\"))
                                                    .collect(Collectors.toList());
            
            if (connectorNames != null && !connectorNames.isEmpty()) {
                for (String name:connectorNames) {
                    try {
                        ConnectorDefinition connectorDefinition = read(repositoryService.getResourceAsStream(deploymentId, name));
                        if (connectorDefinition != null) {
                            connectorDefinitions.add(connectorDefinition); 
                        }
                    } catch (Exception e)  {
                        
                    }
                }
            }
            
        }
        processConnectorDefinitionsMap.put(deploymentId, connectorDefinitions);
        return connectorDefinitions;
    }
    
    private ConnectorDefinition read(InputStream inputStream) throws IOException {
        return objectMapper.readValue(inputStream,
                ConnectorDefinition.class);
    }
    
    
    public Optional<ActionDefinition> find(String deploymentId,String implementation) {

        String connectorName = StringUtils.substringBefore(implementation,
                                                           ".");
        String actionName = StringUtils.substringAfter(implementation,
                                                       ".");

        ConnectorDefinition connectorDefinition = findConnector(deploymentId, connectorName);

        ActionDefinition actionDefinition = findActionDefinition(actionName,
                                                                 connectorDefinition);

        return Optional.ofNullable(actionDefinition);
    }
    
    
    private ConnectorDefinition findConnector(String deploymentId, String connectorName) {
        List<ConnectorDefinition> connectorDefinitions = getConnectorDefinitionsForDeploymentId(deploymentId);
        ConnectorDefinition connectorDefinition = null;
        
        if (connectorDefinitions != null && !connectorDefinitions.isEmpty()) {
        
            List<ConnectorDefinition> resultingConnectors = connectorDefinitions.stream()
                .filter(c -> connectorName.equals(c.getName()))
                .collect(Collectors.toList());

        
            if (resultingConnectors != null && !resultingConnectors.isEmpty()) {
                if (resultingConnectors.size() != 1) {
                    throw new RuntimeException("Expecting exactly 1 connector definition with name mapping `" + connectorName +
                                                   "`, but were found " + resultingConnectors.size());
                }

                connectorDefinition = resultingConnectors.get(0);
            }
        }
        
        return connectorDefinition;
    }
    
    private ActionDefinition findActionDefinition(String actionName,
                                                  ConnectorDefinition connectorDefinition) {
        ActionDefinition actionDefinition = null;
        if (connectorDefinition != null) {
            List<ActionDefinition> actionDefinitions = filterByName(connectorDefinition.getActions(),
                                                                    actionName);
            if (actionDefinitions != null) {
                if (actionDefinitions.size() != 1) {
                    throw new RuntimeException("Expecting exactly 1 action definition with name mapping `" + actionName +
                                                       "`, but were found " + actionDefinitions.size());
                }
                actionDefinition = actionDefinitions.get(0);
            }

            if (actionDefinition == null) {
                throw new RuntimeException("No action with name mapping `" + actionName + "` was found in connector `" +
                                                   connectorDefinition.getName() + "`");
            }
        }
        return actionDefinition;
    }

    private List<ActionDefinition> filterByName(Map<String, ActionDefinition> actionDefinitions,
                                                String actionName) {
        if (actionDefinitions == null || actionName == null) {
            return null;
        }
        return actionDefinitions.entrySet()
                .stream()
                .filter(entry -> actionName.equals(entry.getValue().getName()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    

}
