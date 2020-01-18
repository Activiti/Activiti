package org.activiti.spring.boot.tasks;

import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.TaskAdminRuntime;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.security.SecurityManager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TaskRuntimeClaimReleaseTest {

    private static String currentTaskId;
    @Autowired
    private TaskRuntime taskRuntime;
    @Autowired
    private TaskAdminRuntime taskAdminRuntime;
    @Autowired
    private SecurityManager securityManager;

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void aCreateStandaloneTaskForGroup() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withGroup("activitiTeam")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        currentTaskId = task.getId();

    }


    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void bClaimAndRelease() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(currentTaskId).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(authenticatedUserId);
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


    }

    @Test
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void cCreateStandaloneTaskReleaseUnAuthorized() {

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withGroup("activitiTeam")
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
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void dCreateStandaloneTaskAndClaimAndReleaseUnAuthorized() {

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("group task")
                .withGroup("activitiTeam")
                .build());


        assertThat(standAloneTask.getAssignee()).isNull();
        assertThat(standAloneTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        currentTaskId = standAloneTask.getId();
    }

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void eCreateStandaloneTaskAndClaimAndReleaseUnAuthorized() {

        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(currentTaskId).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(authenticatedUserId);
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test(expected = NotFoundException.class)
    @WithUserDetails(value = "garth", userDetailsServiceBeanName = "myUserDetailsService")
    public void fCreateStandaloneTaskAndClaimAndReleaseUnAuthorized() {
        // UnAuthorized release, task is assigned not to you and hence not visible anymore
        taskRuntime.release(TaskPayloadBuilder.release().withTaskId(currentTaskId).build());

    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "myUserDetailsService")
    public void gCleanUpWithAdmin() {
        Page<Task> tasks = taskAdminRuntime.tasks(Pageable.of(0, 50));
        for (Task t : tasks.getContent()) {
            taskAdminRuntime.delete(TaskPayloadBuilder
                    .delete()
                    .withTaskId(t.getId())
                    .withReason("test clean up")
                    .build());
        }

    }
}
