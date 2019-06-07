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

import java.util.Map;
import java.util.stream.Collectors;

import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;

public class InboundVariableValueProvider {

    private ProcessExtensionService processExtensionService;

    public InboundVariableValueProvider(ProcessExtensionService processExtensionService) {
        this.processExtensionService = processExtensionService;
    }

    public Object calculateMappedValue(VariableDefinition variableDefinition,
                                       DelegateExecution execution) {
        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());
        ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getCurrentActivityId());
        Mapping inputMapping = processVariablesMapping.getInputMapping(variableDefinition.getName());
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return inputMapping.getValue();
            }
            if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                String name = inputMapping.getValue().toString();
                
                //This is extra check, we may search variable simply by variableName
                org.activiti.spring.process.model.VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);
                if (processVariableDefinition != null) {
                    return execution.getVariable(processVariableDefinition.getName());
                }
            }
        }
        return execution.getVariable(variableDefinition.getName());
    }

    public Map<String, Object> calculateStaticValues(DelegateExecution execution) {
        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());
        ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getCurrentActivityId());
        Map<String, Mapping> inputs = processVariablesMapping.getInputs();
        return inputs.entrySet().stream()
                .filter(input -> Mapping.SourceMappingType.STATIC_VALUE.equals(input.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));
    }
}
