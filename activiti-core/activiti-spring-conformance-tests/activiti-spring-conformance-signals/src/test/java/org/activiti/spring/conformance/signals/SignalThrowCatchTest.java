package org.activiti.spring.conformance.signals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableEvent;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.BPMNSignalEvent;
import org.activiti.api.process.model.events.BPMNSignalReceivedEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.model.impl.BPMNActivityImpl;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SignalThrowCatchTest {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @BeforeEach
    public void cleanUp() {
        clearEvents();
    }

    @AfterEach
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0,
                                                                                                     50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
        clearEvents();
    }

    @Test
    public void testProcessWithThrowSignal() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = startThrowSignalProcess();

        assertThat(RuntimeTestConfiguration.collectedEvents)
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
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
                );

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .filteredOn(event -> event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED))
                .filteredOn(event -> ((BPMNActivity) event.getEntity()).getActivityType().equals("throwEvent"))
                .extracting(event -> ((BPMNActivity) event.getEntity()).getActivityType(),
                            event -> ((BPMNActivity) event.getEntity()).getProcessInstanceId())
                .contains(
                        tuple("throwEvent",
                              processInstance.getId()));

    }

    @Test
    public void testProcessWithIntermediateCatchEventSignal() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = startIntermediateCatchEventSignalProcess();

        assertThat(RuntimeTestConfiguration.collectedEvents)
        .extracting(RuntimeEvent::getEventType)
        .containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
        );
        BPMNActivityImpl signalCatchEvent = (BPMNActivityImpl)RuntimeTestConfiguration.collectedEvents.get(5).getEntity();
        assertThat(signalCatchEvent.getActivityType()).isEqualTo("intermediateCatchEvent");

        clearEvents();
        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("Test")
                .withVariable("signal_variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
                );

        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent) RuntimeTestConfiguration.collectedEvents.get(0);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables()).hasSize(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal_variable")).isEqualTo("test");

    }

    @Test
    public void testProcessesWithThrowCatchSignal() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstanceCatch = startIntermediateCatchEventSignalProcess();

        assertThat(RuntimeTestConfiguration.collectedEvents)
        .extracting(RuntimeEvent::getEventType)
        .contains(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
        );
        BPMNActivityImpl signalCatchEvent = (BPMNActivityImpl)RuntimeTestConfiguration.collectedEvents.get(5).getEntity();
        assertThat(signalCatchEvent.getActivityType()).isEqualTo("intermediateCatchEvent");

        clearEvents();

        startThrowSignalProcess();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType
                            )
                .contains(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
                );

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .filteredOn(event -> BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED.name().equals(event.getEventType().name()))
                .extracting(RuntimeEvent::getEventType,
                            RuntimeEvent::getProcessInstanceId)
                .contains(
                        tuple(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                              processInstanceCatch.getId())
                );

    }

    @Test
    public void testProcessWithBoundaryEventSignal() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = startBoundaryEventSignalProcess();

        assertThat(RuntimeTestConfiguration.collectedEvents)
        .extracting(RuntimeEvent::getEventType)
        .containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                TaskRuntimeEvent.TaskEvents.TASK_CREATED
        );

        BPMNActivityImpl signalCatchEvent = (BPMNActivityImpl)RuntimeTestConfiguration.collectedEvents.get(5).getEntity();
        assertThat(signalCatchEvent.getActivityType()).isEqualTo("userTask");
        assertThat(signalCatchEvent.getActivityName()).isEqualTo("Boundary container");

        clearEvents();

        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("Test")
                .withVariable("signal_variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactlyInAnyOrder(
                        BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        TaskRuntimeEvent.TaskEvents.TASK_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_CANCELLED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED
                );

        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent) RuntimeTestConfiguration.collectedEvents.get(0);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables()).hasSize(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal_variable")).isEqualTo("test");

        clearEvents();
    }

    @Test
    public void testProcessStartedBySignal() {
        securityUtil.logInAs("user1");

        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("SignalStart")
                .withVariable("signal_variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
                );

        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent) RuntimeTestConfiguration.collectedEvents.get(1);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables()).hasSize(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal_variable")).isEqualTo("test");

        clearEvents();
    }

    private ProcessInstance startThrowSignalProcess() {

        return processRuntime.start(ProcessPayloadBuilder
                                                               .start()
                                                               .withProcessDefinitionKey("broadcastSignalEventProcess")
                                                               .withBusinessKey("broadcast-signal-business-key")
                                                               .withName("broadcast-signal-instance-name")
                                                               .build());
    }

    private ProcessInstance startIntermediateCatchEventSignalProcess() {

        return processRuntime.start(ProcessPayloadBuilder
                                                               .start()
                                                               .withProcessDefinitionKey("broadcastSignalCatchEventProcess")
                                                               .withBusinessKey("catch-business-key")
                                                               .withName("catch-signal-instance-name")
                                                               .build());
    }

    private ProcessInstance startBoundaryEventSignalProcess() {
        return processRuntime.start(ProcessPayloadBuilder
                                                               .start()
                                                               .withProcessDefinitionKey("ProcessWithBoundarySignal")
                                                               .withBusinessKey("boundary-business-key")
                                                               .withName("boundary-signal-instance-name")
                                                               .build());
    }

    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }
}
