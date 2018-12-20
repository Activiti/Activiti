package org.activiti.spring.boot.tasks;

import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeClaimReleaseTest {

    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private TaskAdminRuntime taskAdminRuntime;
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @After
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void createStandaloneTaskForGroup() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("activitiTeam")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        // Claim and Release
        securityUtil.logInAs("salaboy");

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("salaboy");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

    }

    @Test
    public void createStandaloneTaskReleaseUnAuthorized() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("activitiTeam")
                .build());


        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Throwable thrown = catchThrowable(() -> {
                    // UnAuthorized release, task is not assigned
                    taskRuntime.release(TaskPayloadBuilder.release().withTaskId(standAloneTask.getId()).build());
                }
        );
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }


    @Test
    public void createStandaloneTaskAndClaimAndReleaseUnAuthorized() {
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withCandidateGroup("activitiTeam")
                .build());


        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityUtil.logInAs("salaboy");


        // Claim task
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(standAloneTask.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo("salaboy");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        // UnAuthorized release, task is assigned not to you and hence not visible anymore
        securityUtil.logInAs("garth");

        Throwable throwable = catchThrowable(() ->
                taskRuntime.release(TaskPayloadBuilder.release().withTaskId(standAloneTask.getId()).build()));

        //then
        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);
    }

}
