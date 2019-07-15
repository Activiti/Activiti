package org.activiti.spring.boot.tasks;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeStandaloneTaskTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private TaskRuntimeEventListeners taskRuntimeEventListeners;

    @After
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void createStandaloneTaskForSalaboy() {

        securityUtil.logInAs("salaboy");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("cure Skipper")
                .withAssignee("salaboy")
                .build());

        assertThat(RuntimeTestConfiguration.createdTasks).contains(standAloneTask.getId());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("salaboy");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(task.isStandalone()).isTrue();

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
    public void shouldEmmitEventForStandAloneTaskDeletion() {
        //given
        securityUtil.logInAs("salaboy");

        Task firstTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("First task")
                                                         .withAssignee("salaboy")
                                                         .build());
        Task secondTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("Second task")
                                                         .withAssignee("salaboy")
                                                         .build());

        //when
        taskRuntime.delete(TaskPayloadBuilder
                                   .delete()
                                   .withTaskId(secondTask.getId())
                                   .build());

        //then
        assertThat(taskRuntimeEventListeners.getCancelledTasks())
                .extracting(Task::getId, Task::getName)
                .contains(tuple(secondTask.getId(), secondTask.getName()))
                .doesNotContain(tuple(firstTask.getId(), firstTask.getName()));
    }

    @Test
    public void createStandaloneTaskForGroup() {

        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("find Lucien Sanchez")
                .withCandidateGroup("doctor")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getId()).isEqualTo(standAloneTask.getId());
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        assertThat(task.isStandalone()).isTrue();

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(claimedTask.getAssignee()).isEqualTo("garth");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

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
    public void createStandaloneTaskFailWithEmptyName() {

        securityUtil.logInAs("salaboy");

        //when
        Throwable throwable = catchThrowable(() -> taskRuntime.create(TaskPayloadBuilder.create()
                                                                      .withAssignee("salaboy")
                                                                      .build()));

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);


        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(0);
    }

}
