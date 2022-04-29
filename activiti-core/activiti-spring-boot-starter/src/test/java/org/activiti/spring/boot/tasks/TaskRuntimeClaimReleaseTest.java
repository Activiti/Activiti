/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeClaimReleaseTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void should_ownerClaimATask_when_taskHasNotAssignOrCandidates() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("group task")
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getCandidateUsers()).isNullOrEmpty();
        assertThat(task.getCandidateGroups()).isNullOrEmpty();

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("garth");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(claimedTask.getTaskDefinitionKey()).isNull();

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
    }

    @Test
    public void should_claimAndRelease_when_userIsInCandidateGroup() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("activitiTeam")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        // Claim and Release
        securityUtil.logInAs("user");

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("user");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(claimedTask.getTaskDefinitionKey()).isNull();

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

    }

    @Test
    public void should_throwIllegalStateException_when_releaseNotClaimedAndAssignedTask() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateUsers("garth")
                .withCandidateGroup("activitiTeam")
                .build());


        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Throwable thrown = catchThrowable(() -> {
                    // UnAuthorized release, task is not assigned
                    taskRuntime.release(TaskPayloadBuilder.release().withTaskId(standAloneTask.getId()).build());
                }
        );
        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot release a task that is not claimed");;
    }

    @Test
    public void should_userViewATaskByCandidateGroup_when_itIsNotAssigned() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("group task")
                                                         .withCandidateGroup("activitiTeam")
                                                         .build());

        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityUtil.logInAs("user");
        assertThat(taskRuntime.task(standAloneTask.getId())).isNotNull();

        securityUtil.logInAs("john");
        assertThat(taskRuntime.task(standAloneTask.getId())).isNotNull();

        securityUtil.logInAs("user");

        // Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(standAloneTask.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("user");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        // UnAuthorized release, task is assigned not to you and hence not visible anymore
        securityUtil.logInAs("john");

        Throwable throwable = catchThrowable(() -> taskRuntime.task(standAloneTask.getId()));

        //then
        assertThat(throwable)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Unable to find task for the given id: " + standAloneTask.getId() + " for user: john (with groups: [activitiTeam] & with roles: [ACTIVITI_USER])");
    }

    @Test
    public void should_throwNotFoundException_when_releaseTaskNotAssignedToLoggedUser() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("activitiTeam")
                .build());

        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityUtil.logInAs("user");

        // Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(standAloneTask.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("user");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        // UnAuthorized release, task is assigned not to you and hence not visible anymore
        securityUtil.logInAs("john");

        Throwable throwable = catchThrowable(() ->
                taskRuntime.release(TaskPayloadBuilder.release().withTaskId(standAloneTask.getId()).build()));

        //then
        assertThat(throwable)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Unable to find task for the given id: " + standAloneTask.getId() + " for user: john (with groups: [activitiTeam] & with roles: [ACTIVITI_USER])");
    }

    @Test
    public void should_throwIllegalStateException_when_ownerTryToReleaseATaskAssignedToSomeOneElse() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("group task")
                                                         .withCandidateGroup("activitiTeam")
                                                         .build());


        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityUtil.logInAs("user");


        // Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(standAloneTask.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("user");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        // UnAuthorized release, task is assigned not to you and hence not visible anymore
        securityUtil.logInAs("garth");

        Throwable throwable = catchThrowable(() -> taskRuntime.release(TaskPayloadBuilder.release().withTaskId(standAloneTask.getId()).build()));

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot release a task where you are not the assignee");;
    }

}
