package org.activiti.spring.boot.process;

import java.util.List;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.GetVariablesPayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.DeleteProcessPayload;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application.properties"}
)
public class ProcessRuntimeVariableMappingTest {

    private static final String VARIABLE_MAPPING_PROCESS = "connectorVarMapping";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Test
    public void shouldMapVariables() {

        securityUtil.logInAs("salaboy");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(VARIABLE_MAPPING_PROCESS)
                                                                       .build());

        List<VariableInstance> variables = processRuntime.variables(new GetVariablesPayloadBuilder()
                                                                            .withProcessInstance(processInstance)
                                                                            .build());

        assertThat(variables).extracting(VariableInstance::getName,
                                         VariableInstance::getValue)
                .containsOnly(
                        tuple("name",
                              "outName"),
                        tuple("age",
                              25),
                        tuple("input-unmapped-variable-with-matching-name",
                              "inTest"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name",
                              "inTest"),
                        tuple("nickName",
                              "testName"),
                        tuple("out-unmapped-variable-matching-name",
                              "default"),
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name",
                              "default")
                );

        processRuntime.delete(new DeleteProcessPayload(processInstance.getId(),
                                                       "done"));
    }
}
