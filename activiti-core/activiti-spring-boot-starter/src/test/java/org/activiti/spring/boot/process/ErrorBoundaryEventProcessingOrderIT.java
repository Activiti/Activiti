/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.GetTasksPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.process.listener.DummyBPMNErrorReceivedListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for validating that BPMN error boundary events with specific error codes
 * are processed before catch-all boundary events (those with no error code).
 *
 * This test class validates the fix in ErrorPropagation.java that ensures proper processing
 * order of error boundary events.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ErrorBoundaryEventProcessingOrderIT {

    private static final String ERROR_BOUNDARY_EVENT_PROCESSING_ORDER = "errorBoundaryEventProcessingOrder";
    private static final String ERROR_BOUNDARY_EVENT_CATCH_ALL = "errorBoundaryEventCatchAll";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private DummyBPMNErrorReceivedListener listener;

    @BeforeEach
    public void init() {
        listener.clear();
    }

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
        listener.clear();
    }

    @Test
    public void should_ProcessSpecificErrorHandler_NotCatchAll_When_SpecificErrorCodeMatches() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_PROCESSING_ORDER).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that ERROR_CODE_1 is caught by the specific handler (catchError1), not the catch-all
        checkProcessAndTask(processInstance.getId(), "Task 1");

        // Validate error events received
        assertThat(listener.getErrorReceivedEvents())
            .isNotEmpty()
            .hasSize(1)
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("catchError1", "ERROR_CODE_1")
            );

        // Verify that the specific error handler was triggered, not the catch-all
        assertThat(listener.getErrorReceivedEvents())
            .extracting(event -> event.getEntity().getElementId())
            .doesNotContain("catchErrorAny")
            .contains("catchError1");

        // Additional validation: error code should not be null (proving it's not the catch-all)
        assertThat(listener.getErrorReceivedEvents())
            .allSatisfy(event -> {
                assertThat(event.getEntity().getErrorCode())
                    .as("Error code should not be null for specific error handlers")
                    .isNotNull()
                    .isEqualTo("ERROR_CODE_1");
            });

        // Verify the catch-all handler was NOT executed
        assertThat(listener.getErrorReceivedEvents())
            .noneMatch(event -> event.getEntity().getElementId().equals("catchErrorAny"));
    }

    @Test
    public void should_ExecuteCatchAllBoundaryEvent_When_NoSpecificErrorHandlerMatches() {
        securityUtil.logInAs("user");

        // This process throws UNHANDLED_ERROR which has no specific handler
        // Only the catch-all boundary event (with no error code) should catch it
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_CATCH_ALL).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that the catch-all handler caught the unhandled error
        checkProcessAndTask(processInstance.getId(), "Task Any");

        // Validate that only the catch-all error event was received
        assertThat(listener.getErrorReceivedEvents())
            .isNotEmpty()
            .hasSize(1)
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("catchErrorAny", "UNHANDLED_ERROR")
            );

        // Verify that the catch-all handler was executed
        assertThat(listener.getErrorReceivedEvents())
            .extracting(event -> event.getEntity().getElementId())
            .contains("catchErrorAny")
            .doesNotContain("catchError1");

        // Verify the specific handler (catchError1) was NOT executed
        assertThat(listener.getErrorReceivedEvents())
            .noneMatch(event -> event.getEntity().getElementId().equals("catchError1"));
    }

    @Test
    public void should_PrioritizeSpecificErrorHandlers_Over_CatchAllHandler() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_PROCESSING_ORDER).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that only one error handler was triggered (the specific one)
        assertThat(listener.getErrorReceivedEvents())
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.getEntity().getElementId())
                    .as("Only the specific error handler should be triggered")
                    .isEqualTo("catchError1");
                assertThat(event.getEntity().getErrorCode())
                    .as("Error code should match the specific handler")
                    .isEqualTo("ERROR_CODE_1");
            });
    }

    @Test
    public void should_NotExecuteMultipleBoundaryEvents_When_OneMatchesError() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_PROCESSING_ORDER).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that ONLY ONE boundary event is executed (not multiple)
        assertThat(listener.getErrorReceivedEvents())
            .as("Only one boundary event should be executed per error")
            .hasSize(1);

        // Verify it's the correct specific handler
        assertThat(listener.getErrorReceivedEvents())
            .extracting(event -> event.getEntity().getElementId())
            .containsOnly("catchError1");
    }

    private void checkProcessAndTask(String processInstanceId, String taskName) {
        ProcessInstance processInstance = processRuntime.processInstance(processInstanceId);
        assertThat(processInstance).isNotNull();

        checkTask(processInstanceId, taskName);
    }

    private void checkTask(String processInstanceId, String taskName) {
        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder().withProcessInstanceId(processInstanceId).build();

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50), getTasksPayload);

        assertThat(tasks.getContent()).hasSize(1);
        assertThat(tasks.getContent().get(0).getName()).isEqualTo(taskName);
    }
}
