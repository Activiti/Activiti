package org.activiti.spring.boot.tasks;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.impl.TaskImpl;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeCandidatesTest {

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

        assertThat(RuntimeTestConfiguration.taskCandidateUserRemovedEvents.size()).isEqualTo(1);
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

        assertThat(RuntimeTestConfiguration.taskCandidateUserAddedEvents.size()).isEqualTo(2);
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
        clearTaskCandidateEvents();
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


        assertThat(RuntimeTestConfiguration.taskCandidateGroupAddedEvents.size()).isEqualTo(1);
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

        assertThat(RuntimeTestConfiguration.taskCandidateGroupRemovedEvents.size()).isEqualTo(1);
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

    private void clearTaskCandidateEvents() {
        RuntimeTestConfiguration.taskCandidateUserRemovedEvents.clear();
        RuntimeTestConfiguration.taskCandidateUserAddedEvents.clear();
        RuntimeTestConfiguration.taskCandidateGroupAddedEvents.clear();
        RuntimeTestConfiguration.taskCandidateGroupRemovedEvents.clear();
    }

}
