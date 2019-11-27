/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeMultiInstanceIT {

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Before
    public void setUp() {
        localEventSource.clearEvents();
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void processWithMultiInstances_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelUserTasksCompletionCondition");

        //then
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName,
                            Task::getTaskDefinitionKey)
                .containsExactlyInAnyOrder(tuple("My Task 0", "miTasks"),
                                           tuple("My Task 1", "miTasks"),
                                           tuple("My Task 2", "miTasks"),
                                           tuple("My Task 3", "miTasks"));

        List<TaskCreatedEvent> taskCreatedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CREATED))
                .map(TaskCreatedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCreatedEvents)
                .extracting(event -> event.getEntity().getName(),
                            event -> event.getEntity().getTaskDefinitionKey())
                .containsExactlyInAnyOrder(tuple("My Task 0", "miTasks"),
                                           tuple("My Task 1", "miTasks"),
                                           tuple("My Task 2", "miTasks"),
                                           tuple("My Task 3", "miTasks"));

        //given
        Task taskToComplete = tasks.get(0);
        localEventSource.clearEvents();

        //when first multi instance is completed: 3 remaining / completion condition not reached
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactly(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                       taskToComplete.getName()));

        //given
        localEventSource.clearEvents();
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactlyInAnyOrder(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                                 taskToComplete.getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                                                 tasks.get(2).getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                                                 tasks.get(3).getName())
                );

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                                processInstance.getId()));
    }

    @Test
    public void processWithMultiInstancesOnSubProcess_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelSubprocessCompletionCondition");

        //then
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("Task in sub-process 0",
                          "Task in sub-process 1",
                          "Task in sub-process 2",
                          "Task in sub-process 3");

        List<TaskCreatedEvent> taskCreatedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CREATED))
                .map(TaskCreatedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCreatedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("Task in sub-process 0",
                                           "Task in sub-process 1",
                                           "Task in sub-process 2",
                                           "Task in sub-process 3");

        //given
        Task taskToComplete = tasks.get(0);
        localEventSource.clearEvents();

        //when first multi instance is completed: 3 remaining / completion condition not reached
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactly(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                       taskToComplete.getName()));

        //given
        localEventSource.clearEvents();
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactlyInAnyOrder(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                                 taskToComplete.getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                                                 tasks.get(2).getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                                                 tasks.get(3).getName())
                );

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                                processInstance.getId()));
    }
}
