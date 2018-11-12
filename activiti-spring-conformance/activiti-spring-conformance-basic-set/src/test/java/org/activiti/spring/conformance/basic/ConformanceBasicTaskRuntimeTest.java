package org.activiti.spring.conformance.basic;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessDefinitionMeta;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.api.task.runtime.conf.TaskRuntimeConfiguration;
import org.activiti.api.task.runtime.events.listener.TaskRuntimeEventListener;
import org.activiti.spring.conformance.basic.security.util.SecurityUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_CREATED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_UPDATED;
import static org.activiti.spring.conformance.basic.RuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConformanceBasicTaskRuntimeTest {

    private final String processKey = "usertask-6a854551-861f-4cc5-a1a1-73b8a14ccdc4";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @After
    public void cleanUp() {
        collectedEvents.clear();
    }

    @Test
    public void shouldGetConfiguration() {
        securityUtil.logInAs("user1");
        //when
        TaskRuntimeConfiguration configuration = taskRuntime.configuration();
        //then
        assertThat(configuration).isNotNull();
        //when
        List<TaskRuntimeEventListener<?>> processRuntimeEventListeners = configuration.taskRuntimeEventListeners();
        List<VariableEventListener<?>> variableEventListeners = configuration.variableEventListeners();
        //then
        assertThat(processRuntimeEventListeners).hasSize(5);
        assertThat(variableEventListeners).hasSize(3);

    }


    @Test
    public void shouldStartAProcessAndCreateATask(){

        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withProcessInstanceName("my-process-instance-name")
                .build());

        //then
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
        assertThat(processInstance.getBusinessKey()).isEqualTo("my-business-key");
        assertThat(processInstance.getName()).isEqualTo("my-process-instance-name");

        // I should be able to get the process instance from the Runtime because it is still running
        ProcessInstance processInstanceById =  processRuntime.processInstance(processInstance.getId());

        assertThat(processInstanceById).isEqualTo(processInstance);

        // I should get a task for User1
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getTotalItems()).isEqualTo(1);

        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        TASK_CREATED,
                        TASK_ASSIGNED);


    }







}
