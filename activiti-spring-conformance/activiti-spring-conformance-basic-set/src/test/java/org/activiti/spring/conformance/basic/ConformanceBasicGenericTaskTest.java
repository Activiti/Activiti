package org.activiti.spring.conformance.basic;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableCreatedEvent;
import org.activiti.api.model.shared.event.VariableDeletedEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.*;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.process.runtime.events.ProcessCompletedEvent;
import org.activiti.api.process.runtime.events.ProcessStartedEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.api.runtime.shared.events.VariableEventListener;
import org.activiti.spring.conformance.basic.security.util.SecurityUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.activiti.spring.conformance.basic.RuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConformanceBasicGenericTaskTest {

    private final String processKey = "processwit-c6fd1b26-0d64-47f2-8d04-0b70764444a7";
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @After
    public void cleanUp() {
        collectedEvents.clear();
    }

    /*
     * This test covers the Process with Generic BPMN Task.bpmn20.xml process
     * This execution should generate 11 events:
     *   - PROCESS_CREATED
     *   - PROCESS_STARTED,
     *   - ACTIVITY_STARTED,
     *   - ACTIVITY_COMPLETED,
     *   - SEQUENCE_FLOW_TAKEN,
     *   - ACTIVITY_STARTED,
     *   - ACTIVITY_COMPLETED,
     *   - SEQUENCE_FLOW_TAKEN,
     *   - ACTIVITY_STARTED,
     *   - ACTIVITY_COMPLETED,
     *   - PROCESS_COMPLETED
     *  And the Process Instance Status should be Completed
     *  By Default generic BPMN Tasks doesn't have any associated behaviour, meaning that they will started and completed automatically.
     *  No further operation can be executed on the process due the fact that it start and finish in the same transaction
     */
    @Test
    public void shouldBeAbleToStartProcess() {
        securityUtil.logInAs("user1");
        //when
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withProcessInstanceName("my-process-instance-name")
                .build());

        //then
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(processInstance.getBusinessKey()).isEqualTo("my-business-key");
        assertThat(processInstance.getName()).isEqualTo("my-process-instance-name");

        // No Process Instance should be found
        Throwable throwable = catchThrowable(() -> processRuntime.processInstance(processInstance.getId()));

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);

        // No Variable Instance should be found
        throwable = catchThrowable(() -> processRuntime.variables(
                ProcessPayloadBuilder
                        .variables()
                        .withProcessInstanceId(processInstance.getId())
                        .build()));
        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);


        assertThat(collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED);

    }

}
