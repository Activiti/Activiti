package org.activiti.spring.conformance.signals;

import org.activiti.api.model.shared.event.RuntimeEvent;
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
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.spring.conformance.signals.SignalsRuntimeTestConfiguration.collectedEvents;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SignalThrowCatchTest {

    @Autowired
    private ProcessRuntime processRuntime;
    
    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;
 
    @Before
    public void cleanUp() {
        collectedEvents.clear();
    }

    @Test
    public void testProcessWithThrowSignal() {
    	securityUtil.logInAs("user1");
    	
    	String processInstanceId = startThrowSignalProcess();
    	
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
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
        );
    	collectedEvents.clear();
    }
    
    @Test
    public void testProcessWithIntermediateCatchEventSignal() {
    	securityUtil.logInAs("user1");
    	
    	String processInstanceId = startIntermediateCatchEventSignalProcess();
    	
        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("Test")
                .withVariable("signal-variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);
        
        assertThat(collectedEvents)
		.extracting(RuntimeEvent::getEventType)
		.containsExactly(
				    ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
        );
        
        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent)collectedEvents.get(6);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables().size()).isEqualTo(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal-variable")).isEqualTo("test");
        
        
    	collectedEvents.clear();
    }
    
    @Test
    public void testProcessesWithThrowCatchSignal() {
    	securityUtil.logInAs("user1");
    	
    	String processInstanceCatch = startIntermediateCatchEventSignalProcess();
    	String processInstanceThrow = startThrowSignalProcess();
    	
        assertThat(collectedEvents)
		.extracting(RuntimeEvent::getEventType)
		.contains(
				    ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
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
        
        assertThat(collectedEvents)
		.extracting(RuntimeEvent::getEventType,	RuntimeEvent::getProcessInstanceId)
		.contains(
				  tuple(BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,processInstanceCatch)
        );
        
    	collectedEvents.clear();
    }
    
    

    @Test
    public void testProcessWithBoundaryEventSignal() {
    	securityUtil.logInAs("user1");
    	
    	String processInstanceId = startBoundaryEventSignalProcess();
    	
        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("Test")
                .withVariable("signal-variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);
        
        assertThat(collectedEvents)
		.extracting(RuntimeEvent::getEventType)
		.containsExactly(
				    ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED
        );
        
        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent)collectedEvents.get(6);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables().size()).isEqualTo(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal-variable")).isEqualTo("test");
        
        
        collectedEvents.clear();
    }
    
    @Test
    public void testProcessStartedBySignal() {
    	securityUtil.logInAs("user1");
    	
        SignalPayload signalPayload = ProcessPayloadBuilder.signal()
                .withName("SignalStart")
                .withVariable("signal-variable",
                              "test")
                .build();
        processRuntime.signal(signalPayload);
        
        assertThat(collectedEvents)
		.extracting(RuntimeEvent::getEventType)
		.containsExactly(
				    BPMNSignalEvent.SignalEvents.SIGNAL_RECEIVED,    
				    ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
        );
        
        BPMNSignalReceivedEvent event = (BPMNSignalReceivedEvent)collectedEvents.get(0);

        assertThat(event.getEntity()).isNotNull();
        assertThat(event.getEntity().getSignalPayload()).isNotNull();
        assertThat(event.getEntity().getSignalPayload().getName()).isEqualTo(signalPayload.getName());
        assertThat(event.getEntity().getSignalPayload().getVariables().size()).isEqualTo(signalPayload.getVariables().size());
        assertThat(event.getEntity().getSignalPayload().getVariables().get("signal-variable")).isEqualTo("test");
        
        
        collectedEvents.clear();
    }
    
    @After
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
    }

    private String startThrowSignalProcess(){
    	ProcessInstance process = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("broadcastSignalEventProcess")
                .withBusinessKey("broadcast-signal-business-key")
                .withName("broadcast-signal-instance-name")
                .build());
    	
        return process.getId();

    }
    
    private String startIntermediateCatchEventSignalProcess(){
    	ProcessInstance process = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("broadcastSignalCatchEventProcess")
                .withBusinessKey("catch-business-key")
                .withName("catch-signal-instance-name")
                .build());
        
        return process.getId();
    }
    
    private String startBoundaryEventSignalProcess(){
    	ProcessInstance process = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("ProcessWithBoundarySignal")
                .withBusinessKey("boundary-business-key")
                .withName("boundary-signal-instance-name")
                .build());
    	
    	return process.getId();

    }
    
}
