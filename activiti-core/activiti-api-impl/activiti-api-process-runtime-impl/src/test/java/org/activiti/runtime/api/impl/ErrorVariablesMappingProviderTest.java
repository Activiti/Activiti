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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.bpmn.behavior.VariablesCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ErrorVariablesMappingProviderTest {

    @Mock
    private VariablesCalculator variablesCalculator;

    @InjectMocks
    private ErrorVariablesMappingProvider provider;

    @Test
    void should_DelegateToVariablesCalculator_WithCorrectContext() {
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("errorCode", "123");
        errorPayload.put("errorName", "myError");
        errorPayload.put("errorId", "errorId");

        Map<String, Object> expectedOutput = Map.of("boundaryErrorCode", "123");
        when(
            variablesCalculator.calculateOutPutVariables(any(MappingExecutionContext.class), eq(errorPayload))
        ).thenReturn(expectedOutput);

        Map<String, Object> result = provider.apply(errorPayload, "procDef1", "catchError");

        assertThat(result).isEqualTo(expectedOutput);

        ArgumentCaptor<MappingExecutionContext> contextCaptor = ArgumentCaptor.forClass(MappingExecutionContext.class);
        verify(variablesCalculator).calculateOutPutVariables(contextCaptor.capture(), eq(errorPayload));

        MappingExecutionContext capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getProcessDefinitionId()).isEqualTo("procDef1");
        assertThat(capturedContext.getActivityId()).isEqualTo("catchError");
    }

    @Test
    void should_ReturnEmptyMap_When_CalculatorReturnsEmpty() {
        Map<String, Object> errorPayload = Map.of("errorCode", "456");
        when(
            variablesCalculator.calculateOutPutVariables(any(MappingExecutionContext.class), eq(errorPayload))
        ).thenReturn(Map.of());

        Map<String, Object> result = provider.apply(errorPayload, "procDef1", "catchError");

        assertThat(result).isEmpty();
    }
}
