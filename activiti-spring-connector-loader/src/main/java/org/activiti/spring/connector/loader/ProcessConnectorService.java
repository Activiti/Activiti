/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package org.activiti.spring.connector.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.activiti.core.common.model.connector.ConnectorDefinition;
import org.activiti.core.common.spring.connector.ConnectorDefinitionReader;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.resources.DeploymentResourceLoader;

public class ProcessConnectorService {

    private final DeploymentResourceLoader<ConnectorDefinition> connectorResourceLoader;
    private final ConnectorDefinitionReader connectorReader;
    private final RepositoryService repositoryService;

    //deploymentId => List <ConnectorDefinition>
    private Map<String, List<ConnectorDefinition>> processConnectorDefinitionsMap = new HashMap<>();

    public ProcessConnectorService(DeploymentResourceLoader<ConnectorDefinition> connectorResourceLoader,
                                   ConnectorDefinitionReader connectorReader,
                                   RepositoryService repositoryService) {
        this.connectorResourceLoader = connectorResourceLoader;
        this.connectorReader = connectorReader;
        this.repositoryService = repositoryService;
    }

    private List<ConnectorDefinition> getConnectorDefinitionsForDeploymentId(String deploymentId) {
        List<ConnectorDefinition> connectorDefinitions = processConnectorDefinitionsMap.get(deploymentId);
        if (connectorDefinitions != null) {
            return connectorDefinitions;
        }
        connectorDefinitions = connectorResourceLoader.loadResourcesForDeployment(deploymentId,
                                                                                  connectorReader);
        processConnectorDefinitionsMap.put(deploymentId,
                                           connectorDefinitions);
        return connectorDefinitions;
    }

    public ConnectorDefinition findConnector(String processDefinitionId, String connectorName) {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        List<ConnectorDefinition> resultingConnectors = getConnectorDefinitionsForDeploymentId(processDefinition.getDeploymentId()).stream()
                .filter(c -> connectorName.equals(c.getName()))
                .collect(Collectors.toList());

        ConnectorDefinition connectorDefinition = null;
        if (resultingConnectors != null && !resultingConnectors.isEmpty()) {
            if (resultingConnectors.size() != 1) {
                throw new RuntimeException("Expecting exactly 1 connector definition with name mapping `" + connectorName +
                                                   "`, but were found " + resultingConnectors.size());
            }

            connectorDefinition = resultingConnectors.get(0);
        }
        return connectorDefinition;
    }

}
