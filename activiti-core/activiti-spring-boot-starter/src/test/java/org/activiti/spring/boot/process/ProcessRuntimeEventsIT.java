package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeEventsIT {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";
    public static final String LOGGED_USER = "user";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @BeforeEach
    public void init() {
        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;
        //Reset event collections
        RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance.clear();
        RuntimeTestConfiguration.sequenceFlowTakenEvents.clear();
        localEventSource.clearEvents();
        securityUtil.logInAs(LOGGED_USER);
    }

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldGetSameProcessInstanceIfForAllSequenceFlowTakenEvents(){
        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.sequenceFlowTakenEvents)
                .extracting(event -> event.getProcessInstanceId())
                .contains(categorizeProcess.getId());
    }

    @Test
    public void shouldGetSameProcessInstanceIfForAllVariableCreatedEvents(){
        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .extracting(event -> event.getProcessInstanceId())
                .contains(categorizeProcess.getId());
    }

    @Test
    public void shouldGetJustOneVariableCreatedEvent(){
        //when
        processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariable("name",
                        "peter")
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    public void shouldGetJustThreeVariableCreatedEvent(){
        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
                .withVariables(new HashMap<String, Object>() {{
                    put("name", "peter");
                    put("surname", "peterson");
                    put("age", 25);
                }})
                .build());
        //then
        assertThat(RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance)
                .isNotEmpty()
                .hasSize(3);
    }


    @Test
    public void should_emmitEventOnProcessDeletion() {
        //given
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(SINGLE_TASK_PROCESS)
            .withName("to be deleted")
            .withBusinessKey("my business key")
            .build());

        //when
        processRuntime.delete(ProcessPayloadBuilder.delete(processInstance));

        //then
        List<ProcessCancelledEvent> processCancelledEvents = localEventSource
            .getEvents(ProcessCancelledEvent.class);
        assertThat(processCancelledEvents).hasSize(1);

        ProcessCancelledEvent processCancelledEvent = processCancelledEvents.get(0);
        assertThat(processCancelledEvent.getCause()).isEqualTo("process instance deleted");
        assertThat(processCancelledEvent.getEntity().getId()).isEqualTo(processInstance.getId());
        assertThat(processCancelledEvent.getEntity().getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(processCancelledEvent.getEntity().getName()).isEqualTo(processInstance.getName());
        assertThat(processCancelledEvent.getEntity().getBusinessKey()).isEqualTo(processInstance.getBusinessKey());
        assertThat(processCancelledEvent.getEntity().getStartDate()).isEqualTo(processInstance.getStartDate());
        assertThat(processCancelledEvent.getEntity().getInitiator()).isEqualTo(LOGGED_USER);
    }
}
