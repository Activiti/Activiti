package org.activiti.spring.boot.process;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application-connectors-empty.properties"}
)
public class ProcessRuntimeEmptyConnectorDefinitionIT {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * This test points to a directory having no connectors definitions.
     * As resulting behaviour, we have the same when there is no match with connector definitions.
     **/
    @Test
    public void connectorDefinitionEmptyDir() {

        securityUtil.logInAs("user");

        processRuntime.start(ProcessPayloadBuilder.start()
                                     .withProcessDefinitionKey(CATEGORIZE_PROCESS)
                                     .withVariable("expectedKey",
                                                   true)
                                     .build());
    }
}
