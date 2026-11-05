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

import static org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext.buildMappingExecutionContext;
import static org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider.JSON_PATCH_MAPPING_ERROR;
import static org.activiti.runtime.api.impl.LenientJsonPatch.MAX_ARRAY_INDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.flipkart.zjsonpatch.InvalidJsonPatchException;
import com.flipkart.zjsonpatch.JsonPatchTestFailedException;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tools.jackson.databind.exc.JsonNodeException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExtensionsVariablesMappingProviderJsonPatchArrayTest {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();
    private static final String EXTENSIONS_FILE =
        "src/test/resources/jsonPatch/jsonPatch-array-index-mapping-output.json";
    private static final String PROCESS_DEFINITION_KEY = "Process_jsonPatchArrayIndexMappingOutput";
    private static final String PROCESS_DEFINITION_ID = "procDefId";
    private static final List<String> ARRAY_TO_LOOP_OVER = List.of("ONE", "TWO", "THREE", "FOUR", "FIVE");

    @Mock
    private ProcessExtensionService processExtensionService;

    private ExtensionsVariablesMappingProvider variablesMappingProvider;

    private Extension extension;

    private DelegateExecution execution;

    @BeforeEach
    void setUp() {
        extension = JSON_MAPPER.readValue(new File(EXTENSIONS_FILE), ProcessExtensionModel.class).getExtensions(
            PROCESS_DEFINITION_KEY
        );

        execution = mock(DelegateExecution.class);
        given(execution.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(processExtensionService.getExtensionsForId(PROCESS_DEFINITION_ID)).willReturn(extension);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, extension);
        VariableParsingService variableParsingService = new VariableParsingService(
            Map.of("json", new JsonObjectVariableType(JSON_MAPPER), "array", new JsonObjectVariableType(JSON_MAPPER))
        );

        variablesMappingProvider = new ExtensionsVariablesMappingProvider(
            processExtensionService,
            expressionResolver,
            variableParsingService,
            JSON_MAPPER
        );
    }

    @Test
    void should_createChildArray_when_childPropertyIsUninitialized() {
        Object result = simulateParallelIndexedWrites(
            "writeUninitializedChildPropertyArray",
            "uninitializedChildPropertyArray"
        );

        assertThat(result).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
    }

    @Test
    void should_fillChildArray_when_childPropertyIsInitializedAsEmptyArray() {
        Object result = simulateParallelIndexedWrites(
            "writeInitializedChildPropertyArray",
            "initializedChildPropertyArray"
        );

        assertThat(result).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
    }

    @Test
    void should_keepArrayVariableAsArray_when_arrayVariableIsUninitialized() {
        Object result = simulateParallelIndexedWrites("writeUninitializedArray", "uninitializedArray");

        assertThat(result).isEqualTo(ARRAY_TO_LOOP_OVER);
    }

    @Test
    void should_keepArrayVariableAsArray_when_arrayVariableIsInitializedAsEmptyArray() {
        Object result = simulateParallelIndexedWrites("writeInitializedArray", "initializedArray");

        assertThat(result).isEqualTo(ARRAY_TO_LOOP_OVER);
    }

    @Test
    void should_overwriteElementsAtIndex_when_childArrayIsAlreadyPopulated() {
        Object result = simulateParallelIndexedWrites(
            "writeAlreadyPopulatedChildPropertyArray",
            "alreadyPopulatedChildPropertyArray"
        );

        assertThat(result).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
    }

    @Test
    void should_overwriteElementsAtIndexAndKeepOthers_when_arrayVariableIsAlreadyPopulated() {
        Object result = simulateParallelIndexedWrites("writeAlreadyPopulatedArray", "alreadyPopulatedArray");

        assertThat(result).isEqualTo(List.of("ONE", "TWO", "THREE", "FOUR", "FIVE", "F"));
    }

    @Test
    void should_storeEachItemAtItsIndex_when_writesCompleteOutOfOrder() {
        Object childArrayResult = simulateParallelIndexedWrites(
            "writeUninitializedChildPropertyArray",
            "uninitializedChildPropertyArray",
            3,
            0,
            4,
            1,
            2
        );
        Object arrayResult = simulateParallelIndexedWrites(
            "writeUninitializedArray",
            "uninitializedArray",
            4,
            2,
            0,
            3,
            1
        );

        assertThat(childArrayResult).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
        assertThat(arrayResult).isEqualTo(ARRAY_TO_LOOP_OVER);
    }

    @Test
    void should_fillGapsWithNull_when_writingIndexBeyondArraySize() {
        Object result = writeAtIndex("writeInitializedChildPropertyArray", "initializedChildPropertyArray", 3, "FOUR");

        assertThat(result).isEqualTo(Map.of("documents", Arrays.asList(null, null, null, "FOUR")));
    }

    @Test
    void should_fillGapsWithNull_when_indexIsAtTheLimit() {
        Object result = writeAtIndex(
            "writeInitializedChildPropertyArray",
            "initializedChildPropertyArray",
            MAX_ARRAY_INDEX,
            "X"
        );

        assertThat(result)
            .asInstanceOf(InstanceOfAssertFactories.map(String.class, Object.class))
            .extractingByKey("documents")
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .hasSize(MAX_ARRAY_INDEX + 1)
            .endsWith("X")
            .containsOnlyOnce("X");
    }

    @Test
    void should_fail_when_arrayIndexIsBeyondTheLimit() {
        int index = MAX_ARRAY_INDEX + 1;

        assertThatThrownBy(() ->
            writeAtIndex("writeInitializedChildPropertyArray", "initializedChildPropertyArray", index, "X")
        )
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .cause()
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(
                "Array index " +
                    index +
                    " in JsonPatch path '/documents/" +
                    index +
                    "' is out of range: the array has 0 elements and cannot be extended beyond index " +
                    MAX_ARRAY_INDEX
            );
    }

    @Test
    void should_fail_when_arrayIndexIsNegative() {
        assertThatThrownBy(() ->
            writeAtIndex("writeInitializedChildPropertyArray", "initializedChildPropertyArray", -1, "X")
        )
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .cause()
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage("Invalid array index '-1' in JsonPatch path '/documents/-1': expected a non-negative integer");
    }

    @Test
    void should_removeElementAndShiftFollowingOnes_when_removingArrayElementByIndex() {
        Object result = runOutputMapping("removeByIndex", "populatedChildPropertyArray");

        assertThat(result).isEqualTo(Map.of("documents", List.of("A", "C")));
    }

    @Test
    void should_failWithoutModifyingArray_when_testOperationOnArrayIndexDoesNotMatch() {
        assertThatThrownBy(() -> runOutputMapping("testByIndex", "populatedChildPropertyArray"))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .hasCauseInstanceOf(JsonPatchTestFailedException.class);
    }

    @Test
    void should_appendElement_when_usingEndOfArrayToken() {
        Object result = runOutputMapping("endOfArrayToken", "populatedChildPropertyArray");

        assertThat(result).isEqualTo(Map.of("documents", List.of("A", "B", "C", "X")));
    }

    @Test
    void should_createArray_when_usingEndOfArrayTokenOnMissingParent() {
        Object result = runOutputMapping("endOfArrayTokenOnMissingParent", "emptyJson");

        assertThat(result).isEqualTo(Map.of("documents", List.of("X")));
    }

    @Test
    void should_createArrayOfObjects_when_numericIntermediateSegmentPointsToMissingContainer() {
        Object result = runOutputMapping("numericIntermediateSegment", "emptyJson");

        assertThat(result).isEqualTo(Map.of("items", List.of(Map.of("name", "X"))));
    }

    @Test
    void should_failWithClearError_when_patchEntryHasNoPath() {
        assertThatThrownBy(() -> runOutputMapping("patchEntryWithoutPath", "emptyJson"))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .cause()
            .isInstanceOf(InvalidJsonPatchException.class)
            .hasMessageContaining("missing 'path'");
    }

    @Test
    void should_failInsteadOfOverwritingScalar_when_intermediateSegmentIsScalar() {
        assertThatThrownBy(() -> runOutputMapping("scalarIntermediateSegment", "scalarChildProperty"))
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .cause()
            .isInstanceOf(JsonNodeException.class)
            .hasMessageContaining("Cannot replace `JsonNode` of type `tools.jackson.databind.node.StringNode`")
            .hasMessageContaining("\"/documents\"");
    }

    @Test
    void should_treatNumericSegmentAsKey_when_parentIsObject() {
        Object result = writeAtIndex("numericKeyInObject", "objectWithNumericKeys", 3, "THREE");

        assertThat(result).isEqualTo(Map.of("byId", Map.of("7", "SEVEN", "3", "THREE")));
    }

    @Test
    void should_applyOperationsInOrder_when_patchWritesSeveralIndexes() {
        Object result = runOutputMapping("multipleIndexedWrites", "emptyJson");

        assertThat(result).isEqualTo(Map.of("documents", List.of("X", "Y")));
    }

    @Test
    void should_setValue_when_replacingMissingObjectKey() {
        Object result = runOutputMapping("replaceMissingKey", "emptyJson");

        assertThat(result).isEqualTo(Map.of("person", Map.of("name", "X")));
    }

    @Test
    void should_ignoreRemoval_when_pathDoesNotExist() {
        Object result = runOutputMapping("removeMissingPath", "populatedChildPropertyArray");

        assertThat(result).isEqualTo(Map.of("documents", List.of("A", "B", "C")));
    }

    private Object simulateParallelIndexedWrites(String activityId, String variableName, int... completionOrder) {
        int[] order = completionOrder.length > 0 ? completionOrder : new int[] { 0, 1, 2, 3, 4 };
        Object value = defaultValueOf(variableName);
        for (int index : order) {
            value = runOutputMapping(
                activityId,
                variableName,
                value,
                index,
                Map.of("item", ARRAY_TO_LOOP_OVER.get(index))
            );
        }
        return value;
    }

    private Object writeAtIndex(String activityId, String variableName, int index, String item) {
        return runOutputMapping(activityId, variableName, defaultValueOf(variableName), index, Map.of("item", item));
    }

    private Object runOutputMapping(String activityId, String variableName) {
        return runOutputMapping(activityId, variableName, defaultValueOf(variableName), 0, Collections.emptyMap());
    }

    private Object runOutputMapping(
        String activityId,
        String variableName,
        Object currentValue,
        int arrayIndex,
        Map<String, Object> taskVariables
    ) {
        given(execution.getCurrentActivityId()).willReturn(activityId);

        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("arrayIndex", arrayIndex);
        processVariables.put(variableName, currentValue);
        ExpressionResolverHelper.setExecutionVariables(execution, processVariables);

        return variablesMappingProvider
            .calculateOutPutVariables(buildMappingExecutionContext(execution), taskVariables)
            .get(variableName);
    }

    private Object defaultValueOf(String variableName) {
        return extension.getPropertyByName(variableName).getValue();
    }
}
