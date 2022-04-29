/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.activiti.api.runtime.shared.NotFoundException;
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
public class UserTaskCandidateGroupRuntimeTest {

    private final String processKey = "usertaskwi-75883efa-38c6-4c17-9da4-161234523fbf";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @BeforeEach
    public void cleanUp() {
        clearEvents();
    }


    @Test
    public void shouldCreateClaimAndCompleteTask() {

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

        clearEvents();

        // Check with user2
        securityUtil.logInAs("user2");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(0);

        Throwable throwable = catchThrowable(() ->  taskRuntime.task(task.getId()));

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);


        // Check with user3
        securityUtil.logInAs("user3");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);


        // Try to complete without claim should error

        securityUtil.logInAs("user1");

        throwable = catchThrowable(() ->  taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);

        // Claim should be allowed
        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(claimedTask.getAssignee()).isEqualTo("user1");

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                                 TaskRuntimeEvent.TaskEvents.TASK_UPDATED);

        clearEvents();

        //complete task now should work
        Task completedTask = taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(claimedTask.getId()).build());

        assertThat(completedTask.getStatus()).isEqualTo(Task.TaskStatus.COMPLETED);

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        TaskRuntimeEvent.TaskEvents.TASK_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED);


    }

    @Test
    public void shouldCreateClaimAndReleaseTask() {

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

        // I should get a task for User1
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

        Task task = tasks.getContent().get(0);

        Task taskById = taskRuntime.task(task.getId());

        assertThat(taskById.getStatus()).isEqualTo(Task.TaskStatus.CREATED);

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

        clearEvents();

        // Check with user3
        securityUtil.logInAs("user3");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);


        //Claim with User 1
        securityUtil.logInAs("user1");

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(claimedTask.getAssignee()).isEqualTo("user1");

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                                 TaskRuntimeEvent.TaskEvents.TASK_UPDATED);
        clearEvents();

        // Check with user3, he/she shouldn't see any task now that the task was assigned
        securityUtil.logInAs("user3");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(0);


        //Release with User 1
        securityUtil.logInAs("user1");

        Task releasedTask = taskRuntime.release(TaskPayloadBuilder.release().withTaskId(task.getId()).build());

        assertThat(releasedTask.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        assertThat(releasedTask.getAssignee()).isNull();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED,
                                 TaskRuntimeEvent.TaskEvents.TASK_UPDATED);

        clearEvents();

        // User 1 should be able to see the task to claim now
        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

        // User 3 also should be able to see the task now

        // Check with user3
        securityUtil.logInAs("user3");

        tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

    }

    @AfterEach
    public void cleanup(){
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for(ProcessInstance pi : processInstancePage.getContent()){
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }

        clearEvents();
    }

    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }

}
