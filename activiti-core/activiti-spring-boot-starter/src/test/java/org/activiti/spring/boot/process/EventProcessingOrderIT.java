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
 * Integration tests for validating that BPMN error handlers (boundary events and event subprocesses)
 * with specific error codes are processed before catch-all handlers (those with no error code).
 * This test class validates the fix in ErrorPropagation.java that ensures proper processing
 * order of error boundary events and error event subprocesses.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EventProcessingOrderIT {

    private static final String ERROR_BOUNDARY_EVENT_PROCESSING_ORDER = "errorBoundaryEventProcessingOrder";
    private static final String ERROR_BOUNDARY_EVENT_CATCH_ALL = "errorBoundaryEventCatchAll";
    private static final String ERROR_EVENT_SUBPROCESS_PROCESSING_ORDER = "errorEventSubprocessProcessingOrder";
    private static final String ERROR_EVENT_SUBPROCESS_CATCH_ALL = "errorEventSubprocessCatchAll";

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
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("catchError1", "ERROR_CODE_1")
            );
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
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("catchErrorAny", "UNHANDLED_ERROR")
            );
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
    public void should_ProcessSpecificEventSubprocessHandler_NotCatchAll_When_SpecificErrorCodeMatches() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_EVENT_SUBPROCESS_PROCESSING_ORDER).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that ERROR_CODE_1 is caught by the specific event subprocess handler, not the catch-all
        checkProcessAndTask(processInstance.getId(), "Event Subprocess Task 1");

        // Validate error events received
        assertThat(listener.getErrorReceivedEvents())
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("eventSubprocessError1Start", "ERROR_CODE_1")
            );
    }

    @Test
    public void should_ExecuteCatchAllEventSubprocess_When_NoSpecificEventSubprocessHandlerMatches() {
        securityUtil.logInAs("user");

        // This process throws UNHANDLED_ERROR which has no specific event subprocess handler
        // Only the catch-all event subprocess (with no error code) should catch it
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_EVENT_SUBPROCESS_CATCH_ALL).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that the catch-all event subprocess handler caught the unhandled error
        checkProcessAndTask(processInstance.getId(), "Event Subprocess Task Any");

        // Validate that only the catch-all error event was received
        assertThat(listener.getErrorReceivedEvents())
            .extracting(
                event -> event.getEntity().getElementId(),
                event -> event.getEntity().getErrorCode()
            )
            .containsExactly(
                Tuple.tuple("eventSubprocessCatchAllStart", "UNHANDLED_ERROR")
            );
    }

    @Test
    public void should_PrioritizeSpecificEventSubprocessHandlers_Over_CatchAllHandler() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_EVENT_SUBPROCESS_PROCESSING_ORDER).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify that only one event subprocess handler was triggered (the specific one)
        assertThat(listener.getErrorReceivedEvents())
            .hasSize(1)
            .first()
            .satisfies(event -> {
                assertThat(event.getEntity().getElementId())
                    .as("Only the specific event subprocess handler should be triggered")
                    .isEqualTo("eventSubprocessError1Start");
                assertThat(event.getEntity().getErrorCode())
                    .as("Error code should match the specific event subprocess handler")
                    .isEqualTo("ERROR_CODE_1");
            });
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
        assertThat(tasks.getContent().getFirst().getName()).isEqualTo(taskName);
    }
}
