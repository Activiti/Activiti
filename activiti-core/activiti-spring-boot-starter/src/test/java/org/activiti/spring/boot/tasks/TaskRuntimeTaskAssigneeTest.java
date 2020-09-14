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
import static org.assertj.core.api.Assertions.catchThrowable;

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
public class TaskRuntimeTaskAssigneeTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private TaskAdminRuntime taskAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void aCreateStandaloneTaskForAnotherAssignee() {
        securityUtil.logInAs("garth");

        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("task for dean")
                .withAssignee("dean") //but he should still be assigned the task
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("dean");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);


        // Now the task should be visible for dean
        securityUtil.logInAs("dean");

        // the target user should be able to see the task as well
        tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("dean");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder
                .delete()
                .withTaskId(task.getId())
                .withReason("test clean up")
                .build());

        assertThat(deletedTask).isNotNull();
        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.CANCELLED);

        tasks = taskRuntime.tasks(Pageable.of(0,
                50));
        assertThat(tasks.getContent()).hasSize(0);

    }


    @Test
    public void createStandaloneTaskForGroupAndClaim() {

        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("doctor")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("garth");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    public void createStandaloneTaskForGroupAndAdminAssignUser() {

        securityUtil.logInAs("garth");

        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("doctor")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);

        Task task = tasks.getContent().get(0);
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


        securityUtil.logInAs("admin");
        Task assignedTask = taskAdminRuntime.assign(TaskPayloadBuilder
                                                  .assign()
                                                  .withTaskId(task.getId())
                                                  .withAssignee("garth")
                                                  .build());
        assertThat(assignedTask.getAssignee()).isEqualTo("garth");
        assertThat(assignedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        securityUtil.logInAs("garth");
        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(tasks.getContent()).hasSize(1);
        task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("garth");

        taskRuntime.delete(TaskPayloadBuilder
                                              .delete()
                                              .withTaskId(task.getId())
                                              .withReason("test clean up")
                                              .build());
    }

    @Test
    public void createStandaloneTaskForUsersAndAdminReassignUser() {

        securityUtil.logInAs("garth");

        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateUsers("dean")
                .withCandidateUsers("garth")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);

        Task task = tasks.getContent().get(0);
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


        //Check that admin may assign a user to the task without assignee
        securityUtil.logInAs("admin");
        Task assignedTask = taskAdminRuntime.assign(TaskPayloadBuilder
                                                  .assign()
                                                  .withTaskId(task.getId())
                                                  .withAssignee("garth")
                                                  .build());
        assertThat(assignedTask.getAssignee()).isEqualTo("garth");
        assertThat(assignedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        //Check that admin may reassign a user to the task when assignee is present
        assignedTask = taskAdminRuntime.assign(TaskPayloadBuilder
                                               .assign()
                                               .withTaskId(task.getId())
                                               .withAssignee("dean")
                                               .build());
        assertThat(assignedTask.getAssignee()).isEqualTo("dean");
        assertThat(assignedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);


        securityUtil.logInAs("dean");
        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(tasks.getContent()).hasSize(1);
        task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("dean");

        taskRuntime.delete(TaskPayloadBuilder
                                              .delete()
                                              .withTaskId(task.getId())
                                              .withReason("test clean up")
                                              .build());
    }

    @Test
    public void userCanReassignClaimedTaskToCandidateUsers() {

        String taskId = createTask("garth");
        //assign the new task
        userAssignTask(taskId,"garth", "dean");
        //reassign task to candidate
        userAssignTask(taskId,"dean", "garth");

        test_shouldReturnIllegalStateExceptionWhenUserIsNotAssigneeOrOwner(taskId,"dean", "garth");

        cleanUp(taskId);
    }
    
    private String createTask(String user){
        securityUtil.logInAs(user);

        taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateUsers("dean")
                .withCandidateUsers("garth")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);

        Task task = tasks.getContent().get(0);
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        return task.getId();
    }
    
    private void userAssignTask(String taskId, String user, String assignee){
        securityUtil.logInAs(user);
        Task assignedTask = taskRuntime.assign(TaskPayloadBuilder
                .assign()
                .withTaskId(taskId)
                .withAssignee(assignee)
                .build());
        assertThat(assignedTask.getAssignee()).isEqualTo(assignee);
        assertThat(assignedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }
    
    private void cleanUp(String taskId){
        securityUtil.logInAs("admin");
        taskAdminRuntime.delete(TaskPayloadBuilder
                .delete()
                .withTaskId(taskId)
                .withReason("test clean up")
                .build());
    }
    
    private void test_shouldReturnIllegalStateExceptionWhenUserIsNotAssigneeOrOwner (String taskId, String user, String assignee){
        securityUtil.logInAs(user);

        Throwable thrown = catchThrowable(() -> taskRuntime.assign(TaskPayloadBuilder
                .assign()
                .withTaskId(taskId)
                .withAssignee(assignee)
                .build()));

        //then
        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("You cannot update a task where you are not the assignee");
    }
}
