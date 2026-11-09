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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.variable.VariableParsingService;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class ExtensionsVariablesMappingProviderJsonPatchArrayTest {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();
    private static final String EXTENSIONS_FILE =
        "src/test/resources/jsonPatch/jsonPatch-array-index-mapping-output.json";
    private static final String PROCESS_DEFINITION_KEY = "Process_jsonPatchArrayIndexMappingOutput";
    private static final String PROCESS_DEFINITION_ID = "procDefId";
    private static final String INDEX_VARIABLE = "arrayIndex";
    private static final List<String> ARRAY_TO_LOOP_OVER = List.of("ONE", "TWO", "THREE", "FOUR", "FIVE");

    @Mock
    private ProcessExtensionService processExtensionService;

    @Mock
    private DelegateExecution execution;

    @Mock
    private VariableInstance indexVariableInstance;

    private ExtensionsVariablesMappingProvider variablesMappingProvider;

    private Extension extension;

    @BeforeEach
    void setUp() {
        extension = JSON_MAPPER.readValue(new File(EXTENSIONS_FILE), ProcessExtensionModel.class).getExtensions(
            PROCESS_DEFINITION_KEY
        );

        given(execution.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(processExtensionService.getExtensionsForId(PROCESS_DEFINITION_ID)).willReturn(extension);

        variablesMappingProvider = new ExtensionsVariablesMappingProvider(
            processExtensionService,
            new ExpressionResolver(new ExpressionManager(), JSON_MAPPER, new DefaultDelegateInterceptor()),
            new VariableParsingService(
                Map.of(
                    "json",
                    new JsonObjectVariableType(JSON_MAPPER),
                    "array",
                    new JsonObjectVariableType(JSON_MAPPER)
                )
            ),
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
    void should_keepJsonVariableAsArray_when_jsonVariableIsInitializedAsEmptyArray() {
        Object result = simulateParallelIndexedWrites("writeJsonInitializedEmptyArray", "jsonInitializedEmptyArray");

        assertThat(result).isEqualTo(ARRAY_TO_LOOP_OVER);
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
    void should_throwJsonPatchMappingError_when_indexVariableIsNegative() {
        assertThatThrownBy(() ->
            runOutputMapping(
                "writeInitializedChildPropertyArray",
                "initializedChildPropertyArray",
                defaultValueOf("initializedChildPropertyArray"),
                -1,
                "X"
            )
        )
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage(JSON_PATCH_MAPPING_ERROR)
            .cause()
            .isInstanceOf(ActivitiIllegalArgumentException.class)
            .hasMessage("Invalid array index '-1' in JsonPatch path '/documents/-1': expected a non-negative integer");
    }

    private Object simulateParallelIndexedWrites(String activityId, String variableName, int... completionOrder) {
        int[] order = completionOrder.length > 0 ? completionOrder : new int[] { 0, 1, 2, 3, 4 };
        Object value = defaultValueOf(variableName);
        for (int index : order) {
            value = runOutputMapping(activityId, variableName, value, index, ARRAY_TO_LOOP_OVER.get(index));
        }
        return value;
    }

    private Object runOutputMapping(
        String activityId,
        String variableName,
        Object currentValue,
        int arrayIndex,
        String item
    ) {
        given(execution.getCurrentActivityId()).willReturn(activityId);
        given(execution.getVariable(variableName)).willReturn(currentValue);
        given(execution.getVariableInstance(INDEX_VARIABLE)).willReturn(indexVariableInstance);
        given(indexVariableInstance.getValue()).willReturn(arrayIndex);
        given(indexVariableInstance.getTypeName()).willReturn("integer");

        return variablesMappingProvider
            .calculateOutPutVariables(buildMappingExecutionContext(execution), Map.of("item", item))
            .get(variableName);
    }

    private Object defaultValueOf(String variableName) {
        return extension.getPropertyByName(variableName).getValue();
    }
}
