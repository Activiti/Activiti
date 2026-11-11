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
import java.util.Map;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.runtime.connector.Connector;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-history.properties")
class ProcessRuntimeJsonPatchArrayOutputMappingIT {

    @TestConfiguration
    static class ExampleConnectorConfiguration {

        @Bean(name = "exampleConnector.MAP")
        Connector exampleConnector() {
            return integrationContext -> {
                integrationContext.addOutBoundVariables(integrationContext.getInBoundVariables());
                return integrationContext;
            };
        }
    }

    private static final String PROCESS_DEFINITION_KEY = "Process_1790951452242";

    private static final List<String> ARRAY_TO_LOOP_OVER = List.of("ONE", "TWO", "THREE", "FOUR", "FIVE");

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    private String processInstanceId;

    @BeforeEach
    void setUp() {
        securityUtil.logInAs("user");
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(
            PROCESS_DEFINITION_KEY
        );
        processInstanceId = processInstance.getId();

        HistoricProcessInstance historicProcessInstance = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
        assertThat(historicProcessInstance.getEndTime()).isNotNull();
    }

    @AfterEach
    void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
        historyService.deleteHistoricProcessInstance(processInstanceId);
    }

    @Test
    void should_createChildArray_when_childPropertyIsUninitialized() {
        assertThat(finalValueOf("uninitializedChildPropertyArray")).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
    }

    @Test
    void should_fillChildArray_when_childPropertyIsInitializedAsEmptyArray() {
        assertThat(finalValueOf("initializedChildPropertyArray")).isEqualTo(Map.of("documents", ARRAY_TO_LOOP_OVER));
    }

    @Test
    void should_keepArrayVariableAsArray_when_arrayVariableIsUninitialized() {
        assertThat(finalValueOf("uninitializedArray")).isEqualTo(ARRAY_TO_LOOP_OVER);
    }

    @Test
    void should_keepArrayVariableAsArray_when_arrayVariableIsInitializedAsEmptyArray() {
        assertThat(finalValueOf("initializedArray")).isEqualTo(ARRAY_TO_LOOP_OVER);
    }

    @Test
    void should_overwriteElementsAtIndex_when_childArrayIsAlreadyPopulated() {
        assertThat(finalValueOf("alreadyPopulatedChildPropertyArray")).isEqualTo(
            Map.of("documents", ARRAY_TO_LOOP_OVER)
        );
    }

    @Test
    void should_overwriteElementsAtIndexAndKeepOthers_when_arrayVariableIsAlreadyPopulated() {
        assertThat(finalValueOf("alreadyPopulatedArray")).isEqualTo(
            List.of("ONE", "TWO", "THREE", "FOUR", "FIVE", "F")
        );
    }

    private Object finalValueOf(String variableName) {
        HistoricVariableInstance variable = historyService
            .createHistoricVariableInstanceQuery()
            .processInstanceId(processInstanceId)
            .variableName(variableName)
            .singleResult();
        assertThat(variable).as("historic variable %s", variableName).isNotNull();
        return JSON_MAPPER.convertValue(variable.getValue(), Object.class);
    }
}
