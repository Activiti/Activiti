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

package org.activiti.runtime.api.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;

public class OutboundVariablesProvider {

    private ProcessExtensionService processExtensionService;
    private ConnectorActionDefinitionFinder connectorActionDefinitionFinder;

    public OutboundVariablesProvider(ProcessExtensionService processExtensionService,
                                     ConnectorActionDefinitionFinder connectorActionDefinitionFinder) {
        this.processExtensionService = processExtensionService;
        this.connectorActionDefinitionFinder = connectorActionDefinitionFinder;
    }

    public Map<String, Object> calculateVariables(IntegrationContext integrationContext,
                                                  ActionDefinition actionDefinition) {
        Map<String, Object> outboundVariables = integrationContext.getOutBoundVariables();
        if (actionDefinition == null || !processExtensionService.hasExtensionsFor(integrationContext.getProcessDefinitionId())) {
            return outboundVariables;
        }
        Map<String, Object> mappedOutboundVariables = new HashMap<>();
        ProcessExtensionModel extensionsModel = processExtensionService.getExtensionsForId(integrationContext.getProcessDefinitionId());

        Extension extensions = extensionsModel.getExtensions();
        ProcessVariablesMapping processVariablesMapping = extensions.getMappingForFlowElement(integrationContext.getClientId());
        processVariablesMapping.getOutputs().forEach(
                (varUUID, mapping) -> {
                    VariableDefinition outBoundVariableDefinition = actionDefinition.getOutputs()
                            .stream()
                            .filter(variableDefinition ->
                                            variableDefinition.getId().equals(mapping.getValue()))
                            .findFirst()
                            .orElse(null);
                    //fixme have single VariableDefinition
                    org.activiti.spring.process.model.VariableDefinition processVariableDefinition = extensions.getProperty(varUUID);
                    if (outBoundVariableDefinition != null && processVariableDefinition != null) {
                        mappedOutboundVariables.put(processVariableDefinition.getName(),
                                                    //use remove instead of get to keep only outbound variables without mapping
                                                    outboundVariables.remove(outBoundVariableDefinition.getName()));
                    }
                });
        outboundVariables.forEach(
                (key, value) ->
                        extensions.getProperties().values()
                                .stream()
                                .filter(matchingDefinition -> matchingDefinition.getName().equals(key))
                                .findFirst()
                                .map(variableDefinition -> mappedOutboundVariables.put(key,
                                                                                       value)));
        return mappedOutboundVariables;
    }
    
    public Map<String, Object> calculateVariables(IntegrationContext integrationContext) {
        Optional<ActionDefinition> actionDefinitionOptional = connectorActionDefinitionFinder.find(integrationContext.getProcessDefinitionId(), integrationContext.getConnectorType());
        return calculateVariables(integrationContext, actionDefinitionOptional.orElse(null));
    }
}
