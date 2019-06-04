package org.activiti.spring.conformance.set3;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.activiti.steps.operations.ProcessOperations;
import org.activiti.steps.operations.TaskOperations;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.steps.matchers.BPMNStartEventMatchers.startEvent;
import static org.activiti.steps.matchers.ProcessInstanceMatchers.processInstance;
import static org.activiti.steps.matchers.ProcessTaskMatchers.task;
import static org.activiti.steps.matchers.SequenceFlowMatchers.sequenceFlow;
import static org.activiti.steps.matchers.TaskMatchers.task;
import static org.activiti.steps.matchers.TaskMatchers.withAssignee;
import static org.assertj.core.api.Assertions.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserTaskCandidateGroupAndAssigneeTest {

    private final String processKey = "usertaskas-b5300a4b-8950-4486-ba20-a8d775a3d75d";

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
                                                                          .withProcessDefinitionKey(processKey)
                                                                          .withBusinessKey("my-business-key")
                                                                          .withName("my-process-instance-name")
                                                                          .build())
                //then
                .expect(processInstance().hasStatus(ProcessInstance.ProcessInstanceStatus.RUNNING),
                        processInstance().hasName("my-process-instance-name"),
                        processInstance().hasBusinessKey("my-business-key"))
                .expect(processInstance().hasTask("Task User1",
                                                  Task.TaskStatus.ASSIGNED,
                                                  withAssignee("user1")))
                .expect(processInstance().hasBeenStarted(),
                        startEvent("StartEvent_1").hasBeenStarted(),
                        startEvent("StartEvent_1").hasBeenCompleted(),
                        sequenceFlow("SequenceFlow_1uccvwa").hasBeenTaken(),
                        task("Task User1").hasBeenCreated(),
                        task("Task User1").hasBeenAssigned()
                )
                .andReturn();

        // I should be able to get the process instance from the Runtime because it is still running
        ProcessInstance processInstanceById = processRuntime.processInstance(processInstance.getId());
        assertThat(processInstanceById).isEqualTo(processInstance);

        // I should get a task for User1
        GetTasksPayload processInstanceTasksPayload = TaskPayloadBuilder.tasks().withProcessInstanceId(processInstance.getId()).build();
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50),
                                             processInstanceTasksPayload);
        assertThat(tasks.getTotalItems()).isEqualTo(1);
        Task task = tasks.getContent().get(0);

        //given
        taskOperations.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build())
                //then
                .expect(task().hasBeenCompleted(),
                        sequenceFlow("SequenceFlow_151v2cg").hasBeenTaken(),
                        task("Task Group1").hasBeenCreated())
                .expect(processInstance().hasTask("Task Group1",
                                                  Task.TaskStatus.CREATED,
                                                  createdTask -> {
                                                      assertThat(taskRuntime.userCandidates(createdTask.getId())).isEmpty();
                                                      assertThat(taskRuntime.groupCandidates(createdTask.getId())).contains("group1");
                                                  })
                );

        // Check with user1 as he is a candidate

        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50),
                                  processInstanceTasksPayload);

        assertThat(tasks.getTotalItems()).isEqualTo(1);

        // Check with user2 candidates which is not a candidate
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50));

        assertThat(tasks.getTotalItems()).isEqualTo(0);

        // Check with user3 candidates which is a candidate
        securityUtil.logInAs("user3");

        tasks = taskRuntime.tasks(Pageable.of(0,
                                              50),
                                  processInstanceTasksPayload);

        assertThat(tasks.getTotalItems()).isEqualTo(1);
    }

    @After
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0,
                                                                                                     50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
        clearEvents();
    }

    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }
}
