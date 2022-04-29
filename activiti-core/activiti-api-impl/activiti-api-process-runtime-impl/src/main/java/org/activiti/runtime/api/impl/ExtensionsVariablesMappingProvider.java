/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.runtime.api.impl;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ConstantDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessConstantsMapping;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;

public class ExtensionsVariablesMappingProvider implements VariablesCalculator {

    private ProcessExtensionService processExtensionService;

    private ExpressionResolver expressionResolver;

    public ExtensionsVariablesMappingProvider(ProcessExtensionService processExtensionService,
                                    ExpressionResolver expressionResolver) {
        this.processExtensionService = processExtensionService;
        this.expressionResolver = expressionResolver;
    }

    protected Optional<Object> calculateMappedValue(Mapping inputMapping,
                                                    DelegateExecution execution,
                                                    Extension extensions) {
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return Optional.of(inputMapping.getValue());
            }

            if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                String name = inputMapping.getValue().toString();

                VariableDefinition processVariableDefinition = extensions.getPropertyByName(name);
                if (processVariableDefinition != null) {
                    return Optional.ofNullable(execution.getVariable(processVariableDefinition.getName()));
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> calculateInputVariables(DelegateExecution execution) {

        Extension extensions = processExtensionService.getExtensionsForId(execution.getProcessDefinitionId());

        Map<String, Object> constants = calculateConstants(execution, extensions);

        if (!extensions.hasMapping(execution.getCurrentActivityId())) {
            return constants;
        }

        Map<String, Object> inboundVariables;

        if (extensions.shouldMapAllInputs(execution.getCurrentActivityId())) {
            inboundVariables = execution.getVariables();
        } else {
            inboundVariables = calculateInputVariables(execution, extensions);
        }
        inboundVariables = expressionResolver.resolveExpressionsMap(new VariableScopeExpressionEvaluator(execution), inboundVariables);
        inboundVariables.putAll(constants);
        return inboundVariables;
    }

    private Map<String, Object> calculateConstants(DelegateExecution execution,
                                                   Extension extensions) {
        Map<String, Object> constants = new HashMap<>();

        ProcessConstantsMapping processConstantsMapping = extensions.getConstantForFlowElement(execution.getCurrentActivityId());
        for (Map.Entry<String, ConstantDefinition> mapping : processConstantsMapping.entrySet()) {
            constants.put(mapping.getKey(), mapping.getValue().getValue());
        }
        return constants;
    }

    private Map<String, Object> calculateInputVariables(DelegateExecution execution,
                                                        Extension extensions) {
        Map<String, Object> inboundVariables = new HashMap<>();

        ProcessVariablesMapping processVariablesMapping = extensions.getMappingForFlowElement(execution.getCurrentActivityId());

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

                    return currentContextVariables != null ? Optional.ofNullable(currentContextVariables.get(name)) : Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> calculateOutPutVariables(MappingExecutionContext mappingExecutionContext,
                                                        Map<String, Object> availableVariables) {

        Extension extensions = processExtensionService.getExtensionsForId(mappingExecutionContext.getProcessDefinitionId());

        if (!extensions.hasMapping(mappingExecutionContext.getActivityId())) {
            return emptyMap();
        }

        if (extensions.shouldMapAllOutputs(mappingExecutionContext.getActivityId())) {
            return (availableVariables != null ? new HashMap<>(availableVariables) : emptyMap());
        }

        if (availableVariables != null && !availableVariables.isEmpty()) {
            if (expressionResolver.containsExpression(availableVariables)) {
                throw new ActivitiIllegalArgumentException("Expressions are not allowed as variable values in the output mapping");
            }
            return calculateOutPutVariables(mappingExecutionContext, extensions, availableVariables);
        } else {
            return emptyMap();
        }
    }

    private Map<String, Object> calculateOutPutVariables(MappingExecutionContext mappingExecutionContext,
                                                         Extension extensions,
                                                         Map<String, Object> availableVariables) {
        Map<String, Object> outboundVariables = new HashMap<>();
        ProcessVariablesMapping processVariablesMapping = extensions.getMappingForFlowElement(mappingExecutionContext.getActivityId());
        Map<String, Mapping> outputMappings = processVariablesMapping.getOutputs();

        for (Map.Entry<String, Mapping> mapping : outputMappings.entrySet()) {

            String name = mapping.getKey();

            VariableDefinition processVariableDefinition = extensions.getPropertyByName(name);

            if (processVariableDefinition != null) {
                calculateOutPutMappedValue(mapping.getValue(),
                                           availableVariables)
                                                              .ifPresent(value -> outboundVariables.put(name, value));
            }
        }

        return expressionResolver.resolveExpressionsMap(new SimpleMapExpressionEvaluator(availableVariables
            ),
                                                        outboundVariables);
    }
}
