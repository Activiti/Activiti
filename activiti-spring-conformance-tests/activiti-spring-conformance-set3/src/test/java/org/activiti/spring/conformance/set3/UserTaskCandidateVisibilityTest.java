package org.activiti.spring.conformance.set3;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.activiti.spring.conformance.set3.Set3RuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserTaskCandidateVisibilityTest {

    private final String processKey = "usertaskca-1e577517-7404-4645-b650-4fbde528f612";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Before
    public void cleanUp() {
        collectedEvents.clear();
    }


    @Test
    public void shouldCreateATaskAndAddNewCandidateUser() {

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

        final Task task = tasks.getContent().get(0);

        Task taskById = taskRuntime.task(task.getId());

        assertThat(taskById.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


        assertThat(task).isEqualTo(taskById);

        assertThat(task.getAssignee()).isNull();


        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED);

        collectedEvents.clear();

        // Check with user2
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(0);

        Throwable throwable = catchThrowable(() ->  taskRuntime.task(task.getId()));

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);

        // Check with user1 candidates
        securityUtil.logInAs("user1");

        taskById = taskRuntime.task(task.getId());

        List<String> candidateUsers = taskRuntime.userCandidates(task.getId());
        assertThat(candidateUsers).isEmpty();

        List<String> candidateGroups = taskRuntime.groupCandidates(task.getId());
        assertThat(candidateGroups).contains("group1");

        // This should fail because user1 is not the assignee
        throwable = catchThrowable(() ->  taskRuntime.addCandidateUsers(TaskPayloadBuilder
                .addCandidateUsers()
                .withTaskId(task.getId())
                .withCandidateUser("user2")
                .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);

        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        // Now it should work
        taskRuntime.addCandidateUsers(TaskPayloadBuilder
                .addCandidateUsers()
                .withTaskId(task.getId())
                .withCandidateUser("user2")
                .build());

        candidateUsers = taskRuntime.userCandidates(task.getId());
        assertThat(candidateUsers).contains("user2");

        // User 1 needs to release the task in order for User 2 see it as candidate

        taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());

        // Check with user2
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

    }


    @Test
    public void shouldCreateATaskAndAddNewCandidateGroup() {

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

        final Task task = tasks.getContent().get(0);

        Task taskById = taskRuntime.task(task.getId());

        assertThat(taskById.getStatus()).isEqualTo(Task.TaskStatus.CREATED);


        assertThat(task).isEqualTo(taskById);

        assertThat(task.getAssignee()).isNull();


        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED);

        collectedEvents.clear();

        // Check with user2
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(0);

        Throwable throwable = catchThrowable(() ->  taskRuntime.task(task.getId()));

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);

        // Check with user1 candidates
        securityUtil.logInAs("user1");

        taskById = taskRuntime.task(task.getId());

        List<String> candidateUsers = taskRuntime.userCandidates(task.getId());
        assertThat(candidateUsers).isEmpty();

        List<String> candidateGroups = taskRuntime.groupCandidates(task.getId());
        assertThat(candidateGroups).contains("group1");

        // This should fail because user1 is not the assignee
        throwable = catchThrowable(() ->  taskRuntime.addCandidateUsers(TaskPayloadBuilder
                .addCandidateUsers()
                .withTaskId(task.getId())
                .withCandidateUser("user2")
                .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);



        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                        TaskRuntimeEvent.TaskEvents.TASK_UPDATED);

        collectedEvents.clear();

        // Now it should work
        taskRuntime.addCandidateGroups(TaskPayloadBuilder
                .addCandidateGroups()
                .withTaskId(task.getId())
                .withCandidateGroup("group2")
                .build());

        //@TODO: operations should cause events
        // https://github.com/Activiti/Activiti/issues/2330
        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly();

        collectedEvents.clear();

        candidateGroups = taskRuntime.groupCandidates(task.getId());
        assertThat(candidateGroups).contains("group1", "group2");

        // User 1 needs to release the task in order for User 2 see it as candidate

        taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                        TaskRuntimeEvent.TaskEvents.TASK_UPDATED);

        collectedEvents.clear();

        // Check with user2
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

    }

    @After
    public void cleanup(){
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for(ProcessInstance pi : processInstancePage.getContent()){
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
    }
    
}
