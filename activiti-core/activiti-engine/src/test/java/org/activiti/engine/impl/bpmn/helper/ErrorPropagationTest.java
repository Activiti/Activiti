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

import static org.activiti.engine.impl.bpmn.helper.ErrorPropagation.ERROR_CONTEXT_MAPPING_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Error;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.impl.bpmn.behavior.VariablesPropagator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.AfterEach;
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

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "404", null);

            assertThat(payload)
                .extracting(
                    ErrorContext::getErrorCode,
                    ErrorContext::getErrorId,
                    ErrorContext::getErrorName,
                    ErrorContext::getAttachedToRef
                )
                .containsExactly("404", "errorId", "NOT_FOUND", null);
        }

        @Test
        void should_buildPayload_withFallbackName() {
            BpmnModel bpmnModel = new BpmnModel();

            Error error = new Error("REQUEST_TIMEOUT");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "REQUEST_TIMEOUT", null);

            assertThat(payload)
                .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorId, ErrorContext::getErrorName)
                .containsExactly("REQUEST_TIMEOUT", "REQUEST_TIMEOUT", "REQUEST_TIMEOUT");
        }

        @Test
        void should_buildPayload_withoutName_whenBpmnModelIsNull() {
            Error error = new Error("errorId");

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, null, "404", null);

            assertThat(payload)
                .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorId, ErrorContext::getErrorName)
                .containsExactly("404", "errorId", null);
        }

        @Test
        void should_buildPayload_withConnectorErrorFallback() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("connector.error_1", "Service Unavailable", "503");

            Error error = new Error("503"); // connector passes error code as id

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "503", null);

            assertThat(payload)
                .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorId, ErrorContext::getErrorName)
                .containsExactly("503", "503", "Service Unavailable");
        }

        @Test
        void should_setAttachedToRef_when_eventIsBoundaryEvent() {
            BpmnModel bpmnModel = new BpmnModel();
            bpmnModel.addError("errorId", "NOT_FOUND", "404");

            Error error = new Error("errorId");

            ServiceTask attachedActivity = new ServiceTask();
            attachedActivity.setId("Activity_1w8vukd");
            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setAttachedToRef(attachedActivity);

            ErrorContext payload = ErrorPropagation.buildErrorContext(error, bpmnModel, "404", boundaryEvent);

            assertThat(payload)
                .extracting(
                    ErrorContext::getErrorCode,
                    ErrorContext::getErrorId,
                    ErrorContext::getErrorName,
                    ErrorContext::getAttachedToRef
                )
                .containsExactly("404", "errorId", "NOT_FOUND", "Activity_1w8vukd");
        }
    }

    @Nested
    class ResolveAttachedToRef {

        @Test
        void should_returnAttachedActivityId_when_boundaryEventIsAttached() {
            ServiceTask attachedActivity = new ServiceTask();
            attachedActivity.setId("serviceTask-1");
            BoundaryEvent boundaryEvent = new BoundaryEvent();
            boundaryEvent.setAttachedToRef(attachedActivity);

            assertThat(ErrorPropagation.resolveAttachedToRef(boundaryEvent)).isEqualTo("serviceTask-1");
        }

        @Test
        void should_returnNull_when_boundaryEventHasNoAttachedActivity() {
            assertThat(ErrorPropagation.resolveAttachedToRef(new BoundaryEvent())).isNull();
        }

        @Test
        void should_returnNull_when_eventIsNotBoundaryEvent() {
            assertThat(ErrorPropagation.resolveAttachedToRef(new StartEvent())).isNull();
        }
    }

    @Nested
    class DefaultErrorVariablesPropagator {

        @Test
        void should_haveDefaultPropagator_onPlainEngine() {
            ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();

            assertThat(configuration.getErrorVariablesPropagator()).isNotNull();
        }

        @Test
        void should_returnConfiguredPropagator_when_set() {
            ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();
            VariablesPropagator propagator = mock(VariablesPropagator.class);

            configuration.setErrorVariablesPropagator(propagator);

            assertThat(configuration.getErrorVariablesPropagator()).isSameAs(propagator);
        }
    }

    @Nested
    class PropagateErrorVariables {

        private final ErrorContext errorContext = new ErrorContext("errorId", "404", "NOT_FOUND", null);

        @AfterEach
        void tearDown() {
            // removeProcessEngineConfiguration() pops unconditionally, so only pop when something was pushed
            if (Context.getProcessEngineConfiguration() != null) {
                Context.removeProcessEngineConfiguration();
            }
        }

        private VariablesPropagator givenConfiguredPropagator() {
            ProcessEngineConfigurationImpl configuration = mock(ProcessEngineConfigurationImpl.class);
            VariablesPropagator propagator = mock(VariablesPropagator.class);
            when(configuration.getErrorVariablesPropagator()).thenReturn(propagator);
            Context.setProcessEngineConfiguration(configuration);
            return propagator;
        }

        @Test
        void should_propagateErrorContextToProcessInstance_underErrorKey() {
            VariablesPropagator propagator = givenConfiguredPropagator();
            ExecutionEntity catchingExecution = mock(ExecutionEntity.class);

            ErrorPropagation.propagateErrorVariables(catchingExecution, errorContext);

            verify(propagator).propagateToProcessInstance(
                catchingExecution,
                Map.of(ERROR_CONTEXT_MAPPING_KEY, errorContext)
            );
            verify(propagator, never()).propagate(any(), any(Map.class));
        }

        @Test
        void should_doNothing_when_catchingExecutionIsNull() {
            VariablesPropagator propagator = givenConfiguredPropagator();

            ErrorPropagation.propagateErrorVariables(null, errorContext);

            verifyNoInteractions(propagator);
        }

        @Test
        void should_notFail_when_propagatorIsNull() {
            ProcessEngineConfigurationImpl configuration = mock(ProcessEngineConfigurationImpl.class);
            when(configuration.getErrorVariablesPropagator()).thenReturn(null);
            Context.setProcessEngineConfiguration(configuration);

            assertThatCode(() ->
                ErrorPropagation.propagateErrorVariables(mock(ExecutionEntity.class), errorContext)
            ).doesNotThrowAnyException();
        }

        @Test
        void should_notFail_when_noProcessEngineConfigurationInContext() {
            assertThatCode(() ->
                ErrorPropagation.propagateErrorVariables(mock(ExecutionEntity.class), errorContext)
            ).doesNotThrowAnyException();
        }
    }
}
