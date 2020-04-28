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

import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.events.TaskCancelledEvent;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class TaskRuntimeTerminateEndEventTest {

    private static final String TASK_PROCESS_TERMINATE_EVENT = "ProcessTerminateEvent";

    private static final String PROCESS_TERMINATE_EVENT = "Process_KzwZAEl-";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;
    @Autowired
    private ProcessBaseRuntime processBaseRuntime;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @BeforeEach
    public void setUp(){
        localEventSource.clearEvents();

    }

    @AfterEach
    public void tearDown(){
        taskCleanUpUtil.cleanUpWithAdmin();
        localEventSource.clearEvents();
    }

    @Test
    public void should_ProcessesAndTasksDisappear_whenTerminateEventIsExecuted() {
        ProcessInstance process = processBaseRuntime.startProcessWithProcessDefinitionKey(TASK_PROCESS_TERMINATE_EVENT);

        List<Task> taskList = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskList).isNotEmpty();
        assertThat(taskList).hasSize(2);

        Task task1 = taskList.get(0);

        taskBaseRuntime.completeTask(task1.getId());

        List<Task> taskAfterCompleted = taskBaseRuntime.getTasksByProcessInstanceId(process.getId());
        assertThat(taskAfterCompleted).hasSize(0);

    }

    @Test
    public void should_CancelledTasksByTerminateEndEventHaveCancellationReasonSet(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(PROCESS_TERMINATE_EVENT);
        assertThat(processInstance).isNotNull();

        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(2);
        Task task2 = tasks.get(1);

        taskBaseRuntime.completeTask(task2.getId());

        List<Task> tasksAfterCompletion = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasksAfterCompletion).hasSize(0);

        List<TaskCancelledEvent> taskCancelledEvents =
            localEventSource.getEvents(TaskCancelledEvent.class);

        assertThat(taskCancelledEvents).hasSize(1);
        assertThat(taskCancelledEvents.get(0).getReason()).contains("Terminated by end event");

    }

}
