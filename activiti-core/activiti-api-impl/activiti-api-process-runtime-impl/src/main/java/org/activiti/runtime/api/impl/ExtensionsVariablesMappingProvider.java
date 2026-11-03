/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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
import static org.activiti.spring.process.model.Mapping.SourceMappingType.VARIABLE;

import com.flipkart.zjsonpatch.Jackson3JsonPatch;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.activiti.bpmn.model.CallActivity;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

public class ExtensionsVariablesMappingProvider implements VariablesCalculator {

    private final JsonMapper jsonMapper;

    private ProcessExtensionService processExtensionService;

    private ExpressionResolver expressionResolver;

    private VariableParsingService variableParsingService;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("/\\$\\{(\\w+)}");

    public static final String JSON_PATCH_MAPPING_ERROR = "Invalid jsonPatch variable mapping";

    private static final List<String> OUTPUT_CATEGORY = List.of("output", "input/output");

    static final int MAX_ARRAY_INDEX_GAP = 10_000;

    private static final String ARRAY_VARIABLE_TYPE = "array";

    private static final String END_OF_ARRAY_TOKEN = "-";

    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\d+");

    private static final String OPERATION_FIELD = "op";

    private static final String ADD_OPERATION = "add";

    private static final String REPLACE_OPERATION = "replace";

    private static final String REMOVE_OPERATION = "remove";

    private static final Set<String> CONTAINER_CREATING_OPERATIONS = Set.of(
        ADD_OPERATION,
        REPLACE_OPERATION,
        "move",
        "copy"
    );

    private static final Set<String> SET_AT_INDEX_OPERATIONS = Set.of(ADD_OPERATION, REPLACE_OPERATION);

