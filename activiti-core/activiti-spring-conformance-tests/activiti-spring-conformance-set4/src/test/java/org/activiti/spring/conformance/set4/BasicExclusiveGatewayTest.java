package org.activiti.spring.conformance.set4;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.activiti.test.operations.ProcessOperations;
import org.activiti.test.operations.TaskOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.activiti.test.matchers.BPMNStartEventMatchers.startEvent;
import static org.activiti.test.matchers.ExclusiveGatewayMatchers.exclusiveGateway;
import static org.activiti.test.matchers.ProcessInstanceMatchers.processInstance;
import static org.activiti.test.matchers.ProcessTaskMatchers.taskWithName;
import static org.activiti.test.matchers.SequenceFlowMatchers.sequenceFlow;
import static org.activiti.test.matchers.TaskMatchers.task;
import static org.activiti.test.matchers.TaskMatchers.withAssignee;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BasicExclusiveGatewayTest {

    private static final String PROCESS_KEY = "basicexclu-15cdd4ac-ff4d-4925-9b4e-87ea77528613";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private ProcessOperations processOperations;

    @Autowired
    private TaskOperations taskOperations;

    @Test
    public void shouldCreateAndCompleteATaskAndDontSeeNext() {
        securityUtil.logInAs("user1");

        //given
        ProcessInstance processInstance = processOperations.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(PROCESS_KEY)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build())

        //then
                .expectFields(processInstance().status(ProcessInstance.ProcessInstanceStatus.RUNNING),
                              processInstance().name("my-process-instance-name"),
                              processInstance().businessKey("my-business-key"))

                .expect(processInstance().hasTask("Task 1 User 1",
                                                  Task.TaskStatus.ASSIGNED,
                                                  withAssignee("user1")))
                .expectEvents(processInstance().hasBeenStarted(),
                              startEvent("StartEvent_1").hasBeenStarted(),
                              startEvent("StartEvent_1").hasBeenCompleted(),
                              sequenceFlow("SequenceFlow_1035s34").hasBeenTaken(),
                              taskWithName("Task 1 User 1").hasBeenCreated(),
                              taskWithName("Task 1 User 1").hasBeenAssigned()
                )
                .andReturn();

        // I should be able to get the process instance from the Runtime because it is still running
        ProcessInstance processInstanceById = processRuntime.processInstance(processInstance.getId());
        assertThat(processInstanceById).isEqualTo(processInstance);

        // I should get a task for User1
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(1);

        Task task = tasks.getContent().get(0);
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(task.getAssignee()).isEqualTo("user1");

        //given
        taskOperations.complete(TaskPayloadBuilder
                                .complete()
                                .withTaskId(task.getId())
                                .build())
        //then
                .expectEvents(task().hasBeenCompleted(),
                              sequenceFlow("SequenceFlow_0pdm5j0").hasBeenTaken(),
                              exclusiveGateway("ExclusiveGateway_1ri35t5").hasBeenStarted(),
                              exclusiveGateway("ExclusiveGateway_1ri35t5").hasBeenCompleted(),
                              sequenceFlow("SequenceFlow_1tut9mk").hasBeenTaken(),
                              taskWithName("Task 2 User 1").hasBeenCreated(),
                              taskWithName("Task 2 User 1").hasBeenAssigned())
                .expect(processInstance().hasTask("Task 2 User 1",
                                                  Task.TaskStatus.ASSIGNED,
                                                  withAssignee("user1")));

        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(1);

        task = tasks.getContent().get(0);
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(task.getAssignee()).isEqualTo("user1");
    }

    @AfterEach
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
    }

}
