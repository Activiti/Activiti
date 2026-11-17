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

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

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
    private static final String ERROR_BOUNDARY_MULTI_INSTANCE_OUTPUT_MAPPING =
        "errorBoundaryMultiInstanceOutputMapping";

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
        assertThat(variables)
            .extractingByKey("boundaryError", as(MAP))
            .extractingByKeys("errorId", "errorCode", "errorName", "attachedToRef")
            .containsExactly("errorId", "404", "NOT_FOUND", "subProcess");
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

        assertThat(variables)
            .extractingByKey("catchAllError", as(MAP))
            .extractingByKeys("errorCode", "errorName", "attachedToRef")
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

        // The event subprocess sits inside the embedded subprocess "subProcess"; the mapped
        // variable must be stored on the process instance, not on the intermediate scope.
        Map<String, Object> variables = runtimeService.getVariablesLocal(processInstance.getId());

        assertThat(variables)
            .extractingByKey("subProcessError", as(MAP))
            .extractingByKeys("errorId", "errorCode", "errorName", "attachedToRef")
            .containsExactly("errorId", "503", "SERVICE_UNAVAILABLE", null);
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
        // The name is taken from the child model, even though the parent model has no such error.
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());

        assertThat(variables)
            .extractingByKey("testError", as(MAP))
            .extractingByKeys("errorId", "errorCode", "errorName", "attachedToRef")
            .containsExactly("childError1", "CHILD_404", "Child Not Found", "Activity_1w8vukd");
    }

    @Test
    void should_MapErrorObject_When_BoundaryOnMultiInstanceActivityCatches() {
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_MULTI_INSTANCE_OUTPUT_MAPPING).build()
        );

        assertThat(processInstance).isNotNull();

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).extracting(Task::getName).containsExactly("Multi Instance Error Task");

        Map<String, Object> variables = runtimeService.getVariablesLocal(processInstance.getId());

        assertThat(variables)
            .extractingByKey("miError", as(MAP))
            .extractingByKeys("errorId", "errorCode", "errorName", "attachedToRef")
            .containsExactly("miErrorId", "MI_500", "MI_FAILED", "miSubProcess");
    }
}
