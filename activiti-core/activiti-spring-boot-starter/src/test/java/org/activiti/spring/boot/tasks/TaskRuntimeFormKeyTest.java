/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.builders.UpdateTaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class TaskRuntimeFormKeyTest {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";

    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private SecurityUtil securityUtil;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void standaloneTaskHasFormKey() {
        securityUtil.logInAs("garth");
        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("atask")
                .withAssignee("garth")
                .withFormKey("aFormKey")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));
        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getFormKey()).isEqualTo("aFormKey");

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
    }

    @Test
    public void shouldUpdateTaskFormKey() {
        securityUtil.logInAs("garth");
        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("atask")
                .withAssignee("garth")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));
        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        taskRuntime.update(new UpdateTaskPayloadBuilder()
                .withTaskId(task.getId())
                .withFormKey("aFormKey")
                .build());

        task = taskRuntime.task(task.getId());

        assertThat(task.getFormKey()).isEqualTo("aFormKey");

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
    }

    @Test
    public void processTaskHasFormKeyAndTaskDefinitionKey() {
        securityUtil.logInAs("garth");
        ProcessInstance process = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getFormKey()).isEqualTo("taskForm");
        assertThat(task.getTaskDefinitionKey()).isEqualTo("Task_03l0zc2");

        processRuntime.delete(ProcessPayloadBuilder.delete().withProcessInstance(process).build());
    }

}
