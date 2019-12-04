package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
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
    public void createStandaloneTaskAndDeleteAndAddUserCandidates() {
        
        RuntimeTestConfiguration.taskCandidateUserRemovedEvents.clear();
        RuntimeTestConfiguration.taskCandidateUserAddedEvents.clear();
        
        securityUtil.logInAs("garth");

        Task createTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("task for dean")
                .withAssignee("dean") //but he should still be assigned the task
                .build());

        // Check the task should be visible for dean
        securityUtil.logInAs("dean");

        // the target user should be able to see the task as well
        Task task = taskRuntime.task(createTask.getId());
        assertThat(task.getAssignee()).isEqualTo("dean");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        
        List<String> userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).isNotNull();
        assertThat(userCandidates.size()).isEqualTo(1);
        
        taskRuntime.deleteCandidateUsers(TaskPayloadBuilder
                                         .deleteCandidateUsers()
                                         .withTaskId(task.getId())
                                         .withCandidateUser("garth")
                                         .build());
        
        assertThat(RuntimeTestConfiguration.taskCandidateUserRemovedEvents.size()).isEqualTo(1);
        assertThat(RuntimeTestConfiguration.taskCandidateUserRemovedEvents)
        .extracting(event -> event.getEntity().getUserId())
        .contains("garth");
        
              
        userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).isNotNull();
        assertThat(userCandidates.size()).isEqualTo(0);
           
           
                
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
        
        userCandidates = taskRuntime.userCandidates(createTask.getId());
        assertThat(userCandidates).isNotNull();
        assertThat(userCandidates.size()).isEqualTo(1);
           
     }



}
