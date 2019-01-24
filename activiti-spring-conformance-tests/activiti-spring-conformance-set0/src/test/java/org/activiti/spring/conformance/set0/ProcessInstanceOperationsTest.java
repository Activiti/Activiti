package org.activiti.spring.conformance.set0;

import static org.activiti.spring.conformance.set0.Set0RuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.NotFoundException;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessInstanceOperationsTest {

    private final String processKey = "usertaskwi-4d5c4312-e8fc-4766-a727-b55a4d3255e9";
    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Before
    public void cleanUp() {
        collectedEvents.clear();
    }

    /*
     */
    @Test
    public void shouldBeAbleToStartAndDeleteProcessInstance() {
        securityUtil.logInAs("user1");
        //when
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


        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED);

        collectedEvents.clear();

        ProcessInstance deletedProcessInstance = processRuntime.delete(ProcessPayloadBuilder.delete(processInstance.getId()));
        assertThat(deletedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.DELETED);

        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(
                BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CANCELLED);

        // No Process Instance should be found
        Throwable throwable = catchThrowable(() -> processRuntime.processInstance(deletedProcessInstance.getId()));

        assertThat(throwable)
                .isInstanceOf(NotFoundException.class);
    }


    /*
     */
    @Test
    public void shouldBeAbleToStartSuspendAndResumeProcessInstance() {
        securityUtil.logInAs("user1");
        //when
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


        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED);

        collectedEvents.clear();

        ProcessInstance suspendedProcessInstance = processRuntime.suspend(ProcessPayloadBuilder.suspend(processInstance.getId()));
        assertThat(suspendedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.SUSPENDED);

        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(ProcessRuntimeEvent.ProcessEvents.PROCESS_SUSPENDED);

        collectedEvents.clear();

        ProcessInstance resumedProcessInstance = processRuntime.resume(ProcessPayloadBuilder.resume(suspendedProcessInstance.getId()));
        assertThat(resumedProcessInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);

        assertThat(collectedEvents).extracting(RuntimeEvent::getEventType).containsExactly(ProcessRuntimeEvent.ProcessEvents.PROCESS_RESUMED);
        
    }
}
