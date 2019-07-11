package org.activiti.spring.boot.process;

import java.util.List;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application.properties"}
)
public class ProcessRuntimeConnectorIT {

    private static final String CATEGORIZE_IMAGE_CONNECTORS_PROCESS = "categorizeProcessConnectors";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Before
    public void setUp() {
        securityUtil.logInAs("salaboy");
    }

    @After
    public void tearDown() {
        processCleanUpUtil.cleanUpWithAdmin();
    }

    /**
     * It tests two connector actions inside the xml against two different connector json definitions:
     * the first input variable is defined in the first connector action,
     * the second input variable in the second connector action.
     **/
    @Test
    public void shouldConnectorMatchWithConnectorDefinition() {


        processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_IMAGE_CONNECTORS_PROCESS)
                .withVariable("input-variable-name-1",
                        "input-variable-name-1")
                .withVariable("input-variable-name-2",
                        "input-variable-name-2")
                .withVariable("expectedKey",
                        true)
                .build());

    }

    @Test
    public void shouldSupportStaticValuesForConnectorInputsEvenWhenThereIsNoConnectorDefinition() {
        //given
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey("processWithExtensionsButNoConnectorDef")
                                                                       .build());

        //when
        List<VariableInstance> variables = processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(processInstance).build());


        //then
        assertThat(variables)
                .extracting(VariableInstance::getName,
                            VariableInstance::getValue)
                .containsOnly(
                        tuple(
                                "age",
                                21), //default value incremented by one by the connector
                        tuple("name",
                              "Paul") //static value passed to the connector ans send back as part of integration result
                );
    }
}
