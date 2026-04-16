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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import tools.jackson.databind.json.JsonMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.ClaimTaskPayloadBuilder;
import org.activiti.api.task.model.builders.CompleteTaskPayloadBuilder;
import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = { "classpath:application.properties" })
public class ProcessRuntimeVariableMappingTest {

    private static final String VARIABLE_MAPPING_PROCESS = "connectorVarMapping";
    private static final String VARIABLE_MAPPING_EXPRESSION_PROCESS = "connectorVarMappingExpression";
    private static final String OUTPUT_MAPPING_EXPRESSION_VARIABLE_PROCESS = "outputMappingExpVar";
    private static final String OUTPUT_MAPPING_EXPRESSION_VALUE_PROCESS = "outputMappingExpValue";

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private JsonMapper mapper;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private TaskRuntime taskRuntime;

    @BeforeEach
    public void setUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldMapVariables() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            VARIABLE_MAPPING_PROCESS
        );

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        assertThat(variables)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(
                tuple("name", "outName"),
                tuple("age", 35),
                tuple("input_unmapped_variable_with_matching_name", "inTest"),
                tuple("input_unmapped_variable_with_non_matching_connector_input_name", "inTest"),
                tuple("nickName", "testName"),
                tuple("out_unmapped_variable_matching_name", "default"),
                tuple("output_unmapped_variable_with_non_matching_connector_output_name", "default")
            );
    }

    @Test
    public void should_resolveExpression_when_expressionIsInInputMappingValueOrInMappedProperty() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            VARIABLE_MAPPING_EXPRESSION_PROCESS
        );

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        String[] array = { "first", "John", "Doe", "last" };
        List<String> list = asList(array);

        Map<String, Object> data = new HashMap<>();
        data.put("age-in-months", 240);
        data.put("full-name", "John Doe");
        data.put("demoString", "expressionResolved");
        data.put("list", list);

        assertThat(variables)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(
                tuple("age", 30),
                tuple("name", "outName"),
                tuple("surname", "Doe"),
                tuple("data", data),
                tuple("user-msg", "Hello John Doe, today is your 20th birthday! It means 7305.0 days of life"),
                tuple("input-unmapped-variable-with-matching-name", "Doe"),
                tuple("input-unmapped-variable-with-non-matching-connector-input-name", "inTestExpression"),
                tuple("variableToResolve", "John"),
                tuple("out-unmapped-variable-matching-name", "defaultExpression"),
                tuple("output-unmapped-variable-with-non-matching-connector-output-name", "defaultExpression"),
                tuple("resident", true)
            );
    }

    @Test
    public void should_throwActivitiException_when_expressionIsInOutputMapping() {
        Throwable throwable = catchThrowable(() ->
            processBaseRuntime.startProcessWithProcessDefinitionKey(OUTPUT_MAPPING_EXPRESSION_VARIABLE_PROCESS)
        );

        assertThat(throwable)
            .isInstanceOf(ActivitiException.class)
            .hasMessageContaining("Expressions are not allowed as variable values in the output mapping");
    }

    @Test
    public void should_resolveExpression_when_expressionIsInOutputMappingValueOrInMappedProperty() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            OUTPUT_MAPPING_EXPRESSION_VALUE_PROCESS
        );

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        assertThat(variables)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .containsOnly(
                tuple("name", "John"),
                tuple("outVar", "Resolved expression: value-set-in-connector"),
                tuple("outVarFromJsonExpression", "Tower of London")
            );
    }

    @Test
    public void should_map_output_variables_from_call_activity_to_output_collection_for_multi_instances() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            "multi-instance-call-activity-result-collection-all"
        );

        List<VariableInstance> procVariables = processBaseRuntime.getProcessVariablesByProcessId(
            processInstance.getId()
        );

        assertThat(procVariables)
            .isNotNull()
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .contains(
                tuple(
                    "miResult",
                    asList(Map.of("childVar", "From child 1"), Map.of("childVar", "From child 0"))
                )
            );

        final var task = taskBaseRuntime.getTasks(processInstance).getFirst();

        securityUtil.logInAs("user");

        taskRuntime.claim(new ClaimTaskPayloadBuilder().withTaskId(task.getId()).withAssignee("user").build());

        taskRuntime.complete(new CompleteTaskPayloadBuilder().withTaskId(task.getId()).build());

        assertThatThrownBy(() -> processBaseRuntime.getProcessRuntime().processInstance(processInstance.getId())).isInstanceOf(NotFoundException.class);
    }

}
