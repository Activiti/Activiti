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
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCompletedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.spring.boot.RuntimeTestConfiguration;
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
    public void processWithParallelMultiInstancesOnUserTask_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelUserTasksCompletionCondition");

        //then
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("My Task 0",
                          "My Task 1",
                          "My Task 2",
                          "My Task 3");

        List<TaskCreatedEvent> taskCreatedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CREATED))
                .map(TaskCreatedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCreatedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("My Task 0",
                                           "My Task 1",
                                           "My Task 2",
                                           "My Task 3");

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 4, 0,0);

        //given
        Task taskToComplete = tasks.get(0);

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
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        taskBaseRuntime.completeTask(taskToComplete);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 4, 2,2);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactlyInAnyOrder(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                                 tasks.get(0).getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
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
    public void processWithParallelMultiInstancesOnSubProcess_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
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

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 4, 0,0);

        //given
        Task taskToComplete = tasks.get(0);

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
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        taskBaseRuntime.completeTask(taskToComplete);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 4, 2,2);

        //then
        assertThat(localEventSource.getEvents())
                .filteredOn(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED)
                        || event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED))
                .extracting(RuntimeEvent::getEventType,
                            event -> ((Task) event.getEntity()).getName())
                .containsExactlyInAnyOrder(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                                                 tasks.get(0).getName()),
                                           tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
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
    public void processWithParallelMultiInstancesCallActivity_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelCallActivityCompletionCondition");

        List<ProcessInstance> childProcess = processBaseRuntime.getProcessInstances();
        assertThat(childProcess.size()).isEqualTo(5);


        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED))
                .map(ProcessStartedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(0).getId(), null),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(1).getId(), processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(2).getId(), processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(3).getId(), processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(4).getId(), processInstance.getId()));

        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED))
                .map(TaskAssignedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getName(),
                        event -> event.getEntity().getProcessInstanceId()
                ).containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(1).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(2).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(3).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(4).getId()));


        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                .collect(Collectors.toList()).size()).isEqualTo(0);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 4, 0,0);

        //for i = 1 ,  first multi instance is completed: 3 remaining / completion condition not reached
        //for i = 2 ,  second multi instance is completed: 2 remaining / completion condition reached
        for(int i=1; i < 3; i++) {

            List<Task> tasks = taskBaseRuntime.getTasks(childProcess.get(i));

            assertThat(tasks.size()).isEqualTo(1);
            assertThat(tasks)
                    .extracting(Task::getName)
                    .contains("User Task");
            taskBaseRuntime.completeTask(tasks.get(0));

            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                    .map(TaskCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getName(),
                            event -> event.getEntity().getProcessInstanceId())
                    .contains(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", childProcess.get(i).getId()));


            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                    .map(ProcessCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getId(),
                            event -> event.getEntity().getParentId()
                    ).contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(i).getId(), processInstance.getId()));
        }

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 4, 2,2);

        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();
        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                .map(ProcessCompletedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).contains(
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(2).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(3).getId(), null),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(4).getId(), null),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, processInstance.getId(), null));
    }

    @Test
    public void processWithSequentialMultiInstancesOnUserTask_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialUserTasks");

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 1, 0, 0);

        for(int i = 0; i< 4;i ++) {
            //then
            List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
            assertThat(tasks.size()).isEqualTo(1);
            assertThat(tasks)
                    .extracting(Task::getName)
                    .containsExactlyInAnyOrder("My Task " + i);
            taskBaseRuntime.completeTask(tasks.get(0));
        }

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 4, 4, 0);

        List<TaskCreatedEvent> taskCreatedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CREATED))
                .map(TaskCreatedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCreatedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1",
                        "My Task 2",
                        "My Task 3");

        List<TaskCompletedEvent> taskCompletedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                .map(TaskCompletedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCompletedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1",
                        "My Task 2",
                        "My Task 3");

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithSequentialMultiInstancesOnSubProcess_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialSubprocess");

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 1, 0, 0);

        for(int i = 0; i< 4;i ++) {
            //then
            List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
            assertThat(tasks.size()).isEqualTo(1);
            assertThat(tasks)
                    .extracting(Task::getName)
                    .containsExactlyInAnyOrder("Task in sub-process " + i);
            taskBaseRuntime.completeTask(tasks.get(0));
        }


        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 4, 4, 0);

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

        List<TaskCompletedEvent> taskCompletedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                .map(TaskCompletedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCompletedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("Task in sub-process 0",
                        "Task in sub-process 1",
                        "Task in sub-process 2",
                        "Task in sub-process 3");

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithSequentialMultiInstancesOnCallActivity_should_emmit_EqualStartAndEndEvent() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialCallActivity");

        List<ProcessInstance> childProcess = processBaseRuntime.getProcessInstances();
        assertThat(childProcess.size()).isEqualTo(2);


        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                .collect(Collectors.toList()).size()).isEqualTo(0);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 1, 0, 0);

        // multi instance is sequential 4 times
        for(int i=1; i < 5; i++) {

            childProcess = processBaseRuntime.getProcessInstances();
            List<Task> tasks = taskBaseRuntime.getTasks(childProcess.get(1));

            assertThat(tasks.size()).isEqualTo(1);
            assertThat(tasks)
                    .extracting(Task::getName)
                    .contains("User Task");
            taskBaseRuntime.completeTask(tasks.get(0));

            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                    .map(TaskCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getName(),
                            event -> event.getEntity().getProcessInstanceId())
                    .contains(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", childProcess.get(1).getId()));


            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                    .map(ProcessCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getId(),
                            event -> event.getEntity().getParentId()
                    ).contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(1).getId(), processInstance.getId()));
        }

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 4, 4, 0);

        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithParallelMultiInstancesOnUserTask_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelUserTasks");

        //then
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1",
                        "My Task 2",
                        "My Task 3");

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 4, 0, 0);

        List<TaskCreatedEvent> taskCreatedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_CREATED))
                .map(TaskCreatedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCreatedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1",
                        "My Task 2",
                        "My Task 3");

        for(int i = 0; i< 4;i ++) {
            //then
            taskBaseRuntime.completeTask(tasks.get(i));
        }

        List<TaskCompletedEvent> taskCompletedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                .map(TaskCompletedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCompletedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1",
                        "My Task 2",
                        "My Task 3");

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miTasks", 4, 4, 0);

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithParallelMultiInstancesOnSubProcess_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelSubprocess");

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

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 4, 0, 0);

        for(int i = 0; i< 4;i ++) {
            //then
            taskBaseRuntime.completeTask(tasks.get(i));
        }

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miSubProcess", 4, 4, 0);

        List<TaskCompletedEvent> taskCompletedEvents = localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                .map(TaskCompletedEvent.class::cast)
                .collect(Collectors.toList());
        assertThat(taskCompletedEvents)
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("Task in sub-process 0",
                        "Task in sub-process 1",
                        "Task in sub-process 2",
                        "Task in sub-process 3");

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithParallelMultiInstancesOnCallActivity_should_emmit_EqualStartAndEndEvent() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelCallActivity");

        List<ProcessInstance> childProcess = processBaseRuntime.getProcessInstances();
        assertThat(childProcess.size()).isEqualTo(5);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 4, 0, 0);

        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED))
                .map(ProcessStartedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).containsExactlyInAnyOrder(
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(0).getId(), null),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(1).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(2).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(3).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childProcess.get(4).getId(), processInstance.getId()));

        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED))
                .map(TaskAssignedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getName(),
                        event -> event.getEntity().getProcessInstanceId()
                ).containsExactlyInAnyOrder(
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(1).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(2).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(3).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childProcess.get(4).getId()));


        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                .collect(Collectors.toList()).size()).isEqualTo(0);

        //for i = 1 ,  first multi instance is completed: 3 remaining / completion condition not reached
        //for i = 2 ,  second multi instance is completed: 2 remaining / completion condition reached
        for(int i=1; i < 5; i++) {

            List<Task> tasks = taskBaseRuntime.getTasks(childProcess.get(i));

            assertThat(tasks.size()).isEqualTo(1);
            assertThat(tasks)
                    .extracting(Task::getName)
                    .contains("User Task");
            taskBaseRuntime.completeTask(tasks.get(0));

            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED))
                    .map(TaskCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getName(),
                            event -> event.getEntity().getProcessInstanceId())
                    .contains(tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", childProcess.get(i).getId()));


            assertThat(localEventSource.getEvents().stream()
                    .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                    .map(ProcessCompletedEvent.class::cast))
                    .extracting(
                            RuntimeEvent::getEventType,
                            event -> event.getEntity().getId(),
                            event -> event.getEntity().getParentId()
                    ).contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(i).getId(), processInstance.getId()));
        }


        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount("miCallActivity", 4, 4, 0);

        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();

        assertThat(localEventSource.getEvents().stream()
                .filter(event -> event.getEventType().equals(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED))
                .map(ProcessCompletedEvent.class::cast))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).contains(
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(1).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(2).getId(), processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(3).getId(),  processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, childProcess.get(4).getId(),  processInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED, processInstance.getId(), null));
    }

    @Test
    public void processWithSequentialMultiInstancesOnManualTask_should_emmit_EqualStartAndEndEvent() {
        test_MultiInstance_StartAndEndEventCount("miSequentialManualTasks", "miTasks", 4, 4, 0);
    }

    @Test
    public void processWithSequentialMultiInstancesOnServiceTask_should_emmit_EqualStartAndEndEvent() {
        test_MultiInstance_StartAndEndEventCount("miSequentialServiceTask", "miServiceTask", 4,4, 4);
    }

    @Test
    public void processWithParallelMultiInstancesOnManualTask_should_emmit_EqualStartAndEndEvent() {
        test_MultiInstance_StartAndEndEventCount("miParallelManualTasks", "miTasks", 4,4, 0);
    }

    @Test
    public void processWithParallelMultiInstancesOnServiceTask_should_emmit_EqualStartAndEndEvent() {
        test_MultiInstance_StartAndEndEventCount("miParallelServiceTask", "miServiceTask", 4,4,4);
    }

    private void test_MultiInstance_StartAndEndEventCount(String processDefinitionKey,String elementId, Integer startCount,Integer completeCount, Integer connectorCount) {
        RuntimeTestConfiguration.testConnectorMultiInstanceExecutionCount = 0;

        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(processDefinitionKey);

        // testing the multi instance element start, complete, cancel events
        test_started_completed_canceledCount(elementId, startCount, completeCount, 0);

        //checking the connector execution count
        assertThat(RuntimeTestConfiguration.testConnectorMultiInstanceExecutionCount).isEqualTo(connectorCount);

        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));

    }

    @Test
    public void processWithParallelMultiInstancesManualTask_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        test_parallel_complete_condition_Events("miParallelManualTasksCompletionCondition", "miTasks", 0, 0);
    }

    @Test
    public void processWithParallelMultiInstancesServiceTask_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        test_parallel_complete_condition_Events("miParallelServiceTaskCompletionCondition", "miServiceTask", 2,4);
    }

    private void test_parallel_complete_condition_Events(String processDefinitionKey, String elementId, Integer minCount, Integer maxCount) {
        RuntimeTestConfiguration.testConnectorMultiInstanceExecutionCount = 0;

        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(processDefinitionKey);

        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED)
                .map(BPMNActivityStartedEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isGreaterThanOrEqualTo(2)
                .isLessThanOrEqualTo(4);

        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED)
                .map(BPMNActivityCompletedEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isGreaterThanOrEqualTo(2)
                .isLessThanOrEqualTo(4);

        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED)
                .map(BPMNActivityCancelledEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(2);

        //checking the connector execution count
        assertThat(RuntimeTestConfiguration.testConnectorMultiInstanceExecutionCount).isGreaterThanOrEqualTo(minCount).isLessThanOrEqualTo(maxCount);

        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));

    }


    private void test_started_completed_canceledCount(String elementId, Integer startedCount, Integer completedCount, Integer canceledCount ) {
        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED)
                .map(BPMNActivityStartedEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isEqualTo(startedCount);

        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED)
                .map(BPMNActivityCompletedEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isEqualTo(completedCount);

        assertThat(localEventSource.getEvents().stream()
                .filter(event-> event.getEventType() == BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED)
                .map(BPMNActivityCancelledEvent.class::cast)
                .filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList())
                .size())
                .isEqualTo(canceledCount);
    }


}
