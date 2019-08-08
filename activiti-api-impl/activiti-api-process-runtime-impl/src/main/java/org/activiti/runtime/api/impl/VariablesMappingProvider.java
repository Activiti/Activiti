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

package org.activiti.runtime.api.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ConstantDefinition;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessConstantsMapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;

public class VariablesMappingProvider {

    private ProcessExtensionService processExtensionService;

    public VariablesMappingProvider(ProcessExtensionService processExtensionService) {
        this.processExtensionService = processExtensionService;
    }

    protected Optional<Object> calculateMappedValue(Mapping inputMapping,
                                                    DelegateExecution execution,
                                                    ProcessExtensionModel extensions) {
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return Optional.of(inputMapping.getValue());
            }

            if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                String name = inputMapping.getValue().toString();

                VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);
                if (processVariableDefinition != null) {
                    return Optional.ofNullable(execution.getVariable(processVariableDefinition.getName()));
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> calculateInputVariables(DelegateExecution execution) {

        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());

        Map<String, Object> constants = calculateConstants(execution, extensions);

        if (extensions.getExtensions().hasEmptyInputsMapping(execution.getCurrentActivityId())) {
            return constants;
        }

        Map<String, Object> inboudVariables;

        if (!extensions.getExtensions().hasMapping(execution.getCurrentActivityId())) {
            inboudVariables = execution.getVariables();
        } else {
            inboudVariables = calculateInputVariables(execution, extensions);
        }

        inboudVariables.putAll(constants);
        return inboudVariables;
    }

    private Map<String, Object> calculateConstants(DelegateExecution execution,
                                                   ProcessExtensionModel extensions) {
        Map<String, Object> constants = new HashMap<>();

        ProcessConstantsMapping processConstantsMapping = extensions.getExtensions().getConstantForFlowElement(execution.getCurrentActivityId());
        for (Map.Entry<String, ConstantDefinition> mapping : processConstantsMapping.entrySet()) {
            constants.put(mapping.getKey(), mapping.getValue().getValue());
        }
        return constants;
    }

    private Map<String, Object> calculateInputVariables(DelegateExecution execution,
                                                        ProcessExtensionModel extensions) {
        Map<String, Object> inboundVariables = new HashMap<>();

        ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getCurrentActivityId());

        Map<String, Mapping> inputMappings = processVariablesMapping.getInputs();
        for (Map.Entry<String, Mapping> mapping : inputMappings.entrySet()) {
            Optional<Object> mappedValue = calculateMappedValue(mapping.getValue(),
                    execution,
                    extensions);
            mappedValue.ifPresent(value -> inboundVariables.put(mapping.getKey(),
                    value));
        }
        return inboundVariables;
    }

    private Optional<Object> calculateOutPutMappedValue(Mapping mapping,
                                                        Map<String, Object> currentContextVariables) {
        if (mapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(mapping.getType())) {
                return Optional.of(mapping.getValue());
            } else {
                if (Mapping.SourceMappingType.VARIABLE.equals(mapping.getType())) {
                    String name = mapping.getValue().toString();

                    return currentContextVariables != null ?
                            Optional.ofNullable(currentContextVariables.get(name)) :
                            Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> calculateOutPutVariables(MappingExecutionContext execution,
                                                        Map<String, Object> availableVariables) {

        ProcessExtensionModel extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());

        if (extensions.getExtensions().hasEmptyOutputsMapping(execution.getActivityId())) {
            return Collections.emptyMap();
        }

        if (!extensions.getExtensions().hasMapping(execution.getActivityId())) {
            return new HashMap<>(availableVariables);
        }

        if (!availableVariables.isEmpty()) {
            return calculateOutPutVariables(execution, extensions, availableVariables);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> calculateOutPutVariables(MappingExecutionContext execution,
                                                         ProcessExtensionModel extensions,
                                                         Map<String, Object> availableVariables) {
        Map<String, Object> outboundVariables = new HashMap<>();
        ProcessVariablesMapping processVariablesMapping = extensions.getExtensions().getMappingForFlowElement(execution.getActivityId());
        Map<String, Mapping> outputMappings = processVariablesMapping.getOutputs();

        for (Map.Entry<String, Mapping> mapping : outputMappings.entrySet()) {

            String name = mapping.getKey();

            VariableDefinition processVariableDefinition = extensions.getExtensions().getPropertyByName(name);

            if (processVariableDefinition != null && calculateOutPutMappedValue(mapping.getValue(),
                    availableVariables).isPresent()) {
                outboundVariables.put(name, calculateOutPutMappedValue(mapping.getValue(),
                        availableVariables).get());
            }
        }

        return outboundVariables;
    }
}