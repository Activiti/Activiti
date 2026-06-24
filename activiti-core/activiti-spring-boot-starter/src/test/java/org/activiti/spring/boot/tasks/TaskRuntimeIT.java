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
package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Date;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskIdentificationStrategy;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskRuntimeIT {

    private static final String INITIATOR = "user";
    private static final String TWO_TASK_PROCESS = "twoTaskProcess";

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @BeforeEach
    void setUp() {
        securityUtil.logInAs(INITIATOR);
    }

    @AfterEach
    void taskCleanUp() {
        processEngineConfiguration.getClock().reset();
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    void should_beAbleToAssignTaskToInitiatorEvenWhenInitiatorIsNotSetInStartEvent() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey("taskToInitiatorProcess").build()
        );

        //when
        Page<Task> taskPage = taskRuntime.tasks(
            Pageable.of(0, 10),
            TaskPayloadBuilder.tasksForProcess(processInstance).build()
        );

        //then
        assertThat(taskPage.getContent())
            .extracting(Task::getName, Task::getAssignee)
            .containsExactly(tuple("my-task", INITIATOR));
    }

    @Test
    void should_beAbleToStartLongValuesProcess() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey("longValuesProcess").build()
        );

        //when
        Page<Task> taskPage = taskRuntime.tasks(
            Pageable.of(0, 10),
            TaskPayloadBuilder.tasksForProcess(processInstance).build()
        );

        //then
        assertThat(taskPage.getContent())
            .extracting(Task::getName, Task::getDescription)
            .containsExactly(tuple("a".repeat(255), "a".repeat(4000)));
    }

    @Test
    void should_claimCandidateTaskFromStartedProcess() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(TWO_TASK_PROCESS).build()
        );

        securityUtil.logInAs("dean");

        //when
        Page<Task> taskPage = taskRuntime.tasks(
            Pageable.of(0, 10),
            TaskPayloadBuilder.tasksForProcess(processInstance).build()
        );

        assertThat(taskPage.getContent()).hasSize(1);

        Task availableTask = taskPage.getContent().getFirst();
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(availableTask.getId()).build());

        Page<Task> claimedTaskPage = taskRuntime.tasks(
            Pageable.of(0, 10),
            TaskPayloadBuilder.tasksForProcess(processInstance).build()
        );

        //then
        assertThat(taskPage.getContent())
            .extracting(Task::getName, Task::getAssignee, Task::getStatus)
            .containsExactly(tuple("User Task", null, Task.TaskStatus.CREATED));

        assertThat(claimedTask.getId()).isEqualTo(availableTask.getId());
        assertThat(claimedTask.getAssignee()).isEqualTo("dean");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        assertThat(claimedTaskPage.getContent())
            .extracting(Task::getId, Task::getAssignee, Task::getStatus)
            .containsExactly(tuple(availableTask.getId(), "dean", Task.TaskStatus.ASSIGNED));
    }

    @Test
    void should_notReclaimTaskWhenAlreadyAssignedToAuthenticatedUser() {
        //given
        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder.start().withProcessDefinitionKey(TWO_TASK_PROCESS).build()
        );

        securityUtil.logInAs("dean");

        Page<Task> taskPage = taskRuntime.tasks(
            Pageable.of(0, 10),
            TaskPayloadBuilder.tasksForProcess(processInstance).build()
        );

        assertThat(taskPage.getContent()).hasSize(1);

        Task availableTask = taskPage.getContent().getFirst();

        Task initiallyClaimedTask = taskRuntime.claim(
            TaskPayloadBuilder.claim().withTaskId(availableTask.getId()).build()
        );

        //when
        Task claimedAgainTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(availableTask.getId()).build());

        //then
        assertThat(initiallyClaimedTask.getId()).isEqualTo(availableTask.getId());
        assertThat(initiallyClaimedTask.getAssignee()).isEqualTo("dean");
        assertThat(initiallyClaimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        assertThat(claimedAgainTask.getId()).isEqualTo(initiallyClaimedTask.getId());
        assertThat(claimedAgainTask.getAssignee()).isEqualTo("dean");
        assertThat(claimedAgainTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    void should_rejectClaimWhenTaskAlreadyClaimedByAnotherUser() {
        //given
        Task createdTask = taskRuntime.create(
            TaskPayloadBuilder.create().withName("claim-test-task").withCandidateUsers("dean").build()
        );

        securityUtil.logInAs("dean");

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(createdTask.getId()).build());

        securityUtil.logInAs(INITIATOR);

        //when
        Throwable thrown = catchThrowable(() ->
            taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(createdTask.getId()).build())
        );

        Task persistedTask = taskRuntime.task(createdTask.getId());

        //then
        assertThat(claimedTask.getAssignee()).isEqualTo("dean");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "The task was already claimed, the assignee of this task needs to release it first for you to claim it"
            );

        assertThat(persistedTask.getId()).isEqualTo(createdTask.getId());
        assertThat(persistedTask.getAssignee()).isEqualTo("dean");
        assertThat(persistedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(createdTask.getId()).build());
        assertThat(deletedTask.getId()).isEqualTo(createdTask.getId());
        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.CANCELLED);
    }

    @Test
    void should_returnAssignedTaskBeforeTryingCandidateTasks_whenSelectingNextTask() {
        //given
        securityUtil.logInAs("dean");
        Task assignedTask = taskRuntime.create(
            TaskPayloadBuilder.create()
                .withName("assigned-task")
                .withAssignee("dean")
                .withCandidateUsers("dean")
                .build()
        );
        Task candidateTask = taskRuntime.create(
            TaskPayloadBuilder.create().withName("candidate-task").withCandidateUsers("dean").build()
        );

        //when
        Task nextTask = taskRuntime.nextTask(TaskIdentificationStrategy.CLAIM_BEFORE_OPEN_OLDEST_FIRST);
        Task persistedCandidateTask = taskRuntime.task(candidateTask.getId());

        //then
        assertThat(nextTask.getId()).isEqualTo(assignedTask.getId());
        assertThat(nextTask.getAssignee()).isEqualTo("dean");
        assertThat(nextTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(persistedCandidateTask.getAssignee()).isNull();
        assertThat(persistedCandidateTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(assignedTask.getId()).build());
        taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(candidateTask.getId()).build());
    }

    @Test
    void should_claimOldestCandidateTask_whenNoAssignedTaskExists() {
        //given
        securityUtil.logInAs("dean");

        Date firstTaskTime = new Date(1_000_000_000_000L);
        processEngineConfiguration.getClock().setCurrentTime(firstTaskTime);
        Task oldestCandidateTask = taskRuntime.create(
            TaskPayloadBuilder.create().withName("oldest-candidate-task").withCandidateUsers("dean").build()
        );

        Date secondTaskTime = new Date(firstTaskTime.getTime() + 60_000L);
        processEngineConfiguration.getClock().setCurrentTime(secondTaskTime);
        Task newerCandidateTask = taskRuntime.create(
            TaskPayloadBuilder.create().withName("newer-candidate-task").withCandidateUsers("dean").build()
        );

        //when
        Task nextTask = taskRuntime.nextTask(TaskIdentificationStrategy.CLAIM_BEFORE_OPEN_OLDEST_FIRST);

        //then
        assertThat(nextTask.getId()).isEqualTo(oldestCandidateTask.getId());
        assertThat(nextTask.getAssignee()).isEqualTo("dean");
        assertThat(nextTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(nextTask.getId()).build());
        taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(newerCandidateTask.getId()).build());
    }

    @Test
    void should_defaultToClaimBeforeOpenOldestFirst_whenStrategyIsNull() {
        //given
        securityUtil.logInAs("dean");
        Task oldestCandidateTask = taskRuntime.create(
            TaskPayloadBuilder.create().withName("oldest-candidate-task").withCandidateUsers("dean").build()
        );

        //when
        Task nextTask = taskRuntime.nextTask(null);

        //then
        assertThat(nextTask.getId()).isEqualTo(oldestCandidateTask.getId());
        assertThat(nextTask.getAssignee()).isEqualTo("dean");
        assertThat(nextTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(nextTask.getId()).build());
    }
}
