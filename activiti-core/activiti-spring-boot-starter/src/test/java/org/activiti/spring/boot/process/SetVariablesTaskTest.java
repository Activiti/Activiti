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
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Verifies the built-in {@code setVariablesTask} service task: a
 * {@code <serviceTask implementation="setVariablesTask"/>} resolves its input mappings and writes
 * each resolved value to the process variable named by the mapping key (an already declared process
 * variable), synchronously - no connector and no messaging round-trip.
 *
 * <p>The fixture (set-variables-task-extensions.json) declares source variables
 * {@code firstName="John"}, {@code lastName="Doe"}, {@code age=21} and target variables set from
 * each supported input mapping type.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = { "classpath:application.properties" })
public class SetVariablesTaskTest {

    private static final String SET_VARIABLES_TASK_PROCESS = "setVariablesTaskProcess";

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @BeforeEach
    public void setUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_setVariable_fromAnotherProcessVariable() {
        assertThat(runAndGetVariables().get("copiedName")).isEqualTo("John");
    }

    @Test
    public void should_setVariable_fromLiteralValue() {
        assertThat(runAndGetVariables().get("literalGreeting")).isEqualTo("hello");
    }

    @Test
    public void should_setVariable_fromExpression_interpolatingProcessVariables() {
        assertThat(runAndGetVariables().get("fullName")).isEqualTo("John Doe");
    }

    @Test
    public void should_setVariable_fromExpression_withSurroundingText() {
        assertThat(runAndGetVariables().get("greetingMsg")).isEqualTo("Hello John!");
    }

    @Test
    public void should_setVariable_fromArithmeticExpression() {
        // Note: input mappings are set as-is without coercion to the declared variable type,
        // so EL integer arithmetic yields a Long.
        assertThat(runAndGetVariables().get("doubledAge")).isEqualTo(42L);
    }

    @Test
    public void should_setAllDeclaredTargetVariablesAndKeepSources() {
        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(
            processBaseRuntime.startProcessWithProcessDefinitionKey(SET_VARIABLES_TASK_PROCESS).getId()
        );

        assertThat(variables)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(
                // initial process properties (used as input sources)
                tuple("firstName", "John"),
                tuple("lastName", "Doe"),
                tuple("age", 21),
                // set by the setVariablesTask from its input mappings
                tuple("copiedName", "John"), // type variable -> value of firstName
                tuple("literalGreeting", "hello"), // type value -> literal
                tuple("fullName", "John Doe"), // expression: "${firstName} ${lastName}"
                tuple("greetingMsg", "Hello John!"), // expression with surrounding text
                tuple("doubledAge", 42L) // arithmetic expression "${age * 2}" -> Long (no type coercion)
            );
    }

    private Map<String, Object> runAndGetVariables() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            SET_VARIABLES_TASK_PROCESS
        );
        return processBaseRuntime
            .getProcessVariablesByProcessId(processInstance.getId())
            .stream()
            .collect(Collectors.toMap(VariableInstance::getName, VariableInstance::getValue));
    }
}
