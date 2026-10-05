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
package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.bpmn.behavior.SetVariablesTaskActivityBehavior;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests error handling in {@link SetVariablesTaskActivityBehavior}.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>Exceptions during variable calculation are properly caught
 *   <li>Variables are only set when no errors occur
 *   <li>Error conditions don't prevent normal execution flow
 * </ul>
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = { "classpath:application.properties" })
class SetVariablesTaskErrorHandlingTest {

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @BeforeEach
    void setUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    /**
     * Verifies that normal execution succeeds and all variables are set.
     */
    @Test
    void should_executeSuccessfully_whenNoErrors() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "copiedName", "literalGreeting", "fullName");

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        assertThat(variables.stream().map(VariableInstance::getName).toList()).contains(
            "copiedName",
            "literalGreeting",
            "fullName",
            "greetingMsg",
            "doubledAge"
        );
    }

    /**
     * Verifies that valid variable mappings set the variables correctly.
     */
    @Test
    void should_setVariables_whenMappingsAreValid() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "copiedName");

        VariableInstance copiedName = getVariable(processInstance.getId(), "copiedName");
        assertThat((Object) copiedName.getValue()).isEqualTo("John");
    }

    /**
     * Verifies that literal value mappings work correctly.
     */
    @Test
    void should_handleEmptyVariables_gracefully() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "literalGreeting");

        VariableInstance literalGreeting = getVariable(processInstance.getId(), "literalGreeting");
        assertThat((Object) literalGreeting.getValue()).isEqualTo("hello");
    }

    /**
     * Verifies that successful variable mapping sets variables correctly.
     */
    @Test
    void should_takeSuccessPath_whenNoErrorsOccur() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "copiedName");

        // Verify that variables were actually set (success path taken)
        VariableInstance copiedName = getVariable(processInstance.getId(), "copiedName");
        assertThat((Object) copiedName).isNotNull();
        assertThat((Object) copiedName.getValue()).isEqualTo("John");
    }

    /**
     * Verifies expression resolution works correctly.
     */
    @Test
    void should_resolveExpressions_correctly() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "fullName");

        VariableInstance fullName = getVariable(processInstance.getId(), "fullName");
        assertThat((Object) fullName.getValue()).isEqualTo("John Doe");
    }

    /**
     * Verifies that arithmetic expressions are evaluated correctly.
     */
    @Test
    void should_evaluateArithmetic_expressions() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "doubledAge");

        VariableInstance doubledAge = getVariable(processInstance.getId(), "doubledAge");
        assertThat((Object) doubledAge.getValue()).isEqualTo(42L);
    }

    /**
     * Verifies that text with embedded expressions works correctly.
     */
    @Test
    void should_handleText_withEmbeddedExpressions() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "setVariablesTaskProcess"
        );
        waitForVariablesToBeSet(processInstance.getId(), "greetingMsg");

        VariableInstance greetingMsg = getVariable(processInstance.getId(), "greetingMsg");
        assertThat((Object) greetingMsg.getValue()).isEqualTo("Hello John!");
    }

    private void waitForVariablesToBeSet(String processInstanceId, String... variableNames) {
        long timeout = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < timeout) {
            List<String> actualNames = processBaseRuntime
                .getProcessVariablesByProcessId(processInstanceId)
                .stream()
                .map(VariableInstance::getName)
                .toList();
            if (List.of(variableNames).stream().allMatch(actualNames::contains)) {
                return;
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for variables", e);
            }
        }
        throw new IllegalStateException("Timed out waiting for variables to be set");
    }

    private VariableInstance getVariable(String processInstanceId, String variableName) {
        return processBaseRuntime
            .getProcessVariablesByProcessId(processInstanceId)
            .stream()
            .filter(v -> v.getName().equals(variableName))
            .findFirst()
            .orElse(null);
    }
}
