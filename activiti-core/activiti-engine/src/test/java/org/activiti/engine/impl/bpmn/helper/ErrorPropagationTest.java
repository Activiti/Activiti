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
package org.activiti.engine.impl.bpmn.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ErrorPropagationTest {

    @Nested
    class ResolveErrorName {

        @Test
        void should_returnName_when_directLookupByErrorIdFindsMatch() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("errorId", "NOT_FOUND", "404");

            Error error = new Error("errorId");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, "404");

            assertThat(result).isEqualTo("NOT_FOUND");
        }

        @Test
        void should_returnName_when_fallbackByErrorCodeFindsMatch() {
            // Simulates connector errors where error.getId() is the error code,
            // not the BPMN error id
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("bpmnErrorId", "UNKNOWN_ERROR", "UNKNOWN_ERROR");

            Error error = new Error("UNKNOWN_ERROR"); // id is the error code, not BPMN id

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, "UNKNOWN_ERROR");

            // Direct lookup by "UNKNOWN_ERROR" as id fails (BPMN id is "bpmnErrorId"),
            // but fallback by error code matches
            assertThat(result).isEqualTo("UNKNOWN_ERROR");
        }

        @Test
        void should_returnErrorCode_when_noMatchFoundInBpmnModel() {
            // Error not defined in BPMN at all (e.g., unexpected connector error)
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("REQUEST_TIMEOUT");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, "REQUEST_TIMEOUT");

            assertThat(result).isEqualTo("REQUEST_TIMEOUT");
        }

        @Test
        void should_returnErrorCode_when_errorsMapIsEmpty() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("someError");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, "503");

            assertThat(result).isEqualTo("503");
        }

        @Test
        void should_returnNull_when_errorCodeIsNullAndNoMatch() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("unknownId");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, null);

            assertThat(result).isNull();
        }
    }

    @Nested
    class BuildErrorPayload {

        @Test
        void should_buildPayload_withAllFields() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("errorId", "NOT_FOUND", "404");

            Error error = new Error("errorId");

            Map<String, Object> payload = ErrorPropagation.buildErrorPayload(error, bpmnModel, "404");

            assertThat(payload).contains(entry("errorCode", "404"), entry("errorName", "NOT_FOUND"));
        }

        @Test
        void should_buildPayload_withFallbackName() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("REQUEST_TIMEOUT");

            Map<String, Object> payload = ErrorPropagation.buildErrorPayload(error, bpmnModel, "REQUEST_TIMEOUT");

            assertThat(payload).contains(entry("errorCode", "REQUEST_TIMEOUT"), entry("errorName", "REQUEST_TIMEOUT"));
        }

        @Test
        void should_buildPayload_withoutName_whenBpmnModelIsNull() {
            Error error = new Error("errorId");

            Map<String, Object> payload = ErrorPropagation.buildErrorPayload(error, null, "404");

            assertThat(payload).contains(entry("errorCode", "404")).doesNotContainKey("errorName");
        }

        @Test
        void should_buildPayload_withConnectorErrorFallback() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("connector.error_1", "Service Unavailable", "503");

            Error error = new Error("503"); // connector passes error code as id

            Map<String, Object> payload = ErrorPropagation.buildErrorPayload(error, bpmnModel, "503");

            assertThat(payload).contains(entry("errorCode", "503"), entry("errorName", "Service Unavailable"));
        }
    }

    @Nested
    class ApplyErrorOutputMappings {

        private ErrorPayloadMappingProvider provider;
        private ExecutionEntityManager executionEntityManager;

        @BeforeEach
        void setUp() {
            ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
            CommandContext commandContext = mock(CommandContext.class);
            executionEntityManager = mock(ExecutionEntityManager.class);
            provider = mock(ErrorPayloadMappingProvider.class);

            when(processEngineConfiguration.getErrorPayloadMappingProvider()).thenReturn(provider);
            when(commandContext.getExecutionEntityManager()).thenReturn(executionEntityManager);

            Context.setProcessEngineConfiguration(processEngineConfiguration);
            Context.setCommandContext(commandContext);
        }

        @AfterEach
        void tearDown() {
            Context.removeProcessEngineConfiguration();
            Context.removeCommandContext();
        }

        /**
         * Regression: a subprocess-local variable shadowing the mapped target name
         * must not intercept the write. Variables go to the process-instance execution,
         * not to the interrupted activity execution that BoundaryEventActivityBehavior
         * will destroy.
         */
        @Test
        void should_writeToProcessInstance_when_boundaryEventCatches() {
            ExecutionEntity parentExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessDefinitionId()).thenReturn("proc-def-1");

            ExecutionEntity processInstanceExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessInstance()).thenReturn(processInstanceExecution);

            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setId("boundary-1");

            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("error1", "NOT_FOUND", "404");
            Error error = new Error("error1");

            Map<String, Object> mappedVars = Map.of("boundaryErrorCode", "404", "boundaryErrorName", "NOT_FOUND");
            when(provider.apply(anyMap(), eq("proc-def-1"), eq("boundary-1"))).thenReturn(mappedVars);

            ErrorPropagation.applyErrorOutputMappings(boundaryEvent, parentExecution, error, bpmnModel, "404");

            verify(processInstanceExecution).setVariables(mappedVars);
            verify(parentExecution, never()).setVariables(anyMap());
        }

        @Test
        void should_writeToParentExecution_when_startEventErrorSubprocess() {
            ExecutionEntity parentExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessDefinitionId()).thenReturn("proc-def-2");

            StartEvent startEvent = new StartEvent();
            startEvent.setId("start-1");

            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("error2", "SERVICE_UNAVAILABLE", "503");
            Error error = new Error("error2");

            Map<String, Object> mappedVars = Map.of("subProcessErrorCode", "503");
            when(provider.apply(anyMap(), eq("proc-def-2"), eq("start-1"))).thenReturn(mappedVars);

            ErrorPropagation.applyErrorOutputMappings(startEvent, parentExecution, error, bpmnModel, "503");

            verify(parentExecution).setVariables(mappedVars);
        }

        @Test
        void should_notSetVariables_when_providerReturnsEmptyMap() {
            ExecutionEntity parentExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessDefinitionId()).thenReturn("proc-def-3");

            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setId("boundary-2");

            BpmnModel bpmnModel = new BpmnModel();
            Error error = new Error("error3");

            when(provider.apply(anyMap(), anyString(), anyString())).thenReturn(Map.of());

            ErrorPropagation.applyErrorOutputMappings(boundaryEvent, parentExecution, error, bpmnModel, "500");

            verify(parentExecution, never()).setVariables(anyMap());
        }
    }
}
