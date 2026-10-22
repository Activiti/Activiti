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
import static org.mockito.ArgumentMatchers.any;
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
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        void should_returnNull_when_errorCodeIsNullAndNoMatch() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("unknownId");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, null);

            assertThat(result).isNull();
        }

        @Test
        void should_returnNull_when_errorCodeIsNullAndModelHasErrors() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("someId", "SOME_ERROR", "500");

            Error error = new Error("unknownId");

            String result = ErrorPropagation.resolveErrorName(error, bpmnModel, null);

            assertThat(result).isNull();
        }

        /**
         * Cross-model ID reuse: the called process and the catching process both
         * define an error with id "sharedId" but for different codes. The direct
         * lookup must reject the ID match and fall back to the code-based search.
         */
        @Test
        void should_resolveByCode_when_idMatchesButErrorCodeDiffers() {
            // Catching model: id "sharedId" maps to code "500"
            BpmnModel catchingModel = new BpmnModel();
            catchingModel.addError("sharedId", "INTERNAL_ERROR", "500");
            catchingModel.addError("otherErrorId", "NOT_FOUND", "404");

            // Error from called model: same id "sharedId" but code "404"
            Error error = new Error("sharedId", "404");

            String result = ErrorPropagation.resolveErrorName(error, catchingModel, "404");

            // Must resolve to "NOT_FOUND" (code 404), not "INTERNAL_ERROR" (id "sharedId")
            assertThat(result).isEqualTo("NOT_FOUND");
        }
    }

    @Nested
    class BuildErrorContext {

        @Test
        void should_buildPayload_withAllFields() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("errorId", "NOT_FOUND", "404");

            Error error = new Error("errorId");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "404");

            assertThat(payload.getErrorCode()).isEqualTo("404");
            assertThat(payload.getErrorId()).isEqualTo("errorId");
            assertThat(payload.getErrorName()).isEqualTo("NOT_FOUND");
            assertThat(payload.getSourceId()).isNull();
        }

        @Test
        void should_buildPayload_withFallbackName() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("REQUEST_TIMEOUT");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "REQUEST_TIMEOUT");

            assertThat(payload.getErrorCode()).isEqualTo("REQUEST_TIMEOUT");
            assertThat(payload.getErrorId()).isEqualTo("REQUEST_TIMEOUT");
            assertThat(payload.getErrorName()).isEqualTo("REQUEST_TIMEOUT");
        }

        @Test
        void should_buildPayload_withoutName_whenBpmnModelIsNull() {
            Error error = new Error("errorId");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, null, "404");

            assertThat(payload.getErrorCode()).isEqualTo("404");
            assertThat(payload.getErrorId()).isEqualTo("errorId");
            assertThat(payload.getErrorName()).isNull();
        }

        @Test
        void should_buildPayload_withConnectorErrorFallback() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("connector.error_1", "Service Unavailable", "503");

            Error error = new Error("503"); // connector passes error code as id

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "503");

            assertThat(payload.getErrorCode()).isEqualTo("503");
            assertThat(payload.getErrorId()).isEqualTo("503");
            assertThat(payload.getErrorName()).isEqualTo("Service Unavailable");
        }

        @Test
        void should_includeSourceId_whenSet() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("errorId", "NOT_FOUND", "404");

            Error error = new Error("errorId");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "404");
            payload.setSourceId("Activity_1w8vukd");

            assertThat(payload.getErrorCode()).isEqualTo("404");
            assertThat(payload.getErrorId()).isEqualTo("errorId");
            assertThat(payload.getErrorName()).isEqualTo("NOT_FOUND");
            assertThat(payload.getSourceId()).isEqualTo("Activity_1w8vukd");
        }
    }

    @Nested
    class ApplyErrorOutputMappings {

        private ErrorContextMappingProvider provider;
        private ExecutionEntityManager executionEntityManager;

        @BeforeEach
        void setUp() {
            ProcessEngineConfigurationImpl processEngineConfiguration = mock(ProcessEngineConfigurationImpl.class);
            CommandContext commandContext = mock(CommandContext.class);
            executionEntityManager = mock(ExecutionEntityManager.class);
            provider = mock(ErrorContextMappingProvider.class);

            when(processEngineConfiguration.getErrorContextMappingProvider()).thenReturn(provider);
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

            ServiceTask attachedActivity = new ServiceTask();
            attachedActivity.setId("serviceTask-1");

            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setId("boundary-1");
            boundaryEvent.setAttachedToRef(attachedActivity);

            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("error1", "NOT_FOUND", "404");
            Error error = new Error("error1");

            Map<String, Object> mappedVars = Map.of("boundaryErrorCode", "404", "boundaryErrorName", "NOT_FOUND");
            when(provider.apply(anyMap(), any(DelegateExecution.class), eq("boundary-1"))).thenReturn(mappedVars);

            ErrorPropagation.applyErrorOutputMappings(boundaryEvent, parentExecution, error, bpmnModel, "404");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
            verify(provider).apply(payloadCaptor.capture(), eq(processInstanceExecution), eq("boundary-1"));

            ErrorContext capturedContext = (ErrorContext) payloadCaptor.getValue().get("error");
            assertThat(capturedContext.getSourceId()).isEqualTo("serviceTask-1");

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
            when(provider.apply(anyMap(), any(DelegateExecution.class), eq("start-1"))).thenReturn(mappedVars);

            ErrorPropagation.applyErrorOutputMappings(startEvent, parentExecution, error, bpmnModel, "503");

            verify(provider).apply(anyMap(), eq(parentExecution), eq("start-1"));
            verify(parentExecution).setVariables(mappedVars);
        }

        @Test
        void should_notSetVariables_when_providerReturnsEmptyMap() {
            ExecutionEntity parentExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessDefinitionId()).thenReturn("proc-def-3");

            ExecutionEntity processInstanceExecution = mock(ExecutionEntity.class);
            when(parentExecution.getProcessInstance()).thenReturn(processInstanceExecution);

            ServiceTask attachedActivity = new ServiceTask();
            attachedActivity.setId("serviceTask-2");

            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setId("boundary-2");
            boundaryEvent.setAttachedToRef(attachedActivity);

            BpmnModel bpmnModel = new BpmnModel();
            Error error = new Error("error3");

            when(provider.apply(anyMap(), any(DelegateExecution.class), anyString())).thenReturn(Map.of());

            ErrorPropagation.applyErrorOutputMappings(boundaryEvent, parentExecution, error, bpmnModel, "500");

            verify(processInstanceExecution, never()).setVariables(anyMap());
            verify(parentExecution, never()).setVariables(anyMap());
        }
    }
}
