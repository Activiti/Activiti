package org.activiti.spring.boot.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.Task.TaskStatus;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeCompleteTaskTest {

    private static final String TWOTASK_PROCESS = "twoTaskProcess";

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void createStandaloneTaskAndComplete() {

        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("simple task")
                .withAssignee("garth")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("garth");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
        assertThat(completedTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);


    }


    @Test
    public void createStandaloneTaskandCompleteWithUnAuthorizedUser() {

        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("simple task")
                .withAssignee("garth")
                .build());

        // the owner should be able to see the created task
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("garth");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        // Complete should fail with a different user
        securityUtil.logInAs("user");

        //when
        //then
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build()));
    }

    @Test
    public void completeProcessTaskAndCheckReturnedTaskAndVariables() {

        securityUtil.logInAs("user");

        Map<String,Object> startVariables = new HashMap<>();
        startVariables.put("start1","start1");
        startVariables.put("start2","start2");

        //when
        ProcessInstance twoTaskInstance = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(TWOTASK_PROCESS)
                .withVariables(startVariables)
                .build());

        //both tasks should have same variables
        List<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10),TaskPayloadBuilder.tasks().build()).getContent();
        List<VariableInstance> variables;

        for (Task task : tasks) {
            variables = taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task.getId()).build());
            assertThat(variables)
                       .extracting(VariableInstance::getName, VariableInstance::getValue)
                       .containsExactly(
                               tuple("start1", "start1"),
                               tuple("start2", "start2"));

        }

        Task task = tasks.get(0);

        //claim task
        Task claimTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(claimTask)
        .extracting(Task::getStatus,
                    Task::getOwner,
                    Task::getAssignee,
                    Task::getName,
                    Task::getDescription,
                    Task::getCreatedDate,
                    Task::getDueDate,
                    Task::getPriority,
                    Task::getProcessDefinitionId,
                    Task::getProcessInstanceId,
                    Task::getParentTaskId,
                    Task::getFormKey,
                    Task::getProcessDefinitionVersion)
        .containsExactly(
                      TaskStatus.ASSIGNED,
                      task.getOwner(),
                      "user",
                      task.getName(),
                      task.getDescription(),
                      task.getCreatedDate(),
                      task.getDueDate(),
                      task.getPriority(),
                      task.getProcessDefinitionId(),
                      task.getProcessInstanceId(),
                      task.getParentTaskId(),
                      task.getFormKey(),
                      task.getProcessDefinitionVersion());



        //complete one task and change var

        Task completeTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).withVariable("start1","modagainstart1").build());

        assertThat(completeTask)
            .isNotNull()
            .extracting(Task::getStatus,
                        Task::getOwner,
                        Task::getAssignee,
                        Task::getName,
                        Task::getDescription,
                        Task::getCreatedDate,
                        Task::getDueDate,
                        Task::getClaimedDate,
                        Task::getPriority,
                        Task::getProcessDefinitionId,
                        Task::getProcessInstanceId,
                        Task::getParentTaskId,
                        Task::getFormKey,
                        Task::getProcessDefinitionVersion)
            .containsExactly(
                        TaskStatus.COMPLETED,
                        task.getOwner(),
                        claimTask.getAssignee(),
                        task.getName(),
                        task.getDescription(),
                        task.getCreatedDate(),
                        task.getDueDate(),
                        claimTask.getClaimedDate(),
                        task.getPriority(),
                        task.getProcessDefinitionId(),
                        task.getProcessInstanceId(),
                        task.getParentTaskId(),
                        task.getFormKey(),
                        task.getProcessDefinitionVersion());

        //after completion of the process variable start1 should updated
        assertThat(processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(twoTaskInstance).build()))
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsExactly(
                        tuple("start1", "modagainstart1"),
                        tuple("start2", "start2"));

    }
}
