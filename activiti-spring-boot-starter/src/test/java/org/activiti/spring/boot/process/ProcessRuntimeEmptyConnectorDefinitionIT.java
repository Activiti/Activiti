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
        locations = {"classpath:application-connectors-empty.properties"}
)
@ContextConfiguration
public class ProcessRuntimeEmptyConnectorDefinitionIT {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    /**
     * This test points to a directory having no connectors definitions.
     * As resulting behaviour, we have the same when there is no match with connector definitions.
     **/
    @Test
    @WithUserDetails(value = "salaboy", userDetailsServiceBeanName = "myUserDetailsService")
    public void connectorDefinitionEmptyDir() {
        processRuntime.start(ProcessPayloadBuilder.start()
                                     .withProcessDefinitionKey(CATEGORIZE_PROCESS)
                                     .withVariable("expectedKey",
                                                   true)
                                     .build());
    }
}
