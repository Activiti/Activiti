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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.events.BPMNActivityCancelledEvent;
import org.activiti.api.process.model.events.BPMNActivityCompletedEvent;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNActivityStartedEvent;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.events.TaskAssignedEvent;
import org.activiti.api.task.runtime.events.TaskCreatedEvent;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.spring.boot.process.ProcessBaseRuntime;
import org.activiti.spring.boot.process.ProcessRuntimeBPMNTimerIT;
import org.activiti.spring.boot.process.TimerTestConfigurator;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerCancelledListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerExecutedListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerFiredListener;
import org.activiti.spring.boot.process.listener.DummyBPMNTimerScheduledListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(ProcessRuntimeBPMNTimerIT.PROCESS_RUNTIME_BPMN_TIMER_IT)
@Import({TimerTestConfigurator.class,
        DummyBPMNTimerFiredListener.class,
        DummyBPMNTimerScheduledListener.class,
        DummyBPMNTimerCancelledListener.class,
        DummyBPMNTimerExecutedListener.class})
public class TaskRuntimeMultiInstanceIT {

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    public void setUp() {
        localEventSource.clearEvents();
    }

    @AfterEach
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
        processEngineConfiguration.getClock().reset();
        localEventSource.clearEvents();
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

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                        RuntimeEvent::getEventType
                )
                .containsExactlyInAnyOrder(tuple("My Task 0",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("My Task 1",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("My Task 2",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("My Task 3",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("My Task 0",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("My Task 1",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("My Task 2",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("My Task 3",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        //given
        Task taskToComplete = tasks.get(0);

        //when first multi instance is completed: 3 remaining / completion condition not reached
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType
                )
                .containsExactly(tuple(taskToComplete.getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_COMPLETED));

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED);


        //given
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskToComplete);

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType
                )
                .containsExactlyInAnyOrder(tuple(taskToComplete.getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_COMPLETED),
                                           tuple(tasks.get(2).getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                                           tuple(tasks.get(3).getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_CANCELLED));

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED);


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

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType
                )
                .containsExactlyInAnyOrder(tuple("Task in sub-process 0",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("Task in sub-process 1",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("Task in sub-process 2",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("Task in sub-process 3",
                                                 TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                                           tuple("Task in sub-process 0",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("Task in sub-process 1",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("Task in sub-process 2",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                                           tuple("Task in sub-process 3",
                                                 TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        //given
        Task taskToComplete = tasks.get(0);

        //when first multi instance is completed: 3 remaining / completion condition not reached
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskToComplete);

        //then
        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType
                )
                .containsExactly(tuple(taskToComplete.getName(),
                                       TaskRuntimeEvent.TaskEvents.TASK_COMPLETED));

        assertActivityEvents( "miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        //given
        taskToComplete = tasks.get(1);

        //when second multi instance is completed: 2 remaining / completion condition reached
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskToComplete);


        //then
        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType
                )
                .containsExactlyInAnyOrder(tuple(taskToComplete.getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_COMPLETED),
                                           tuple(tasks.get(2).getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                                           tuple(tasks.get(3).getName(),
                                                 TaskRuntimeEvent.TaskEvents.TASK_CANCELLED));

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED
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
        ProcessInstance parentProcessInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelCallActivityCompletionCondition");

        List<ProcessInstance> childrenProcess = processBaseRuntime.getChildrenProcessInstances(parentProcessInstance.getId()).getContent();
        assertThat(childrenProcess).hasSize(4);

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance) event.getEntity()).getId(),
                        event -> ((ProcessInstance) event.getEntity()).getParentId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              parentProcessInstance.getId(),
                              null),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              parentProcessInstance.getId(),
                              null),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              childrenProcess.get(0).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              childrenProcess.get(0).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              childrenProcess.get(1).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              childrenProcess.get(1).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              childrenProcess.get(2).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              childrenProcess.get(2).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              childrenProcess.get(3).getId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              childrenProcess.get(3).getId(),
                              parentProcessInstance.getId())
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task)event.getEntity()).getName(),
                        event -> ((Task)event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(0).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(1).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(2).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(3).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(0).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(1).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(2).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(3).getId())
        );


        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        //first multi instance is completed: 3 remaining / completion condition not reached
        List<TaskAssignedEvent> assignedTasksEvents = localEventSource.getEvents(TaskAssignedEvent.class);
        TaskAssignedEvent taskAssignedEvent = assignedTasksEvents.get(0);
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskAssignedEvent.getEntity().getId());

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance) event.getEntity()).getId(),
                        event -> ((ProcessInstance) event.getEntity()).getParentId()
                )
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              taskAssignedEvent.getEntity().getProcessInstanceId(),
                              parentProcessInstance.getId())
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "User Task",
                              taskAssignedEvent.getEntity().getProcessInstanceId())
                );


        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        //second multi instance is completed: 2 remaining / completion condition reached
        taskAssignedEvent = assignedTasksEvents.get(1);
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(taskAssignedEvent.getEntity().getId());
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              taskAssignedEvent.getEntity().getProcessInstanceId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED,
                              assignedTasksEvents.get(2).getEntity().getProcessInstanceId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED,
                              assignedTasksEvents.get(3).getEntity().getProcessInstanceId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              parentProcessInstance.getId())
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task)event.getEntity()).getName(),
                        event -> ((Task)event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", taskAssignedEvent.getEntity().getProcessInstanceId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED, "User Task", assignedTasksEvents.get(2).getEntity().getProcessInstanceId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED, "User Task", assignedTasksEvents.get(3).getEntity().getProcessInstanceId())
                );


        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED
                );



        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();

    }

    @Test
    public void processWithSequentialMultiInstancesOnUserTask_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialUserTasks");

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              processInstance.getId())
                );

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "My Task 0",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "My Task 0",
                              processInstance.getId())
                );
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("My Task 0");

        //complete first iteration, multi-instance should not complete yet
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED //second iteration was created
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "My Task 0",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "My Task 1",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "My Task 1",
                              processInstance.getId())
                );

        tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("My Task 1");

        //complete second iteration, multi-instance should not complete
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents( "miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "My Task 1",
                              processInstance.getId())
                );


        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithSequentialMultiInstancesOnSubProcess_should_emmit_EqualStartAndEndEvent() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialSubprocess");

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              processInstance.getId())
                );

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "Task in sub-process 0",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "Task in sub-process 0",
                              processInstance.getId())
                );
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("Task in sub-process 0");

        //complete first iteration, multi-instance should not complete yet
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED //second iteration was created
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "Task in sub-process 0",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "Task in sub-process 1",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "Task in sub-process 1",
                              processInstance.getId())
                );

        tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("Task in sub-process 1");

        //complete second iteration, multi-instance should not complete
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "Task in sub-process 1",
                              processInstance.getId())
                );


        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .containsExactly(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));
    }

    @Test
    public void processWithSequentialMultiInstancesOnCallActivity_should_emmit_EqualStartAndEndEvent() {
        ProcessInstance parentProcessInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialCallActivity");

        List<ProcessInstance> childrenProcessInstances = processBaseRuntime.getChildrenProcessInstances(parentProcessInstance.getId()).getContent();
        assertThat(childrenProcessInstances).hasSize(1);
        ProcessInstance firstChildProcInst = childrenProcessInstances.get(0);

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              firstChildProcInst.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              firstChildProcInst.getId())
                );

        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "User Task",
                              firstChildProcInst.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "User Task",
                              firstChildProcInst.getId())
                );
        List<Task> tasks = taskBaseRuntime.getTasks(firstChildProcInst);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("User Task");

        //complete first iteration, multi-instance should not complete yet
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED //second iteration was created
                );


        childrenProcessInstances = processBaseRuntime.getChildrenProcessInstances(parentProcessInstance.getId()).getContent();
        assertThat(childrenProcessInstances).hasSize(1);
        ProcessInstance secondChildProcInst = childrenProcessInstances.get(0);

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "User Task",
                              firstChildProcInst.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "User Task",
                              secondChildProcInst.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "User Task",
                              secondChildProcInst.getId())
                );

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            event -> ((ProcessInstance) event.getEntity()).getId())
                .contains(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              firstChildProcInst.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              secondChildProcInst.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              secondChildProcInst.getId()));

        tasks = taskBaseRuntime.getTasks(secondChildProcInst);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("User Task");

        //complete second iteration, multi-instance should not complete
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertActivityEvents("miCallActivity",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              "User Task",
                              secondChildProcInst.getId())
                );


        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        secondChildProcInst.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              parentProcessInstance.getId()));
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
                        "My Task 1");

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              processInstance.getId())
                );


        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "My Task 0",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                             "My Task 0",
                             processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "My Task 1",
                              processInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                             "My Task 1",
                             processInstance.getId())
                );


        //complete first iteration: multi instance should not complete yet
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));


        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              tasks.get(0).getName(),
                              processInstance.getId())
                );

        assertThat(localEventSource.getProcessInstanceEvents()).isEmpty();

        //complete second iteration: multi instance should complete
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(1));

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              tasks.get(1).getName(),
                              processInstance.getId())
                );

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((ProcessInstance)event.getEntity()).getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              processInstance.getId())
                );

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
    }

    @Test
    public void processWithParallelMultiInstancesOnUserTask_Boundary_Event() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelUserTaskBoundaryEvent");

        //then
        assertThat(taskBaseRuntime.getTasks(processInstance))
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("My Task 0",
                        "My Task 1");

        assertActivityEvents("miTasks",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType)
                .containsExactlyInAnyOrder(
                        tuple("My Task 0",
                              TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                        tuple("My Task 1",
                              TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                        tuple("My Task 0",
                              TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                        tuple("My Task 1",
                              TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));


        //when
        long waitTime = 1 * 60 * 1000;
        Date startTime = new Date();
        Date dueDate = new Date(startTime.getTime() + waitTime);

        //set the clock so the timer fires
        localEventSource.clearEvents();
        processEngineConfiguration.getClock().setCurrentTime(new Date(dueDate.getTime()));

        await().untilAsserted(() -> {
            assertThat(localEventSource.getEvents(BPMNTimerEvent.class))
                .extracting(BPMNTimerEvent::getEventType,
                    BPMNTimerEvent::getProcessDefinitionId,
                    event -> event.getEntity().getProcessDefinitionId(),
                    event -> event.getEntity().getProcessInstanceId(),
                    event -> event.getEntity().getElementId()
                )
                .containsExactly(
                    tuple(BPMNTimerEvent.TimerEvents.TIMER_FIRED,
                        processInstance.getProcessDefinitionId(),
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        "timer"
                    ),
                    tuple(BPMNTimerEvent.TimerEvents.TIMER_EXECUTED,
                        processInstance.getProcessDefinitionId(),
                        processInstance.getProcessDefinitionId(),
                        processInstance.getId(),
                        "timer"
                    )

                );

            assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                    RuntimeEvent::getEventType)
                .containsExactlyInAnyOrder(
                    tuple("My Task 0",
                        TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                    tuple("My Task 1",
                        TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                    tuple("Escalation Task",
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                    tuple("Escalation Task",
                        TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));

            assertActivityEvents("miTasks",
                BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED
            );

            //need to login again before getting the list of tasks
            //because Awaitility will run this inside another thread
            securityUtil.logInAs("user");

            List<Task> availableTasks = taskBaseRuntime.getTasks(processInstance);
            assertThat(availableTasks)
                .extracting(Task::getName)
                .containsExactly("Escalation Task");
            localEventSource.clearEvents();
            taskBaseRuntime.completeTask(availableTasks.get(0));
        });


        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType)
                .containsExactly(
                        tuple("Escalation Task",
                              TaskRuntimeEvent.TaskEvents.TASK_COMPLETED));

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              processInstance.getId()));
    }

    @Test
    public void processWithParallelMultiInstancesOn_SubProcess_Boundary_Event() {
        //when
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelSubProcessBoundaryEvent");

        //then
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks)
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("Task in sub-process 0",
                        "Task in sub-process 1");

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType)
                .containsExactlyInAnyOrder(
                        tuple("Task in sub-process 0",
                              TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                        tuple("Task in sub-process 1",
                              TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                        tuple("Task in sub-process 0",
                              TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED),
                        tuple("Task in sub-process 1",
                              TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));

        //send signal: boundary event should cancel multi-instance
        localEventSource.clearEvents();
        processBaseRuntime.signal("goMiParallelSubProcessBoundaryEvent");

        assertThat(localEventSource.getTaskEvents())
                .extracting(event -> ((Task) event.getEntity()).getName(),
                            RuntimeEvent::getEventType)
                .containsExactlyInAnyOrder(
                        tuple("Task in sub-process 0",
                              TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                        tuple("Task in sub-process 1",
                              TaskRuntimeEvent.TaskEvents.TASK_CANCELLED),
                        tuple("Escalation Task",
                              TaskRuntimeEvent.TaskEvents.TASK_CREATED),
                        tuple("Escalation Task",
                              TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED));

        assertActivityEvents("miSubProcess",
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED
                );

        tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks.size()).isEqualTo(1);
        assertThat(tasks)
                .extracting(Task::getName)
                .contains("Escalation Task");

        taskBaseRuntime.completeTask(tasks.get(0));

        assertThat(taskBaseRuntime.getTasks(processInstance)).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              processInstance.getId()));
    }

    @Test
    public void processWithParallelMultiInstancesOn_CallActivity_Boundary_Event() {
        ProcessInstance parentProcessInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelCallActivityBoundaryEvent");

        final Page<ProcessInstance> childrenProcessInstances = processBaseRuntime.getChildrenProcessInstances(parentProcessInstance.getId());
        assertThat(childrenProcessInstances.getContent()).hasSize(2);

        assertThat(localEventSource.getEvents(ProcessStartedEvent.class))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).containsExactlyInAnyOrder(
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                      parentProcessInstance.getId(),
                      null),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                      childrenProcessInstances.getContent().get(0).getId(),
                      parentProcessInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                      childrenProcessInstances.getContent().get(1).getId(),
                      parentProcessInstance.getId()));

        assertActivityEvents("miCallActivity",
                              BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                              BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
                );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "User Task",
                              childrenProcessInstances.getContent().get(0).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "User Task",
                              childrenProcessInstances.getContent().get(1).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "User Task",
                              childrenProcessInstances.getContent().get(0).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "User Task",
                              childrenProcessInstances.getContent().get(1).getId())
                );

        assertThat(localEventSource.getEvents(ProcessCompletedEvent.class)).isEmpty();

        //when
        localEventSource.clearEvents();
        processBaseRuntime.signal("goMiParallelCallActivityBoundaryEvent");


        // then
        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                              "User Task",
                              childrenProcessInstances.getContent().get(0).getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                              "User Task",
                              childrenProcessInstances.getContent().get(1).getId()),

                        tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                              "Escalation Task",
                              parentProcessInstance.getId()),
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                              "Escalation Task",
                              parentProcessInstance.getId())
                );

        assertActivityEvents("miCallActivity",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED);

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            event -> ((ProcessInstance) event.getEntity()).getId())
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED,
                              childrenProcessInstances.getContent().get(0).getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED,
                              childrenProcessInstances.getContent().get(1).getId())
                );



        List<Task> tasks = taskBaseRuntime.getTasks(parentProcessInstance);
        assertThat(tasks.size()).isEqualTo(1);
        assertThat(tasks)
                .extracting(Task::getName)
                .contains("Escalation Task");

        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertThat(taskBaseRuntime.getTasks(parentProcessInstance)).isEmpty();
        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              parentProcessInstance.getId()));

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
                        "Task in sub-process 1");

        assertThat(localEventSource.getEvents(TaskCreatedEvent.class))
                .extracting(event -> event.getEntity().getName())
                .containsExactlyInAnyOrder("Task in sub-process 0",
                        "Task in sub-process 1");

        assertActivityEvents("miSubProcess",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED);



        //complete first iteration: multi instance should not finish yet
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(0));

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              tasks.get(0).getName(),
                              processInstance.getId()));

        assertActivityEvents("miSubProcess",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED);

        assertThat(localEventSource.getProcessInstanceEvents()).isEmpty();

        //complete second iteration: multi instance should finish
        localEventSource.clearEvents();
        taskBaseRuntime.completeTask(tasks.get(1));


        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactly(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                              tasks.get(1).getName(),
                              processInstance.getId()));

        assertActivityEvents("miSubProcess",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED);

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              processInstance.getId()));
    }

    private void assertActivityEvents(String miCallActivity,
                                      BPMNActivityEvent.ActivityEvents ... activityEvents) {
        assertThat(localEventSource.getEvents(BPMNActivityEvent.class))
                .filteredOn(event -> miCallActivity.equals(event.getEntity().getElementId()))
                .extracting(BPMNActivityEvent::getEventType)
                .containsExactlyInAnyOrder(
                        activityEvents
                );
    }

    @Test
    public void processWithParallelMultiInstancesOnCallActivity_should_emmit_EqualStartAndEndEvent() {
        ProcessInstance parentProcessInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelCallActivity");

        List<ProcessInstance> childrenProcess = processBaseRuntime.getChildrenProcessInstances(parentProcessInstance.getId()).getContent();
        assertThat(childrenProcess).hasSize(2);

        assertActivityEvents("miCallActivity",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED);

        assertThat(localEventSource.getEvents(ProcessStartedEvent.class))
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> event.getEntity().getId(),
                        event -> event.getEntity().getParentId()
                ).containsExactlyInAnyOrder(
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, parentProcessInstance.getId(), null),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childrenProcess.get(0).getId(), parentProcessInstance.getId()),
                tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED, childrenProcess.get(1).getId(), parentProcessInstance.getId())
        );

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
       .containsExactlyInAnyOrder(
                tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(0).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_CREATED, "User Task", childrenProcess.get(1).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(0).getId()),
                tuple(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED, "User Task", childrenProcess.get(1).getId())
       );

        //complete first iteration: multi instance should not complete yet
        List<TaskAssignedEvent> taskAssignedEvents = localEventSource.getEvents(TaskAssignedEvent.class);
        localEventSource.clearEvents();
        TaskAssignedEvent taskAssignedEvent = taskAssignedEvents.get(0);
        taskBaseRuntime.completeTask(taskAssignedEvent.getEntity());

        assertActivityEvents("miCallActivity",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED);

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", taskAssignedEvent.getEntity().getProcessInstanceId())
                );

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId,
                            event -> ((ProcessInstance) event.getEntity()).getParentId())
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              taskAssignedEvent.getEntity().getProcessInstanceId(),
                              parentProcessInstance.getId()));

        //complete second iteration: multi instance should complete
        localEventSource.clearEvents();
        taskAssignedEvent = taskAssignedEvents.get(1);
        taskBaseRuntime.completeTask(taskAssignedEvent.getEntity());

        assertActivityEvents("miCallActivity",
                             BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED);

        assertThat(localEventSource.getTaskEvents())
                .extracting(
                        RuntimeEvent::getEventType,
                        event -> ((Task) event.getEntity()).getName(),
                        event -> ((Task) event.getEntity()).getProcessInstanceId()
                )
                .containsExactlyInAnyOrder(
                        tuple(TaskRuntimeEvent.TaskEvents.TASK_COMPLETED, "User Task", taskAssignedEvent.getEntity().getProcessInstanceId())
                );

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId,
                            event -> ((ProcessInstance) event.getEntity()).getParentId())
                .containsExactly(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              taskAssignedEvent.getEntity().getProcessInstanceId(),
                              parentProcessInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              parentProcessInstance.getId(),
                              null));


        assertThat(processBaseRuntime.getProcessInstances()).isEmpty();

    }

    @Test
    public void processWithSequentialMultiInstancesOnManualTask_should_emmit_EqualStartAndEndEvent() {
        verifyMultiInstanceStartAndEndEventCount("miSequentialManualTasks", "miTasks", 2, 2);
    }

    @Test
    public void processWithSequentialMultiInstancesOnServiceTask_should_emmit_EqualStartAndEndEvent() {
        verifyMultiInstanceStartAndEndEventCount("miSequentialServiceTask", "miServiceTask", 2, 2);
    }

    @Test
    public void processWithParallelMultiInstancesOnManualTask_should_emmit_EqualStartAndEndEvent() {
        verifyMultiInstanceStartAndEndEventCount("miParallelManualTasks", "miTasks", 2, 2);
    }

    @Test
    public void processWithParallelMultiInstancesOnServiceTask_should_emmit_EqualStartAndEndEvent() {
        verifyMultiInstanceStartAndEndEventCount("miParallelServiceTask", "miServiceTask", 2, 2);
    }

    private void verifyMultiInstanceStartAndEndEventCount(String processDefinitionKey,
                                                          String elementId,
                                                          Integer startCount,
                                                          Integer completeCount) {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(processDefinitionKey);

        // testing the multi instance element start, complete, cancel events
        verifyActivityStartedAndCompletedAreEmitted(elementId, startCount, completeCount);

        assertThat(localEventSource.getEvents())
                .extracting(RuntimeEvent::getEventType,
                        RuntimeEvent::getProcessInstanceId)
                .contains(tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        processInstance.getId()));

    }

    @Test
    public void processWithParallelMultiInstancesManualTask_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        verifyEventsOnAutomaticMultiInstanceWithCompletionCondition("miParallelManualTasksCompletionCondition", "miTasks");
    }

    @Test
    public void processWithParallelMultiInstancesServiceTask_should_emmitEventsAndContinueOnceCompletionConditionIsReached() {
        verifyEventsOnAutomaticMultiInstanceWithCompletionCondition("miParallelServiceTaskCompletionCondition", "miServiceTask");
    }

    private void verifyEventsOnAutomaticMultiInstanceWithCompletionCondition(String processDefinitionKey,
                                                                             String elementId) {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(processDefinitionKey);

        List<BPMNActivityStartedEvent> startedEvents = localEventSource.getEvents(BPMNActivityStartedEvent.class)
                .stream().filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList());

        List<BPMNActivityCompletedEvent> completedEvents = localEventSource.getEvents(BPMNActivityCompletedEvent.class)
                .stream().filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList());

        List<BPMNActivityCancelledEvent> cancelledEvents = localEventSource.getEvents(BPMNActivityCancelledEvent.class)
                .stream().filter(event -> elementId.equals(event.getEntity().getElementId()))
                .collect(Collectors.toList());

        // in some cases, the execution is scheduled in the agenda but it get cancelled even before starting
        assertThat(startedEvents.size()).isGreaterThanOrEqualTo(2);
        assertThat(completedEvents).hasSize(2);
        assertThat(cancelledEvents).hasSize(2);

        assertThat(localEventSource.getProcessInstanceEvents())
                .extracting(RuntimeEvent::getEventType,
                            event -> ((ProcessInstance) event.getEntity()).getId())
                .containsExactlyInAnyOrder(
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                              processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                              processInstance.getId()),
                        tuple(ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                              processInstance.getId())
                );
    }

    private void verifyActivityStartedAndCompletedAreEmitted(String elementId,
                                                             Integer startedCount,
                                                             Integer completedCount) {
        assertThat(localEventSource.getEvents(BPMNActivityStartedEvent.class))
                .filteredOn(event -> elementId.equals(event.getEntity().getElementId()))
                .hasSize(startedCount);

        assertThat(localEventSource.getEvents(BPMNActivityCompletedEvent.class))
                .filteredOn(event -> elementId.equals(event.getEntity().getElementId()))
                .hasSize(completedCount);

        assertThat(localEventSource.getEvents(BPMNActivityCancelledEvent.class))
                .filteredOn(event -> elementId.equals(event.getEntity().getElementId()))
                .hasSize(0);
    }

    @Test
    public void parallelMultiInstance_should_collectOutputValues() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miParallelUserTasksOutputCollection");
        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(2);

        taskBaseRuntime.completeTask(tasks.get(0), singletonMap("meal", "pizza"));
        taskBaseRuntime.completeTask(tasks.get(1), singletonMap("meal", "pasta"));

        List<VariableInstance> variables = processBaseRuntime.getVariables(processInstance);

        assertThat(variables)
            .extracting(VariableInstance::getName,
                VariableInstance::getValue)
            .contains(
                tuple("meals",
                    asList("pizza", "pasta")));
    }

    @Test
    public void sequentialMultiInstance_should_collectOutputValues() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey("miSequentialUserTasksOutputCollection");

        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(1);
        taskBaseRuntime.completeTask(tasks.get(0), singletonMap("meal", "pizza"));

        tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(1);
        taskBaseRuntime.completeTask(tasks.get(0), singletonMap("meal", "pasta"));

        List<VariableInstance> variables = processBaseRuntime.getVariables(processInstance);

        assertThat(variables)
            .extracting(VariableInstance::getName, VariableInstance::getValue)
            .contains(tuple("meals", asList("pizza", "pasta")));
    }

}
