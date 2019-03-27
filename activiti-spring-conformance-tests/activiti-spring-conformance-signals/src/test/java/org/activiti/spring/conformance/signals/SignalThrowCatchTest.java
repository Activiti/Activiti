package org.activiti.spring.conformance.signals;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
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

    @After
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
    }

    private String startThrowSignalProcess(){
        return processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("broadcastSignalEventProcess")
                .withBusinessKey("broadcast-signal-business-key")
                .withName("broadcast-signal-instance-name")
                .build()).getId();

    }
    
}
