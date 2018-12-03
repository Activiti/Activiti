package org.activiti.spring.conformance.basic;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.spring.conformance.basic.security.util.SecurityUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.spring.conformance.basic.RuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ConformanceBasicProcessInformationTest {

    private final String processKey = "processinf-4e42752c-cc4d-429b-9528-7d3df24a9537";
    @Autowired
    private ProcessRuntime processRuntime;

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
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();

        //then
        assertThat(configuration).isNotNull();
    }

    /*
     * This test covers the Process Information.bpmn20.xml process which contains a BPMN Start Event, a BPMN SequenceFlow and BPMN End Event
     * This execution should generate 8 events:
     *   - PROCESS_CREATED,
     *   - PROCESS_STARTED
     *   - ACTIVITY_STARTED
     *   - ACTIVITI_COMPLETED
     *   - SEQUENCE_FLOW_TAKEN
     *   - ACTIVITY_STARTED
     *   - ACTIVITI_COMPLETED
     *   - PROCESS_COMPLETED
     *  And the Process Instance Status should be Completed
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

        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED);

    }

    /*
     * This test covers the Process Information.bpmn20.xml process which contains a BPMN Start Event, a BPMN SequenceFlow and BPMN End Event
     * This test add variables to the process and generates more events
     * This execution should generate 9 events:
     *   - PROCESS_CREATED,
     *   - VARIABLE_CREATED
     *   - PROCESS_STARTED
     *   - ACTIVITY_STARTED
     *   - ACTIVITI_COMPLETED
     *   - SEQUENCE_FLOW_TAKEN
     *   - ACTIVITY_STARTED
     *   - ACTIVITI_COMPLETED
     *   - PROCESS_COMPLETED
     *  And the Process Instance Status should be Completed
     *  No further operation can be executed on the process due the fact that it start and finish in the same transaction.
     *
     */
    @Test
    public void shouldBeAbleToStartProcessWithVariables() {
        securityUtil.logInAs("user1");
        //when
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withProcessInstanceName("my-process-instance-name")
                .withVariable("var1", "value1")
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

        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                VariableEvent.VariableEvents.VARIABLE_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED);
    }


}
