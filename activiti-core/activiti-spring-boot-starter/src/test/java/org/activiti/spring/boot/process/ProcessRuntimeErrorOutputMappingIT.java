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

import java.util.Map;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.GetTasksPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.bpmn.helper.ErrorContext;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProcessRuntimeErrorOutputMappingIT {

    private static final String ERROR_BOUNDARY_OUTPUT_MAPPING = "errorBoundaryEventOutputMapping";
    private static final String ERROR_BOUNDARY_CATCH_ALL_OUTPUT_MAPPING = "errorBoundaryCatchAllOutputMapping";
    private static final String ERROR_EVENT_SUBPROCESS_OUTPUT_MAPPING = "errorEventSubProcessOutputMapping";
    private static final String PARENT_PROCESS_ERROR_BOUNDARY = "parentProcessErrorBoundary";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @BeforeEach
    void setUp() {
        securityUtil.logInAs("user");
    }

    @AfterEach
    void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    void should_MapErrorObject_When_ErrorBoundaryEventCatches() {
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_OUTPUT_MAPPING).build()
        );

        assertThat(processInstance).isNotNull();

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).extracting(Task::getName).containsExactly("Task");

        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).containsKey("boundaryError");

        ErrorContext errorContext = (ErrorContext) variables.get("boundaryError");
        assertThat(errorContext)
            .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorName, ErrorContext::getSourceId)
            .containsExactly("404", "NOT_FOUND", "subProcess");
    }

    @Test
    void should_MapErrorObject_When_CatchAllBoundaryEventCatches() {
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_CATCH_ALL_OUTPUT_MAPPING).build()
        );

        assertThat(processInstance).isNotNull();

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).extracting(Task::getName).containsExactly("Catch All Task");

        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).containsKey("catchAllError");

        ErrorContext errorContext = (ErrorContext) variables.get("catchAllError");
        assertThat(errorContext)
            .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorName, ErrorContext::getSourceId)
            .containsExactly("KNOWN_ERROR", "Known Error", "subProcess");
    }

    @Test
    void should_MapErrorObject_When_ErrorEventSubProcessCatches() {
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_EVENT_SUBPROCESS_OUTPUT_MAPPING).build()
        );

        assertThat(processInstance).isNotNull();

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).extracting(Task::getName).containsExactly("Event Subprocess Task");

        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).containsKey("subProcessError");

        ErrorContext errorContext = (ErrorContext) variables.get("subProcessError");
        assertThat(errorContext)
            .extracting(ErrorContext::getErrorCode, ErrorContext::getErrorName, ErrorContext::getSourceId)
            .containsExactly("503", "SERVICE_UNAVAILABLE", null);
    }

    @Test
    void should_MapErrorObject_When_CallActivityChildThrowsError() {
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(PARENT_PROCESS_ERROR_BOUNDARY).build()
        );

        assertThat(processInstance).isNotNull();

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).extracting(Task::getName).containsExactly("Error Handler Task");

        // The child process throws error id="childError1", code="CHILD_404", name="Child Not Found".
        // The parent model has no error with code CHILD_404, so errorName falls back to the code.
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).containsKey("testError");

        ErrorContext errorContext = (ErrorContext) variables.get("testError");
        assertThat(errorContext)
            .extracting(
                ErrorContext::getErrorCode,
                ErrorContext::getErrorId,
                ErrorContext::getErrorName,
                ErrorContext::getSourceId
            )
            .containsExactly("CHILD_404", "childError1", "CHILD_404", "Activity_1w8vukd");
    }
}
