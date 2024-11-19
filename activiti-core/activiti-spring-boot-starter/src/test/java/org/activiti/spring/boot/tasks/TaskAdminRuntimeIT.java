/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.spring.boot.tasks;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskAdminRuntimeIT {

    private static final String ADMIN = "admin";

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private TaskAdminRuntime taskAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    public void setUp() {
        securityUtil.logInAs(ADMIN);
    }

    @AfterEach
    public void taskCleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    void should_returnLastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey() {

        ProcessInstance processInstance = processAdminRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey("Process_at2zjUes")
            .build());
        Task task = taskAdminRuntime.tasks(Pageable.of(0, 1)).getContent().getFirst();

        Task retrievedTask = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(processInstance.getId(), task.getTaskDefinitionKey());
        assertThat(retrievedTask).isEqualTo(task);
    }

    @Test
    public void should_returnLastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey_whenTaskIsInALoop() {
        String taskDefinitionKey = "Task_125yjke";
        final ProcessInstance processInstance = processAdminRuntime.start(new StartProcessPayloadBuilder().withProcessDefinitionKey("Process_N4qkN051N").build());
        Task task1 = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(processInstance.getId(), taskDefinitionKey);

        //complete task and provide a value that causes a loop back
        taskAdminRuntime.complete(new CompleteTaskPayload(task1.getId(), singletonMap("formInput", "provided-it1")));

        Task task2 = taskAdminRuntime.lastCreatedTaskByProcessInstanceIdAndTaskDefinitionKey(processInstance.getId(), taskDefinitionKey);
        assertThat(task2).satisfies(t -> {
            assertThat(t.getId()).isNotEqualTo(task1.getId());
            assertThat(t.getProcessInstanceId()).isEqualTo(processInstance.getId());
            assertThat(t.getTaskDefinitionKey()).isEqualTo(taskDefinitionKey);
        });

    }

}
