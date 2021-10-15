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

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeClaimTaskFromProcessTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    private static final String TWOTASK_PROCESS = "twoTaskProcess";

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void claimTaskWithoutGroup() {
        securityUtil.logInAs("user");

        //when
        ProcessInstance twoTaskInstance = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(TWOTASK_PROCESS)
                .build()
        );

        securityUtil.logInAs("dean");

        Task task = taskRuntime
            .tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build())
            .getContent()
            .get(0);

        taskRuntime.claim(
            TaskPayloadBuilder.claim().withTaskId(task.getId()).build()
        );

        //should still be in dean's list after claiming
        task =
            taskRuntime
                .tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build())
                .getContent()
                .get(0);

        assertThat(task).isNotNull();

        taskRuntime.complete(
            TaskPayloadBuilder.complete().withTaskId(task.getId()).build()
        );
    }
}
