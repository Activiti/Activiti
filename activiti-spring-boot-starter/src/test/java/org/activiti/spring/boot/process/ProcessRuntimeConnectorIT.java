package org.activiti.spring.boot.process;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application.properties"}
)
@ContextConfiguration
public class ProcessRuntimeConnectorIT {

    private static final String CATEGORIZE_IMAGE_CONNECTORS_PROCESS = "categorizeProcessConnectors";

    @Autowired
    private ProcessRuntime processRuntime;

    /**
     * It tests two connector actions inside the xml against two different connector json definitions:
     * the first input variable is defined in the first connector action,
     * the second input variable in the second connector action.
     **/
    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
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
}
