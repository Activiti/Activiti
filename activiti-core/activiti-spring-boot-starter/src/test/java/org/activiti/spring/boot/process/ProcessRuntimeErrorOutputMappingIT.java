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

import static java.util.Map.entry;
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
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProcessRuntimeErrorOutputMappingIT {

    private static final String ERROR_BOUNDARY_OUTPUT_MAPPING = "errorBoundaryEventOutputMapping";

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

    @AfterEach
    void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    void should_MapErrorCodeAndName_When_ErrorBoundaryEventCatches() {
        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(ERROR_BOUNDARY_OUTPUT_MAPPING).build()
        );

        assertThat(processInstance).isNotNull();

        // Verify the process reached the error task (boundary caught the error)
        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
            .withProcessInstanceId(processInstance.getId())
            .build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), getTasksPayload);
        assertThat(tasks.getContent()).hasSize(1).extracting(Task::getName).containsExactly("Task");

        // Verify the error output mapping variables were set
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(variables).contains(entry("boundaryErrorCode", "404"), entry("boundaryErrorName", "NOT_FOUND"));
    }
}
