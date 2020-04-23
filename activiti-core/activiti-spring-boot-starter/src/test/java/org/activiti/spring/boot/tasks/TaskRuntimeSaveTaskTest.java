package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.SaveTaskPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeSaveTaskTest {
    private static final String COMPLETE_REVIEW_TASK_PROCESS = "CompleteReviewTaskProcess";

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void createStandaloneTaskAndSave() {
        // given
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("simple task")
                .withAssignee("garth")
                .build());

        // when
        taskRuntime.save(new SaveTaskPayloadBuilder().withTaskId(standAloneTask.getId()).withVariable("name", "value").build());

        // then
        List<VariableInstance> variables = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(standAloneTask.getId()).build());
        assertThat(variables).extracting(VariableInstance::getName, VariableInstance::getValue)
                             .containsExactly(tuple("name", "value"));
    }


    @Test()
    public void createStandaloneTaskAndSaveWithUnAuthorizedUser() {
        // given
        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("simple task")
                .withAssignee("garth")
                .build());

        // Complete should fail with a different user
        securityUtil.logInAs("user");

        //when
        Throwable throwable = catchThrowable(() ->
            taskRuntime.save(new SaveTaskPayloadBuilder().withTaskId(standAloneTask.getId()).withVariable("name", "value").build()));

        //then
        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);

    }

    @Test()
    public void testSaveCompleteReviewOutcomeTasksProcessWithVariables() {
        // given
        securityUtil.logInAs("user");

        Map<String,Object> startVariables = new HashMap<>();
        startVariables.put("name","");

        // when
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(COMPLETE_REVIEW_TASK_PROCESS)
                .withVariables(startVariables)
                .build());

        // complete task
        securityUtil.logInAs("garth");

        Task task1 = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        List<VariableInstance> variables = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task1.getId()).build());

        assertThat(variables).extracting(VariableInstance::getName, VariableInstance::getValue)
                             .containsExactly(tuple("name", ""));

        taskRuntime.save(new SaveTaskPayloadBuilder().withTaskId(task1.getId()).withVariable("name", "wrong").build());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task1.getId()).build());

        // reject task
        securityUtil.logInAs("user");

        Task task2 = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        List<VariableInstance> variables1 = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task2.getId()).build());

        assertThat(variables1).extracting(VariableInstance::getName, VariableInstance::getValue)
                              .containsExactly(tuple("name", "wrong"));

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task2.getId()).withVariable("approved", false).build());

        // fix task
        securityUtil.logInAs("garth");

        Task task3 = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        taskRuntime.save(new SaveTaskPayloadBuilder().withTaskId(task3.getId()).withVariable("name", "correct").build());

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task3.getId()).build());

        // approve task
        securityUtil.logInAs("user");

        Task task4 = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent().get(0);

        List<VariableInstance> variables2 = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task4.getId()).build());

        assertThat(variables2).extracting(VariableInstance::getName, VariableInstance::getValue)
                              .contains(tuple("name", "correct"));

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task4.getId()).withVariable("approved", true).build());

        // then process completes
        Throwable throwable = catchThrowable(() ->
                assertThat(processRuntime.processInstance(processInstance.getId())).isNull());

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);

    }

}
