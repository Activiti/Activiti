package org.activiti.spring.boot;

import java.util.Arrays;

import org.activiti.runtime.api.NotFoundException;
import org.activiti.runtime.api.TaskAdminRuntime;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.identity.ActivitiUser;
import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.activiti.runtime.api.security.SecurityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private TaskAdminRuntime taskAdminRuntime;

    @Autowired
    private UserGroupManager userGroupManager;

    @Autowired
    private SecurityManager securityManager;

    private ActivitiUser salaboy;

    private ActivitiUser garth;

    private ActivitiUser admin;

    @Before
    public void init() {
        if (!userGroupManager.exists("admin")) {
            admin = userGroupManager.create("admin",
                                              "password",
                                              Arrays.asList("adminGroup"),
                                              Arrays.asList("admin"));
        } else {
            admin = userGroupManager.loadUser("admin");
        }
        if (!userGroupManager.exists("salaboy")) {
            salaboy = userGroupManager.create("salaboy",
                                              "password",
                                              Arrays.asList("activitiTeam"),
                                              Arrays.asList("user"));
        } else {
            salaboy = userGroupManager.loadUser("salaboy");
        }
        if (!userGroupManager.exists("garth")) {
            garth = userGroupManager.create("Garth",
                                            "darkplace",
                                            Arrays.asList("doctor"),
                                            Arrays.asList("user"));
        } else {
            garth = userGroupManager.loadUser("garth");
        }
    }

    @After
    public void tearDown() {
        // Created Task clean up

        securityManager.authorize(admin);

        Page<Task> tasks = taskAdminRuntime.tasks(Pageable.of(0,
                                                              50));
        for (Task t : tasks.getContent()) {
            taskAdminRuntime.delete(TaskPayloadBuilder.delete().withTaskId(t.getId()).withReason("test clean up").build());
        }
    }

    @Test
    public void createStandaloneTaskForGarth() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("cure Skipper")
                                                         .withAssignee(garth.getUsername())
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    public void createStandaloneTaskForGroup() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("find Lucien Sanchez")
                                                         .withGroup("doctor")
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        // salaboy doesn't belong to the doctor's group so no task for him
        securityManager.authorize(salaboy);

        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));
        assertThat(tasks.getContent()).hasSize(0);
    }

    @Test
    public void createStandaloneTaskWithNoCandidates() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("task with no candidates besides owner")
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
    }

    @Test
    @Ignore
    // @TODO: The task owner should be able to see the task even if he/she is not assigned or belong to the target groups
    public void createStandaloneTaskForAnotherAssignee() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("task for salaboy")
                                                         .withAssignee(salaboy.getUsername())
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        securityManager.authorize(salaboy);
        // the target user should be able to see the task as well
        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(tasks.getContent()).hasSize(1);
        task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    public void createStandaloneTaskForGroupAndClaim() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("group task")
                                                         .withGroup("doctor")
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(garth.getUsername());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    public void createStandaloneTaskForGroupAndClaimUnAuthorized() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("group task")
                                                         .withGroup("doctor")
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityManager.authorize(salaboy);
        Exception e = null;
        try {
            // UnAuthorized Claim
            Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void createStandaloneTaskForGroupAndClaimAuthorized() {

        securityManager.authorize(garth);

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

        securityManager.authorize(salaboy);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
    }

    @Test
    public void createStandaloneTaskForGroupAndClaimAndRelease() {

        securityManager.authorize(garth);

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

        securityManager.authorize(salaboy);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
        assertThat(releasedTask.getAssignee()).isNull();
        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
    }

    @Test
    public void createStandaloneTaskReleaseUnAuthorized() {

        securityManager.authorize(garth);

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

        Exception e = null;
        try {
            // UnAuthorized release, task is not assigned
            Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void createStandaloneTaskAndClaimAndReleaseUnAuthorized() {

        securityManager.authorize(garth);

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

        securityManager.authorize(salaboy);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        securityManager.authorize(garth);

        Exception e = null;
        try {
            // UnAuthorized release, task is assigned not to you and hence not visible anymore
            Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void createStandaloneTaskAndComplete() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("simple task")
                                                         .withAssignee(garth.getUsername())
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
        assertThat(completedTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
    }

    @Test
    public void createStandaloneTaskAndCompleteUnAuthorized() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("simple task")
                                                         .withAssignee(garth.getUsername())
                                                         .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        securityManager.authorize(salaboy);
        Exception e = null;
        try {
            // UnAuthorized Complete
            Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void createStandaloneTaskAndDelete() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("simple task")
                                                         .withAssignee(garth.getUsername())
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.DELETED);
    }

    @Test
    public void createStandaloneGroupTaskAndDeleteFail() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("simple task")
                                                         .withGroup("activitiTeam")
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        Exception e = null;
        try {
            // UnAuthorized Delete, task is not assigned
            Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void createStandaloneGroupTaskClaimAndDeleteFail() {

        securityManager.authorize(garth);

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("simple task")
                                                         .withGroup("activitiTeam")
                                                         .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

        securityManager.authorize(salaboy);

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        securityManager.authorize(garth);

        Exception e = null;
        try {
            // UnAuthorized Delete, task is not visible, because it was claimed by another user
            Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
        } catch (Exception ex) {
            e = ex;
        }
        assertThat(e).isNotNull();
        assertThat(e).isInstanceOf(NotFoundException.class);
    }

    //@TODO: add tests for
    //  - Complete task with variables
    //  - Add other users to test group and claim/release combinations
    //  - Add get/set variables tests
}