    public ExtensionsVariablesMappingProvider(
        ProcessExtensionService processExtensionService,
        ExpressionResolver expressionResolver,
        VariableParsingService variableParsingService,
        JsonMapper jsonMapper
    ) {
        this.processExtensionService = processExtensionService;
        this.expressionResolver = expressionResolver;
        this.variableParsingService = variableParsingService;
        this.jsonMapper = jsonMapper;
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

            if (VARIABLE.equals(inputMapping.getType())) {
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

    public boolean isMappingEphemeral(DelegateExecution execution) {
        String processDefinitionId = execution.getProcessDefinitionId();
        String activityId = execution.getCurrentActivityId();

        return Optional.ofNullable(processExtensionService.getExtensionsForId(processDefinitionId))
            .map(Extension::getMappings)
            .map(mappings -> mappings.get(activityId))
            .map(ProcessVariablesMapping::isEphemeral)
            .orElse(false);
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
        VariableDefinition variableDefinition = extensions.getPropertyByName(outputVariableName);
        Object processVariableCurrentValue = calculateProcessVariableCurrentValue(
            executionVariableValue,
            variableDefinition
        );

        try {
            JsonNode patchNode = jsonMapper.convertValue(changesToApply, JsonNode.class);
            replaceVariablesInJsonPath(patchNode, execution, extensions);

            JsonNode oldNode = toRootNode(processVariableCurrentValue, variableDefinition, patchNode);
            JsonNode patchedNode = applyPatch(oldNode, patchNode);

            return Optional.ofNullable(jsonMapper.treeToValue(patchedNode, Object.class));
        } catch (Exception e) {
            throw new ActivitiIllegalArgumentException(JSON_PATCH_MAPPING_ERROR, e);
        }
    }

    private JsonNode applyPatch(JsonNode document, JsonNode patchNode) {
        if (!patchNode.isArray()) {
            return Jackson3JsonPatch.apply(patchNode, document);
        }
        JsonNode result = document;
        for (JsonNode operation : patchNode) {
            if (prepareOperation(result, operation)) {
                result = Jackson3JsonPatch.apply(jsonMapper.createArrayNode().add(operation), result);
            }
        }
        return result;
    }

    private JsonNode toRootNode(Object currentValue, VariableDefinition variableDefinition, JsonNode patchNode) {
        if (isContainerVariable(currentValue)) {
            return jsonMapper.convertValue(currentValue, JsonNode.class);
        }
        if (isArrayVariable(variableDefinition) || isArrayToken(firstPathSegment(patchNode))) {
            return jsonMapper.createArrayNode();
        }
        return jsonMapper.createObjectNode();
    }

    private boolean isContainerVariable(Object variable) {
        return (
            (variable instanceof JsonNode jsonNode && jsonNode.isContainer()) ||
            variable instanceof Map ||
            variable instanceof Collection
        );
    }

    private boolean isArrayVariable(VariableDefinition variableDefinition) {
        return variableDefinition != null && ARRAY_VARIABLE_TYPE.equalsIgnoreCase(variableDefinition.getType());
    }

    private String firstPathSegment(JsonNode patchNode) {
        for (JsonNode patch : patchNode) {
            String path = textField(patch, "path");
            if (path != null) {
                String[] segments = path.split("/", -1);
                return segments.length > 1 ? segments[1] : null;
            }
        }
        return null;
    }

    private void replaceVariablesInJsonPath(JsonNode patchNode, DelegateExecution execution, Extension extensions) {
        for (JsonNode patch : patchNode) {
            if (patch.has("path")) {
                String path = patch.get("path").asString();
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

    private boolean prepareOperation(JsonNode document, JsonNode operation) {
        String op = textField(operation, OPERATION_FIELD);
        String path = textField(operation, "path");
        if (op == null || path == null || !path.startsWith("/")) {
            return true;
        }
        if (REMOVE_OPERATION.equals(op)) {
            return !document.at(path).isMissingNode();
        }
        if (!CONTAINER_CREATING_OPERATIONS.contains(op)) {
            return true;
        }

        String[] segments = path.split("/", -1);
        JsonNode parent = document;
        for (int i = 1; i < segments.length - 1; i++) {
            parent = getOrCreateContainer(parent, segments[i], segments[i + 1], path);
        }

        String lastSegment = segments[segments.length - 1];
        if (SET_AT_INDEX_OPERATIONS.contains(op) && parent instanceof ArrayNode arrayNode) {
            prepareSetAtIndex((ObjectNode) operation, arrayNode, lastSegment, path);
        } else if (
            REPLACE_OPERATION.equals(op) &&
            parent instanceof ObjectNode objectNode &&
            !objectNode.has(unescapeJsonPointerSegment(lastSegment))
        ) {
            ((ObjectNode) operation).put(OPERATION_FIELD, ADD_OPERATION);
        }
        return true;
    }

    private String textField(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && field.isString() ? field.asString() : null;
    }

    private JsonNode getOrCreateContainer(JsonNode parent, String segment, String nextSegment, String path) {
        JsonNode child;
        if (parent instanceof ArrayNode arrayNode) {
            int index = parseArrayIndex(segment, arrayNode, path);
            padWithNulls(arrayNode, index + 1);
            child = arrayNode.get(index);
            if (child.isNull()) {
                child = createContainerFor(nextSegment);
                arrayNode.set(index, child);
            }
        } else {
            ObjectNode objectNode = (ObjectNode) parent;
            String key = unescapeJsonPointerSegment(segment);
            child = objectNode.get(key);
            if (child == null || child.isNull()) {
                child = createContainerFor(nextSegment);
                objectNode.set(key, child);
            }
        }

        if (!child.isContainer()) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Cannot apply JsonPatch path '%s': segment '%s' holds a %s value instead of an object or array",
                    path,
                    segment,
                    child.getNodeType().name().toLowerCase(Locale.ROOT)
                )
            );
        }
        return child;
    }

    private void prepareSetAtIndex(ObjectNode operation, ArrayNode arrayNode, String segment, String path) {
        if (END_OF_ARRAY_TOKEN.equals(segment)) {
            return;
        }
        int index = parseArrayIndex(segment, arrayNode, path);
        padWithNulls(arrayNode, index + 1);
        operation.put(OPERATION_FIELD, REPLACE_OPERATION);
    }

    private int parseArrayIndex(String segment, ArrayNode arrayNode, String path) {
        int index = toArrayIndex(segment);
        if (index < 0) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Invalid array index '%s' in JsonPatch path '%s': expected a non-negative integer",
                    segment,
                    path
                )
            );
        }

