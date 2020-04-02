package org.activiti.examples;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    private static final String PROCESS_DEFINITION_KEY = "categorizeProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void contextLoads() {
        securityUtil.logInAs("system");

        ProcessDefinition processDefinition = processRuntime.processDefinition(PROCESS_DEFINITION_KEY);

        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(processDefinition.getAppVersion()).isNull();
    }

}
