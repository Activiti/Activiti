/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ConstantDefinition;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessConstantsMapping;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableParsingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionsVariablesMappingProvider implements VariablesCalculator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionsVariablesMappingProvider.class);

    private ProcessExtensionService processExtensionService;

    private ExpressionResolver expressionResolver;

    private VariableParsingService variableParsingService;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("/\\$\\{(\\w+)}");

    public static final String JSON_PATCH_MAPPING_ERROR = "Invalid jsonPatch variable mapping";

    public ExtensionsVariablesMappingProvider(
        ProcessExtensionService processExtensionService,
        ExpressionResolver expressionResolver,
        VariableParsingService variableParsingService
    ) {
        this.processExtensionService = processExtensionService;
        this.expressionResolver = expressionResolver;
        this.variableParsingService = variableParsingService;
    }

    protected Optional<Object> calculateMappedValue(
        Mapping inputMapping,
        DelegateExecution execution,
        Extension extensions
    ) {
        if (inputMapping != null) {
            if (Mapping.SourceMappingType.VALUE.equals(inputMapping.getType())) {
                return Optional.of(inputMapping.getValue());
            }

            if (Mapping.SourceMappingType.VARIABLE.equals(inputMapping.getType())) {
                String name = inputMapping.getValue().toString();

                if (isTargetProcessVariableDefined(extensions, execution, name)) {
                    return Optional.ofNullable(execution.getVariable(name));
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

        if (extensions.shouldMapAllInputs(execution.getCurrentActivityId())) {
            Map<String, Object> variables = new HashMap<>(constants);
            variables.putAll(execution.getVariables());
            return variables;
        }

        Map<String, Object> inboundVariables = calculateInputVariables(execution, extensions);
        inboundVariables = expressionResolver.resolveExpressionsMap(
            new VariableScopeExpressionEvaluator(execution),
            inboundVariables
        );
        inboundVariables.putAll(constants);
        return inboundVariables;
    }

    private Map<String, Object> calculateConstants(DelegateExecution execution, Extension extensions) {
        Map<String, Object> constants = new HashMap<>();

        ProcessConstantsMapping processConstantsMapping = extensions.getConstantForFlowElement(
            execution.getCurrentActivityId()
        );
        for (Map.Entry<String, ConstantDefinition> mapping : processConstantsMapping.entrySet()) {
            constants.put(mapping.getKey(), mapping.getValue().getValue());
        }
        return constants;
    }

    private Map<String, Object> calculateInputVariables(DelegateExecution execution, Extension extensions) {
        Map<String, Object> inboundVariables = new HashMap<>();

        ProcessVariablesMapping processVariablesMapping = extensions.getMappingForFlowElement(
            execution.getCurrentActivityId()
        );

        Map<String, Mapping> inputMappings = processVariablesMapping.getInputs();
        for (Map.Entry<String, Mapping> mapping : inputMappings.entrySet()) {
            Optional<Object> mappedValue = calculateMappedValue(mapping.getValue(), execution, extensions);
            mappedValue.ifPresent(value -> inboundVariables.put(mapping.getKey(), value));
        }
        return inboundVariables;
    }

    private Optional<Object> calculateOutPutMappedValue(
        Map.Entry<String, Mapping> mappingEntry,
        Map<String, Object> currentContextVariables,
        DelegateExecution execution,
        Extension extensions
    ) {
        Mapping mapping = mappingEntry.getValue();
        if (mapping == null || mapping.getType() == null) {
            return Optional.empty();
        }

        switch (mapping.getType()) {
            case VALUE:
                return Optional.of(mapping.getValue());
            case JSONPATCH:
                return resolvePatchMapping(mappingEntry.getKey(), mapping.getValue(), execution, extensions);
            case VARIABLE:
                if (currentContextVariables != null) {
                    return Optional.ofNullable(currentContextVariables.get(mapping.getValue().toString()));
                }
            default:
                return Optional.empty();
        }
    }

    private Optional<Object> resolvePatchMapping(
        String outputVariableName,
        Object changesToApply,
        DelegateExecution execution,
        Extension extensions
    ) {
        Object executionVariableValue = execution != null ? execution.getVariable(outputVariableName) : null;
        Object processVariableCurrentValue = calculateProcessVariableCurrentValue(
            executionVariableValue,
            extensions.getPropertyByName(outputVariableName)
        );

        try {
            JsonNode oldNode;
            if (isObjectVariable(processVariableCurrentValue)) {
                oldNode = objectMapper.convertValue(processVariableCurrentValue, JsonNode.class);
            } else {
                oldNode = objectMapper.createObjectNode();
            }

            JsonNode patchNode = objectMapper.convertValue(changesToApply, JsonNode.class);

            replaceVariablesInJsonPath(patchNode, execution, extensions);
            initializePath(oldNode, patchNode);

            JsonNode patchedNode = JsonPatch.apply(patchNode, oldNode);

            return Optional.ofNullable(objectMapper.treeToValue(patchedNode, Object.class));
        } catch (Exception e) {
            LOGGER.error(
                "Error patching variable. Changes to apply: {}, Process variable current value: {}",
                changesToApply,
                processVariableCurrentValue,
                e
            );
            throw new ActivitiIllegalArgumentException(JSON_PATCH_MAPPING_ERROR, e);
        }
    }

    private boolean isObjectVariable(Object variable) {
        return variable instanceof ObjectNode || variable instanceof Map;
    }

    private void replaceVariablesInJsonPath(JsonNode patchNode, DelegateExecution execution, Extension extensions) {
        for (JsonNode patch : patchNode) {
            if (patch.has("path")) {
                String path = patch.get("path").asText();
                String updatedPath = resolvePath(path, execution, extensions);

                // Only update if there was a variable in the path
                if (!path.equals(updatedPath) && patch instanceof ObjectNode) {
                    ((ObjectNode) patch).put("path", updatedPath);
                }
            }
        }
    }

    private String resolvePath(String path, DelegateExecution execution, Extension extensions) {
        Matcher matcher = VARIABLE_PATTERN.matcher(path);
        StringBuilder updatedPath = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1); // Extract variable name without `${..}`
            String replacedValue = replacePathVariables(variableName, execution, extensions);
            matcher.appendReplacement(updatedPath, "/" + replacedValue);
        }

        matcher.appendTail(updatedPath);
        return updatedPath.toString();
    }

    private String replacePathVariables(String variableName, DelegateExecution execution, Extension extensions) {
        if (!isTargetProcessVariableDefined(extensions, execution, variableName)) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Path variable $%s used in JsonPatch mapping is not defined for the current process",
                    variableName
                )
            );
        }

        VariableInstance variableInstance = execution != null ? execution.getVariableInstance(variableName) : null;
        if (variableInstance != null) {
            return replaceVariableIfSupported(
                variableInstance.getValue(),
                variableInstance.getTypeName(),
                variableName
            );
        }

        VariableDefinition propertyObj = extensions.getPropertyByName(variableName);
        return replaceVariableIfSupported(propertyObj.getValue(), propertyObj.getType(), variableName);
    }

    private String replaceVariableIfSupported(Object value, String type, String originalProperty) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            throw new ActivitiIllegalArgumentException(
                String.format("Path variable $%s used in JsonPatch mapping should not be empty", originalProperty)
            );
        }

        String typeLowerCase = type.toLowerCase();
        if ("string".equals(typeLowerCase) || "integer".equals(typeLowerCase)) {
            return value.toString();
        }

        throw new ActivitiIllegalArgumentException(
            String.format(
                "Variable %s of type '%s' is not allowed in JsonPatch mapping. Only string and integer types are allowed",
                originalProperty,
                type
            )
        );
    }

    private void initializePath(JsonNode oldNode, JsonNode patchNode) {
        for (JsonNode patch : patchNode) {
            String path = patch.get("path").asText();
            String[] properties = path.split("/");

            JsonNode currentNode = oldNode;

            for (int i = 1; i < properties.length; i++) {
                String property = properties[i];
                if(isArrayElementPath(i, properties, property)) {
                    prepareArrayElementForReplace((ObjectNode) patch, currentNode, property);
                }
                else if (isArrayProperty(currentNode, property)) {
                    currentNode = handleArrayPath(property, currentNode);
                } else {
                    if (!currentNode.has(property) || !currentNode.get(property).isObject()) {
                        ((ObjectNode) currentNode).set(property, objectMapper.createObjectNode());
                    }
                    currentNode = currentNode.get(property);
                }
            }
        }
    }

    private boolean isArrayProperty(JsonNode node, String property) {
        return (
            node.isArray() ||
            ((!node.isEmpty() && (node.has(property) && node.get(property).isArray())) || property.matches("\\d+"))
        );
    }

    private void prepareArrayElementForReplace(ObjectNode patch, JsonNode currentNode, String property) {
        if (currentNode.isArray()) {
            patch.put("op", "replace");
            ArrayNode arrayNode = (ArrayNode) currentNode;
            int index = Integer.parseInt(property);
            while (arrayNode.size() <= index) {
                arrayNode.add(objectMapper.createObjectNode());
            }
        }
    }

    private boolean isArrayElementPath(int index, String[] properties, String property) {
        return index == properties.length - 1 && StringUtils.isNumeric(property);
    }

    private JsonNode handleArrayPath(String property, JsonNode currentNode) {
        if (!currentNode.isArray()) {
            return currentNode.get(property);
        }
        int index = Integer.parseInt(property);
        ArrayNode arrayNode = (ArrayNode) currentNode;

        while (arrayNode.size() <= index) {
            arrayNode.add(objectMapper.createObjectNode());
        }

        return arrayNode.get(index);
    }

    public Map<String, Object> calculateOutPutVariables(
        MappingExecutionContext mappingExecutionContext,
        Map<String, Object> availableVariables
    ) {
        Extension extensions = processExtensionService.getExtensionsForId(
            mappingExecutionContext.getProcessDefinitionId()
        );

        if (!extensions.hasMapping(mappingExecutionContext.getActivityId())) {
            return emptyMap();
        }

        if (expressionResolver.containsExpression(availableVariables)) {
            throw new ActivitiIllegalArgumentException(
                "Expressions are not allowed as variable values in the output mapping"
            );
        }

        if (extensions.shouldMapAllOutputs(mappingExecutionContext.getActivityId())) {
            return (availableVariables != null ? new HashMap<>(availableVariables) : emptyMap());
        }

        return calculateOutPutVariables(mappingExecutionContext, extensions, availableVariables);
    }

    private Map<String, Object> calculateOutPutVariables(
        MappingExecutionContext mappingExecutionContext,
        Extension extensions,
        Map<String, Object> availableVariables
    ) {
        Map<String, Object> outboundVariables = new HashMap<>();
        ProcessVariablesMapping processVariablesMapping = extensions.getMappingForFlowElement(
            mappingExecutionContext.getActivityId()
        );
        Map<String, Mapping> outputMappings = processVariablesMapping.getOutputs();
        DelegateExecution execution = mappingExecutionContext.getExecution();

        for (Map.Entry<String, Mapping> mappingEntry : outputMappings.entrySet()) {
            String name = mappingEntry.getKey();

            if (isTargetProcessVariableDefined(extensions, execution, name)) {
                calculateOutPutMappedValue(mappingEntry, availableVariables, execution, extensions).ifPresent(value -> {
                        extensions
                            .getProperties()
                            .values()
                            .stream()
                            .filter(v -> v.getName().equals(name))
                            .findAny()
                            .ifPresentOrElse(
                                v ->
                                    outboundVariables.put(
                                        name,
                                        variableParsingService.parse(new VariableDefinition(v.getType(), value))
                                    ),
                                () -> outboundVariables.put(name, value)
                            );
                    });
            }
        }

        return resolveExpressions(mappingExecutionContext, availableVariables, outboundVariables);
    }

    private Object calculateProcessVariableCurrentValue(
        Object executionVariableValue,
        VariableDefinition propertyVariableDefinition
    ) {
        return !isProcessVariableNull(executionVariableValue)
            ? executionVariableValue
            : propertyVariableDefinition.getValue();
    }

    private boolean isProcessVariableNull(Object variable) {
        return variable == null || NullNode.getInstance().equals(variable);
    }

    private Map<String, Object> resolveExpressions(
        MappingExecutionContext mappingExecutionContext,
        Map<String, Object> availableVariables,
        Map<String, Object> outboundVariables
    ) {
        if (mappingExecutionContext.hasExecution()) {
            return resolveExecutionExpressions(mappingExecutionContext, availableVariables, outboundVariables);
        } else {
            return expressionResolver.resolveExpressionsMap(
                new SimpleMapExpressionEvaluator(availableVariables),
                outboundVariables
            );
        }
    }

    private Map<String, Object> resolveExecutionExpressions(
        MappingExecutionContext mappingExecutionContext,
        Map<String, Object> availableVariables,
        Map<String, Object> outboundVariables
    ) {
        if (availableVariables != null && !availableVariables.isEmpty()) {
            return expressionResolver.resolveExpressionsMap(
                new CompositeVariableExpressionEvaluator(
                    new SimpleMapExpressionEvaluator(availableVariables),
                    new VariableScopeExpressionEvaluator(mappingExecutionContext.getExecution())
                ),
                outboundVariables
            );
        }
        return expressionResolver.resolveExpressionsMap(
            new VariableScopeExpressionEvaluator(mappingExecutionContext.getExecution()),
            outboundVariables
        );
    }

    private boolean isTargetProcessVariableDefined(
        Extension extensions,
        DelegateExecution execution,
        String variableName
    ) {
        return (
            extensions.getPropertyByName(variableName) != null ||
            (execution != null && execution.getVariable(variableName) != null)
        );
    }
}
