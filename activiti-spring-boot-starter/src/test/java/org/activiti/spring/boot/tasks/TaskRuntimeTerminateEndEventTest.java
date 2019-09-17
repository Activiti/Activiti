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

import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.task.model.Task;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaskRuntimeTerminateEndEventTest {

    private static final String TASK_PROCESS_TERMINATE_EVENT = "ProcessTerminateEvent";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;
    @Autowired
    private ProcessBaseRuntime processBaseRuntime;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @After
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processTerminateEventWithParallelTasks() {
        ProcessInstance process = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_PROCESS_TERMINATE_EVENT);

        List<Task> taskList = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskList).isNotEmpty();
        assertThat(taskList).hasSize(2);

        Task task1 = taskList.get(0);

        taskBaseRuntime.completeTask(task1.getId());

        List<Task> taskAfterCompleted = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskAfterCompleted).hasSize(0);
    }

}
