package org.activiti.spring.boot.process;

import java.util.HashMap;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeEventsIT {

    private static final String SINGLE_TASK_PROCESS = "SingleTaskProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @BeforeEach
    public void init() {
        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;
        //Reset event collections
        RuntimeTestConfiguration.variableCreatedEventsFromProcessInstance.clear();
        RuntimeTestConfiguration.sequenceFlowTakenEvents.clear();
    }

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void shouldGetSameProcessInstanceIfForAllSequenceFlowTakenEvents(){

        //given
        securityUtil.logInAs("user");

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

        //given
        securityUtil.logInAs("user");

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
        //given
        securityUtil.logInAs("user");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
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
        //given
        securityUtil.logInAs("user");

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

}
