package org.activiti.spring.conformance.set2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserTaskCandidateDeleteRuntimeTest {

    private final String processKey = "usertaskwi-09c219d1-61fa-4b10-bacd-22af08a9ce81";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    public void cleanUp() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }


    @Test
    public void shouldFailOnCandidateDelete() {

        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build());

        //then
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
        assertThat(processInstance.getBusinessKey()).isEqualTo("my-business-key");
        assertThat(processInstance.getName()).isEqualTo("my-process-instance-name");

        // I should be able to get the process instance from the Runtime because it is still running
        ProcessInstance processInstanceById = processRuntime.processInstance(processInstance.getId());

        assertThat(processInstanceById).isEqualTo(processInstance);

        // I should get a task for User1
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

        Task task = tasks.getContent().get(0);

        Task taskById = taskRuntime.task(task.getId());

        assertThat(taskById.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


        assertThat(task).isEqualTo(taskById);

        assertThat(task.getAssignee()).isNull();


        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED);

        RuntimeTestConfiguration.collectedEvents.clear();

        Throwable throwable = catchThrowable(() -> taskRuntime.delete(TaskPayloadBuilder.delete().withTaskId(task.getId()).build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You cannot delete a task where you are not the assignee/owner");
    }

    @AfterEach
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
        RuntimeTestConfiguration.collectedEvents.clear();
    }

}
