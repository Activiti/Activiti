package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.engine.ActivitiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(
        locations = {"classpath:application.properties"}
)
public class ProcessRuntimeVariableMappingTest {

    private static final String VARIABLE_MAPPING_PROCESS = "connectorVarMapping";
    private static final String VARIABLE_MAPPING_EXPRESSION_PROCESS = "connectorVarMappingExpression";
    private static final String OUTPUT_MAPPING_EXPRESSION_VARIABLE_PROCESS = "outputMappingExpVar";

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
                        tuple("input_unmapped_variable_with_matching_name", "inTest"),
                        tuple("input_unmapped_variable_with_non_matching_connector_input_name", "inTest"),
                        tuple("nickName", "testName"),
                        tuple("out_unmapped_variable_matching_name", "default"),
                        tuple("output_unmapped_variable_with_non_matching_connector_output_name", "default")
                );

        processBaseRuntime.delete(processInstance.getId(),"done");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void should_resolveExpression_when_expressionIsInInputMappingValueOrInMappedProperty() {
        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(VARIABLE_MAPPING_EXPRESSION_PROCESS);

        List<VariableInstance> variables = processBaseRuntime.getProcessVariablesByProcessId(processInstance.getId());

        String[] array = { "first", "${name}", "${surname}", "last" };
        List<String> list = Arrays.asList(array);

        Map<String, Object> dataMap = new HashMap();
        dataMap.put("age-in-months",
                    "${age * 12}");
        dataMap.put("full-name",
                    "${name} ${surname}");
        dataMap.put("demoString",
                    "expressionResolved");
        dataMap.put("list",
                    list);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.convertValue(dataMap, JsonNode.class);


        assertThat(variables).extracting(VariableInstance::getName,
                VariableInstance::getValue)
                .containsOnly(
                        tuple("age", 30),
                        tuple("name", "outName"),
                        tuple("surname", "Doe"),
                        tuple("data", data),
                        tuple("user-msg", "Hello ${name.concat(' ').concat(surname)}, today is your ${age}th birthday! It means ${age * 365.25} days of life"),
                        tuple("input-unmapped-variable-with-matching-name", "${surname}"),
                        tuple("input-unmapped-variable-with-non-matching-connector-input-name", "inTestExpression"),
                        tuple("variableToResolve", "${name}"),
                        tuple("out-unmapped-variable-matching-name", "defaultExpression"),
                        tuple("output-unmapped-variable-with-non-matching-connector-output-name", "defaultExpression")
                );

        processBaseRuntime.delete(processInstance.getId(),"done");
    }

    @Test
    public void should_throwActivitiException_when_expressionIsInOutputMapping() {

        Throwable throwable = catchThrowable(() -> processBaseRuntime.startProcessWithProcessDefinitionKey(OUTPUT_MAPPING_EXPRESSION_VARIABLE_PROCESS));

        assertThat(throwable).isInstanceOf(ActivitiException.class);

        assertThat(throwable.getMessage()).contains("Expressions are not allowed as variable values in the output mapping");
    }
}
