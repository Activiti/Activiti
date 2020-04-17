package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Date;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeUpdateTaskTest {

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
    public void createAndUpdateStandaloneTaskForUser() {

        securityUtil.logInAs("garth");

        Task standaloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("test task update")
                                                         .withDescription("test task update description")
                                                         .withDueDate(new Date())
                                                         .withPriority(50)
                                                         .withAssignee("garth")
                                                         .build());

        assertThat(RuntimeTestConfiguration.createdTasks).contains(standaloneTask.getId());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).containsOnly(standaloneTask)
                .extracting("status",
                            "assignee")
                .contains(tuple(Task.TaskStatus.ASSIGNED,
                                "garth"));

        final Task updatedTask = taskRuntime.update(TaskPayloadBuilder.update()
                                                            .withTaskId(standaloneTask.getId())
                                                            .withName(standaloneTask.getName() + " [UPDATED]")
                                                            .withPriority(60)
                                                            .withDueDate(new Date())
                                                            .withDescription(standaloneTask.getDescription() + " [UPDATED]")
                                                            .build());
        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(RuntimeTestConfiguration.updatedTasks).contains(updatedTask.getId());
        assertThat(tasks.getTotalItems()).isEqualTo(1);
        assertThat(tasks.getContent())
                .filteredOn("status",
                            Task.TaskStatus.ASSIGNED)
                .containsOnly(updatedTask);
    }

    @Test
    public void createClaimAndUpdateStandaloneTask() {

        securityUtil.logInAs("garth");
        // create
        Task standaloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("test task update")
                                                         .withDescription("test task update description")
                                                         .withDueDate(new Date())
                                                         .withPriority(50)
                                                         .withCandidateUsers("garth")
                                                         .build());

        assertThat(RuntimeTestConfiguration.createdTasks).contains(standaloneTask.getId());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);
        assertThat(tasks.getContent())
                .extracting("status",
                            "id")
                .contains(tuple(Task.TaskStatus.CREATED,
                                standaloneTask.getId()));

        final UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder.update()
                .withTaskId(standaloneTask.getId())
                .withName(standaloneTask.getName() + " [UPDATED]")
                .withPriority(60)
                .withDueDate(new Date())
                .withDescription(standaloneTask.getDescription() + " [UPDATED]")
                .build();

        // try update
        Throwable thrown = catchThrowable(() -> taskRuntime.update(updateTaskPayload)); // task should be claimed before be updated
        assertThat(thrown)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("You cannot update a task where you are not the assignee");

        // claim
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(standaloneTask.getId()).build());

        // update
        final Task updatedTask = taskRuntime.update(updateTaskPayload);
        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(RuntimeTestConfiguration.updatedTasks).contains(updatedTask.getId());
        assertThat(tasks.getContent())
                .extracting("status",
                            "id")
                .contains(tuple(Task.TaskStatus.ASSIGNED,
                                standaloneTask.getId()));
    }

    @Test
    public void createClaimAndAdminUpdateStandaloneTask() {

        securityUtil.logInAs("garth");
        // create
        Task standaloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("test task update")
                                                         .withDescription("test task update description")
                                                         .withDueDate(new Date())
                                                         .withPriority(50)
                                                         .build());

        assertThat(RuntimeTestConfiguration.createdTasks).contains(standaloneTask.getId());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);
        assertThat(tasks.getContent())
                .extracting("status",
                            "id")
                .contains(tuple(Task.TaskStatus.CREATED,
                                standaloneTask.getId()));

        final UpdateTaskPayload updateTaskPayload = TaskPayloadBuilder.update()
                .withTaskId(standaloneTask.getId())
                .withName(standaloneTask.getName() + " [UPDATED]")
                .withPriority(60)
                .withDueDate(new Date())
                .withDescription(standaloneTask.getDescription() + " [UPDATED]")
                .build();

        // admin should update a task
        securityUtil.logInAs("admin");
        final Task updatedTask = taskAdminRuntime.update(updateTaskPayload);
        tasks = taskAdminRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(RuntimeTestConfiguration.updatedTasks).contains(updatedTask.getId());
        assertThat(tasks.getContent())
                .extracting("id",
                            "name",
                            "description",
                            "priority"
                            )
                .contains(tuple(standaloneTask.getId(),
                                updateTaskPayload.getName(),
                                updateTaskPayload.getDescription(),
                                updateTaskPayload.getPriority()
                                ));
    }


}