        if (index - arrayNode.size() > MAX_ARRAY_INDEX_GAP) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Array index %d in JsonPatch path '%s' is out of range: the array has %d elements and at most %d missing elements can be filled with null",
                    index,
                    path,
                    arrayNode.size(),
                    MAX_ARRAY_INDEX_GAP
                )
            );
        }
        return index;
    }

    private int toArrayIndex(String segment) {
        if (!ARRAY_INDEX_PATTERN.matcher(segment).matches()) {
            return -1;
        }
        try {
            return Integer.parseInt(segment);
        } catch (NumberFormatException _) {
            return -1;
        }
    }

    private void padWithNulls(ArrayNode arrayNode, int size) {
        while (arrayNode.size() < size) {
            arrayNode.addNull();
        }
    }

    private JsonNode createContainerFor(String nextSegment) {
        return isArrayToken(nextSegment) ? jsonMapper.createArrayNode() : jsonMapper.createObjectNode();
    }

    private boolean isArrayToken(String segment) {
        return (
            segment != null && (END_OF_ARRAY_TOKEN.equals(segment) || ARRAY_INDEX_PATTERN.matcher(segment).matches())
        );
    }

    private String unescapeJsonPointerSegment(String segment) {
        return segment.replace("~1", "/").replace("~0", "~");
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

        List<String> variableNamesWithExpressions = expressionResolver.findVariableNamesContainingExpressions(
            availableVariables
        );
        if (!variableNamesWithExpressions.isEmpty()) {
            throw new ActivitiIllegalArgumentException(
                String.format(
                    "Expressions are not allowed as variable values in the output mapping for activity '%s'. " +
                        "The following variables contain expressions: %s",
                    mappingExecutionContext.getActivityId(),
                    variableNamesWithExpressions
                )
            );
        }

        if (extensions.shouldMapAllOutputs(mappingExecutionContext.getActivityId())) {
            return availableVariables != null ? new HashMap<>(availableVariables) : emptyMap();
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

        if (outputMappings.isEmpty() && isMultiInstanceCallActivity(execution)) {
            final Predicate<VariableDefinition> isOutputVariableCategory = variableDefinition ->
                Optional.ofNullable(variableDefinition.getCategory())
                    .map(it -> it.toLowerCase(Locale.ROOT))
                    .filter(OUTPUT_CATEGORY::contains)
                    .isPresent();

            final Function<VariableDefinition, Map.Entry<String, Mapping>> toVariableOutputMapping =
                variableDefinition -> {
                    final var mapping = new Mapping();
                    mapping.setType(VARIABLE);
                    mapping.setValue(variableDefinition.getName());

                    return Map.entry(variableDefinition.getName(), mapping);
                };

            outputMappings = Optional.ofNullable(mappingExecutionContext.getSubProcessExecution())
                .map(DelegateExecution::getProcessDefinitionId)
                .map(processExtensionService::getExtensionsForId)
                .map(Extension::getProperties)
                .map(properties ->
                    properties
                        .values()
                        .stream()
                        .filter(variableDefinition ->
                            Optional.of(variableDefinition)
                                .filter(Predicate.not(VariableDefinition::isEphemeral).and(isOutputVariableCategory))
                                .isPresent()
                        )
                        .map(toVariableOutputMapping)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                )
                .orElseGet(Map::of);
        }

        for (Map.Entry<String, Mapping> mappingEntry : outputMappings.entrySet()) {
            String name = mappingEntry.getKey();

            if (isTargetProcessVariableDefined(extensions, execution, name) || isMultiInstanceCallActivity(execution)) {
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
        if (!isProcessVariableNull(executionVariableValue)) {
            return executionVariableValue;
        }
        return propertyVariableDefinition != null ? propertyVariableDefinition.getValue() : null;
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

    private boolean isMultiInstanceCallActivity(DelegateExecution execution) {
        return Optional.ofNullable(execution)
            .filter(isMultiInstanceRootParent().and(isCallActivityFlowElement()))
            .isPresent();
    }

    private Predicate<DelegateExecution> isMultiInstanceRootParent() {
        return execution ->
            Optional.ofNullable(execution.getParent()).filter(DelegateExecution::isMultiInstanceRoot).isPresent();
    }

    private Predicate<DelegateExecution> isCallActivityFlowElement() {
        return execution -> execution.getCurrentFlowElement() instanceof CallActivity;
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
