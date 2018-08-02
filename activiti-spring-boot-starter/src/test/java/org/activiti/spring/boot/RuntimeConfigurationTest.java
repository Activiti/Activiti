package org.activiti.spring.boot;

import org.activiti.runtime.api.identity.UserGroupManager;
import org.activiti.runtime.api.security.SecurityManager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class RuntimeConfigurationTest {


    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserGroupManager userGroupManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void validatingConfigurationForUser() {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isNotBlank();

        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUserId);
        assertThat(userDetails).isNotNull();

        assertThat(userDetails.getAuthorities()).hasSize(2);

        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        assertThat(userRoles).isNotNull();
        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0)).isEqualTo("ACTIVITI_USER");
        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).isNotNull();
        assertThat(userGroups).hasSize(1);
        assertThat(userGroups.get(0)).isEqualTo("activitiTeam");
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "myUserDetailsService")
    public void validatingConfigurationForAdmin() {
        String authenticatedUserId = securityManager.getAuthenticatedUserId();
        assertThat(authenticatedUserId).isNotBlank();

        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUserId);
        assertThat(userDetails).isNotNull();

        assertThat(userDetails.getAuthorities()).hasSize(1);

        List<String> userRoles = userGroupManager.getUserRoles(authenticatedUserId);
        assertThat(userRoles).isNotNull();
        assertThat(userRoles).hasSize(1);
        assertThat(userRoles.get(0)).isEqualTo("ACTIVITI_ADMIN");
        List<String> userGroups = userGroupManager.getUserGroups(authenticatedUserId);
        assertThat(userGroups).isNotNull();
        assertThat(userGroups).hasSize(0);

    }

//    @Test
//    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
//    public void createStandaloneTaskForGroup() {
//
//
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("find Lucien Sanchez")
//                                                         .withGroup("doctor")
//                                                         .build());
//
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        // salaboy doesn't belong to the doctor's group so no task for him
//        securityManager.authenticate(salaboy);
//
//        tasks = taskRuntime.tasks(Pageable.of(0,
//                                              50));
//        assertThat(tasks.getContent()).hasSize(0);
//    }
//
//    @Test
//    public void createStandaloneTaskWithNoCandidates() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("task with no candidates besides owner")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//    }
//
//    @Test
//    public void createStandaloneTaskForAnotherAssignee() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("task for salaboy")
//                                                         .withAssignee(salaboy.getUsername())
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        securityManager.authenticate(salaboy);
//        // the target user should be able to see the task as well
//        tasks = taskRuntime.tasks(Pageable.of(0,
//                                              50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//    }
//
//    @Test
//    public void createStandaloneTaskForGroupAndClaim() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("doctor")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        assertThat(claimedTask.getAssignee()).isEqualTo(garth.getUsername());
//        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//    }
//
//    @Test
//    public void createStandaloneTaskForGroupAndClaimUnAuthorized() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("doctor")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        securityManager.authenticate(salaboy);
//        Exception e = null;
//        try {
//            // UnAuthorized Claim
//            Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(NotFoundException.class);
//    }
//
//    @Test
//    public void createStandaloneTaskForGroupAndClaimAuthorized() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        securityManager.authenticate(salaboy);
//
//        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//    }
//
//    @Test
//    public void createStandaloneTaskForGroupAndClaimAndRelease() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        securityManager.authenticate(salaboy);
//
//        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(claimedTask.getId()).build());
//        assertThat(releasedTask.getAssignee()).isNull();
//        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//    }
//
//    @Test
//    public void createStandaloneTaskReleaseUnAuthorized() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        Exception e = null;
//        try {
//            // UnAuthorized release, task is not assigned
//            Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    public void createStandaloneTaskAndClaimAndReleaseUnAuthorized() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("group task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        securityManager.authenticate(salaboy);
//
//        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        securityManager.authenticate(garth);
//
//        Exception e = null;
//        try {
//            // UnAuthorized release, task is assigned not to you and hence not visible anymore
//            Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(NotFoundException.class);
//    }
//
//    @Test
//    public void createStandaloneTaskAndComplete() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("simple task")
//                                                         .withAssignee(garth.getUsername())
//                                                         .build());
//
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
//        assertThat(completedTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);
//    }
//
//    @Test
//    public void createStandaloneTaskAndCompleteUnAuthorized() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("simple task")
//                                                         .withAssignee(garth.getUsername())
//                                                         .build());
//
//        // the owner should be able to see the created task
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        securityManager.authenticate(salaboy);
//        Exception e = null;
//        try {
//            // UnAuthorized Complete
//            Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(NotFoundException.class);
//    }
//
//    @Test
//    public void createStandaloneTaskAndDelete() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("simple task")
//                                                         .withAssignee(garth.getUsername())
//                                                         .build());
//
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isEqualTo(garth.getUsername());
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
//        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.DELETED);
//    }
//
//    @Test
//    public void createStandaloneGroupTaskAndDeleteFail() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("simple task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        Exception e = null;
//        try {
//            // UnAuthorized Delete, task is not assigned
//            Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(IllegalStateException.class);
//    }
//
//    @Test
//    public void createStandaloneGroupTaskClaimAndDeleteFail() {
//
//        securityManager.authenticate(garth);
//
//        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
//                                                         .withName("simple task")
//                                                         .withGroup("activitiTeam")
//                                                         .build());
//
//        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
//                                                         50));
//
//        assertThat(tasks.getContent()).hasSize(1);
//        Task task = tasks.getContent().get(0);
//
//        assertThat(task.getAssignee()).isNull();
//        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
//
//        securityManager.authenticate(salaboy);
//
//        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());
//        assertThat(claimedTask.getAssignee()).isEqualTo(salaboy.getUsername());
//        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
//
//        securityManager.authenticate(garth);
//
//        Exception e = null;
//        try {
//            // UnAuthorized Delete, task is not visible, because it was claimed by another user
//            Task deletedTask = taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build());
//        } catch (Exception ex) {
//            e = ex;
//        }
//        assertThat(e).isNotNull();
//        assertThat(e).isInstanceOf(NotFoundException.class);
//    }

    //@TODO: add tests for
    //  - Complete task with variables
    //  - Add other users to test group and claim/release combinations
    //  - Add get/set variables tests
    //  - Add Impersonation methods to TaskAdminRuntime
}
