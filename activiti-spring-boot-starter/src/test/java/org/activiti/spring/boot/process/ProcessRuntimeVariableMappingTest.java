package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application.properties"}
)
public class ProcessRuntimeVariableMappingTest {

    private static final String VARIABLE_MAPPING_PROCESS = "connectorVarMapping";
    
    private static final String VARIABLE_MAPPING_EXPRESSION_PROCESS = "connectorVarMappingExpression";

    @Autowired
    private ProcessBaseRuntime processBaseRuntime;

    @Test
    public void shouldMapVariables() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(VARIABLE_MAPPING_PROCESS);

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        assertThat(variables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("name", "outName"),
                        tuple("age", 35),
                        tuple("input-unmapped-variable-with-matching-name", "inTest"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name", "inTest"),
                        tuple("nickName", "testName"),
                        tuple("out-unmapped-variable-matching-name", "default"),
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name", "default")
                );

        processBaseRuntime.delete(processInstance.getId(),"done");
    }
    
    @Test
    public void should_resolveExpression_when_expressionIsInInputMappingValue() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(VARIABLE_MAPPING_EXPRESSION_PROCESS);

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        assertThat(variables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("name", "outName"),
                        tuple("age", 30),
                        tuple("input-unmapped-variable-with-matching-name", "inTestExpression"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name", "inTestExpression"),
                        tuple("valueToResolve", "expressionResolved"),
                        tuple("out-unmapped-variable-matching-name", "defaultExpression"),
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name", "defaultExpression")
                );

        processBaseRuntime.delete(processInstance.getId(),"done");
    }
}
