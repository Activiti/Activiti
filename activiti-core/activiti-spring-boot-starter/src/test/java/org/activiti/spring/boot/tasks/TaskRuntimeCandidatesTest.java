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

import java.util.List;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeCandidatesTest {

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
    public void should_addAndRemoveCandidateUser() {

        RuntimeTestConfiguration.taskCandidateUserRemovedEvents.clear();
        RuntimeTestConfiguration.taskCandidateUserAddedEvents.clear();

        securityUtil.logInAs("garth");

        Task createTask = taskRuntime.create(TaskPayloadBuilder.create()
            .withName("task for dean")
            .withCandidateUsers("garth")
            .withAssignee("dean") //but he should still be assigned the task
            .build());

        // Check the task should be visible for dean
        securityUtil.logInAs("dean");

        // the target user should be able to see the task as well
        TaskImpl task = (TaskImpl) taskRuntime.task(createTask.getId());
        assertThat(task.getAssignee()).isEqualTo("dean");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        List<String> userCandidatesOnTask = task.getCandidateUsers();
        assertThat(userCandidatesOnTask).hasSize(1);

        List<String> userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).hasSize(1);

        taskRuntime.deleteCandidateUsers(TaskPayloadBuilder
                                         .deleteCandidateUsers()
                                         .withTaskId(task.getId())
                                         .withCandidateUser("garth")
                                         .build());

        assertThat(RuntimeTestConfiguration.taskCandidateUserRemovedEvents).hasSize(1);
        assertThat(RuntimeTestConfiguration.taskCandidateUserRemovedEvents)
        .extracting(event -> event.getEntity().getUserId())
        .contains("garth");

        task = (TaskImpl) taskRuntime.task(createTask.getId());
        userCandidatesOnTask = task.getCandidateUsers();
        assertThat(userCandidatesOnTask).isEmpty();

        userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).isEmpty();


        taskRuntime.addCandidateUsers(TaskPayloadBuilder
                                      .addCandidateUsers()
                                      .withTaskId(task.getId())
                                      .withCandidateUser("garth")
                                      .build());

        assertThat(RuntimeTestConfiguration.taskCandidateUserAddedEvents).hasSize(2);
        assertThat(RuntimeTestConfiguration.taskCandidateUserAddedEvents)
        .extracting(event -> event.getEntity().getUserId())
        .contains("garth",
                  "garth");

        task = (TaskImpl) taskRuntime.task(createTask.getId());
        userCandidatesOnTask = task.getCandidateUsers();
        assertThat(userCandidatesOnTask).hasSize(1);

        userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).hasSize(1);
    }

    @Test
    public void should_addAndRemoveCandidateGroup() {
        RuntimeTestConfiguration.clearTaskCandidateEvents();
        securityUtil.logInAs("garth");

        Task createTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                     .withName("task for dean")
                                                     .withAssignee("garth")
                                                     .build());


        taskRuntime.addCandidateGroups(TaskPayloadBuilder
                                               .addCandidateGroups()
                                               .withTaskId(createTask.getId())
                                               .withCandidateGroup("test")
                                               .build());


        assertThat(RuntimeTestConfiguration.taskCandidateGroupAddedEvents).hasSize(1);
        assertThat(RuntimeTestConfiguration.taskCandidateGroupAddedEvents)
                .extracting(event -> event.getEntity().getGroupId())
                .contains("test");

        TaskImpl task = (TaskImpl) taskRuntime.task(createTask.getId());
        List<String> groupCandidatesOnTask = task.getCandidateGroups();
        assertThat(groupCandidatesOnTask).hasSize(1);

        List<String> groupCandidates = taskRuntime.groupCandidates(createTask.getId());
        assertThat(groupCandidates).hasSize(1);

        taskRuntime.deleteCandidateGroups(TaskPayloadBuilder
                                                 .deleteCandidateGroups()
                                                 .withTaskId(task.getId())
                                                 .withCandidateGroup("test")
                                                 .build());

        assertThat(RuntimeTestConfiguration.taskCandidateGroupRemovedEvents).hasSize(1);
        assertThat(RuntimeTestConfiguration.taskCandidateGroupRemovedEvents)
                .extracting(event -> event.getEntity().getGroupId())
                .contains("test");

        task = (TaskImpl) taskRuntime.task(createTask.getId());
        groupCandidatesOnTask = task.getCandidateGroups();
        assertThat(groupCandidatesOnTask).isEmpty();

        groupCandidates = taskRuntime.groupCandidates(createTask.getId());
        assertThat(groupCandidates).isEmpty();

        taskRuntime.addCandidateGroups(TaskPayloadBuilder
                                               .addCandidateGroups()
                                               .withTaskId(createTask.getId())
                                               .withCandidateGroup("test")
                                               .build());

        task = (TaskImpl) taskRuntime.task(createTask.getId());
        groupCandidatesOnTask = task.getCandidateGroups();
        assertThat(groupCandidatesOnTask).hasSize(1);

        groupCandidates = taskRuntime.groupCandidates(createTask.getId());
        assertThat(groupCandidates).hasSize(1);
    }

}
