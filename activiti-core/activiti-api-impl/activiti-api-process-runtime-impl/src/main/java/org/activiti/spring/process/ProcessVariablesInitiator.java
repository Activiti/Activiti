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
package org.activiti.spring.process;

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.VariableValidationService;

public class ProcessVariablesInitiator extends ProcessInstanceHelper {

    private ProcessExtensionService processExtensionService;

    private final VariableParsingService variableParsingService;

    private final VariableValidationService variableValidationService;

    private VariablesCalculator variablesCalculator;

    public ProcessVariablesInitiator(ProcessExtensionService processExtensionService,
                                     VariableParsingService variableParsingService,
                                     VariableValidationService variableValidationService,
                                     VariablesCalculator variablesCalculator) {
        this.processExtensionService = processExtensionService;
        this.variableParsingService = variableParsingService;
        this.variableValidationService = variableValidationService;
        this.variablesCalculator = variablesCalculator;
    }

    public Map<String, Object> calculateVariablesFromExtensionFile(ProcessDefinition processDefinition,
                                                                   Map<String, Object> variables) {
        Map<String, Object> processedVariables = new HashMap<>();
        if (processExtensionService.hasExtensionsFor(processDefinition)) {
            Extension processExtension = processExtensionService.getExtensionsFor(processDefinition);

            Map<String, VariableDefinition> variableDefinitionMap = processExtension.getProperties();
            processedVariables = processVariables(variables, variableDefinitionMap);

            Set<String> missingRequiredVars = checkRequiredVariables(processedVariables,
                    variableDefinitionMap);
            if (!missingRequiredVars.isEmpty()) {
                throw new ActivitiException("Can't start process '" + processDefinition.getKey() + "' without required variables - " + String.join(", ",
                        missingRequiredVars));
            }
            Set<String> varsWithMismatchedTypes = validateVariablesAgainstDefinitions(processedVariables,
                    variableDefinitionMap);
            if (!varsWithMismatchedTypes.isEmpty()) {
                throw new ActivitiException("Can't start process '" + processDefinition.getKey() + "' as variables fail type validation - " + String.join(", ",
                        varsWithMismatchedTypes));
            }
        }

        return processedVariables;
    }

    public Map<String, Object> calculateOutputVariables(Map<String, Object> variables, ProcessDefinition processDefinition, FlowElement initialFlowElement) {
        Map<String, Object> processVariables = variables;

        if (processExtensionService.hasExtensionsFor(processDefinition)) {

            Map<String, Object> calculateOutPutVariables = variablesCalculator.calculateOutPutVariables(MappingExecutionContext.buildMappingExecutionContext(
                processDefinition.getId(),
                initialFlowElement.getId()),
                variables);
            if(!calculateOutPutVariables.isEmpty()) {
                processVariables = calculateOutPutVariables;
            }

            processVariables = calculateVariablesFromExtensionFile(processDefinition,
                processVariables);
        }
        return processVariables;
    }

    private Map<String, Object> processVariables(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Map<String, Object> newVarsMap = new HashMap<>(Optional.ofNullable(variables).orElse(emptyMap()));
        variableDefinitionMap.forEach((k, v) -> {
            if (!newVarsMap.containsKey(v.getName()) && v.getValue() != null) {
                newVarsMap.put(v.getName(), createDefaultVariableValue(v));
            }
        });
        return newVarsMap;
    }

    private Object createDefaultVariableValue(VariableDefinition variableDefinition) {
        // take a default from the variable definition in the proc extensions
        return variableParsingService.parse(variableDefinition);
    }

    private Set<String> checkRequiredVariables(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Set<String> missingRequiredVars = new HashSet<>();
        variableDefinitionMap.forEach((k, v) -> {
            if (!variables.containsKey(v.getName()) && v.isRequired()) {
                missingRequiredVars.add(v.getName());
            }
        });
        return missingRequiredVars;
    }

    private Set<String> validateVariablesAgainstDefinitions(Map<String, Object> variables, Map<String, VariableDefinition> variableDefinitionMap) {
        Set<String> mismatchedVars = new HashSet<>();
        variableDefinitionMap.forEach((k, v) -> {
            //if we have definition for this variable then validate it
            if (variables.containsKey(v.getName())) {
                if (!variableValidationService.validate(variables.get(v.getName()), v)) {
                    mismatchedVars.add(v.getName());
                }
            }
        });
        return mismatchedVars;
    }

    public void startProcessInstance(ExecutionEntity processInstance, CommandContext commandContext, Map<String, Object> variables, FlowElement initialFlowElement, Map<String, Object> transientVariables) {
        ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(processInstance.getProcessDefinitionId());
        Map<String, Object> calculatedVariables = calculateOutputVariables(variables, processDefinition, initialFlowElement);
        super.startProcessInstance(processInstance, commandContext, calculatedVariables, initialFlowElement, transientVariables);
    }
}
