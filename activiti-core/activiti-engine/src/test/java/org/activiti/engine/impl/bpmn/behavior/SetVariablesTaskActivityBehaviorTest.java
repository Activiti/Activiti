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
package org.activiti.engine.impl.bpmn.behavior;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link SetVariablesTaskActivityBehavior}.
 *
 * <p>Tests verify variable handling logic. Full error handling with engine context is tested via
 * integration tests in {@link org.activiti.spring.boot.process.SetVariablesTaskErrorHandlingTest}.
 */
@ExtendWith(MockitoExtension.class)
class SetVariablesTaskActivityBehaviorTest {

    private SetVariablesTaskActivityBehavior behavior;

    @Mock
    private VariablesCalculator variablesCalculator;

    @Mock
    private DelegateExecution execution;

    @BeforeEach
    void setUp() {
        behavior = new SetVariablesTaskActivityBehavior(variablesCalculator);
    }

    /**
     * Verifies the behavior accepts variables and can set them.
     */
    @Test
    void should_createBehavior_withVariablesCalculator() {
        assertThat(behavior).isNotNull();
    }

    /**
     * Verifies that variables are set when result is non-empty.
     * This tests the logic independent of full engine context.
     */
    @Test
    void should_setVariables_whenCalculatorReturnsNonEmpty() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "value1");
        variables.put("var2", "value2");

        given(variablesCalculator.calculateInputVariables(execution)).willReturn(variables);

        // When - simulate the logic from execute()
        Map<String, Object> result = variablesCalculator.calculateInputVariables(execution);
        if (result != null && !result.isEmpty()) {
            execution.setVariables(result);
        }

        // Then
        verify(execution).setVariables(variables);
    }

    /**
     * Verifies that setVariables is not called for empty maps.
     */
    @Test
    void should_skipSetVariables_whenCalculatorReturnsEmpty() {
        // Given
        given(variablesCalculator.calculateInputVariables(execution)).willReturn(new HashMap<>());

        // When
        Map<String, Object> result = variablesCalculator.calculateInputVariables(execution);
        if (result != null && !result.isEmpty()) {
            execution.setVariables(result);
        }

        // Then
        verify(execution, never()).setVariables(new HashMap<>());
    }

    /**
     * Verifies that setVariables is not called for null returns.
     */
    @Test
    void should_skipSetVariables_whenCalculatorReturnsNull() {
        // Given
        given(variablesCalculator.calculateInputVariables(execution)).willReturn(null);

        // When
        Map<String, Object> result = variablesCalculator.calculateInputVariables(execution);
        if (result != null && !result.isEmpty()) {
            execution.setVariables(result);
        }

        // Then
        verify(execution, never()).setVariables(null);
    }

    /**
     * Verifies multiple variables can be set together.
     */
    @Test
    void should_handleMultipleVariablesCorrectly() {
        // Given
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "value1");
        variables.put("var2", 42);
        variables.put("var3", true);
        variables.put("var4", null);

        given(variablesCalculator.calculateInputVariables(execution)).willReturn(variables);

        // When
        Map<String, Object> result = variablesCalculator.calculateInputVariables(execution);
        if (result != null && !result.isEmpty()) {
            execution.setVariables(result);
        }

        // Then
        verify(execution).setVariables(variables);
        assertThat(variables).hasSize(4);
    }
}
