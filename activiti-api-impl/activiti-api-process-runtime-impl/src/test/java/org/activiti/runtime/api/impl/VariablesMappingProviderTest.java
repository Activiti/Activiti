package org.activiti.runtime.api.impl;

import static org.activiti.runtime.api.impl.MappingExecutionContext.buildMappingExecutionContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VariablesMappingProviderTest {

    @InjectMocks
    private VariablesMappingProvider variablesMappingProvider;

    @Mock
    private ProcessExtensionService processExtensionService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void calculateInputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
        ExpressionResolverHelper.initContext(execution,
                                             extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     new ExpressionResolver());

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables.get("task_input_variable_name_1")).isEqualTo("new-input-value");

        //mapped with process variable that is null, so it should not be present
        assertThat(inputVariables).doesNotContainKeys("task_input_variable_mapped_with_null_process_variable");
    }

    private DelegateExecution buildExecution(ProcessExtensionModel extensions) {
        DelegateExecution execution = mock(DelegateExecution.class);
        given(processExtensionService.getExtensionsForId("taskVarMapping")).willReturn(extensions);
        given(execution.getProcessDefinitionId()).willReturn("taskVarMapping");
        given(execution.getCurrentActivityId()).willReturn("simpleTask");
        return execution;

    }

    @Test
    public void calculateInputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
        Map<String, Object> variables = new HashMap<>();
        variables.put("varone",
                      "one");
        variables.put("vartwo",
                      2);
        given(execution.getVariables()).willReturn(variables);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isEqualTo(variables);
    }

    @Test
    public void calculateInputVariablesShouldNotPassAnyVariablesWhenTheMappingIsEmpty() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isEmpty();
    }

    @Test
    public void calculateInputVariablesShouldPassOnlyConstantsWhenTheMappingIsEmpty() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-with-constants-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1_2",
                                    "constant_2_value"),
                              tuple("process_constant_inputmap_2",
                                    "constant_value"));

    }

    @Test
    public void calculateOutputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1",
                            "varone");

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        //then
        assertThat(outPutVariables.get("process_variable_outputmap_1")).isEqualTo("varone");

        //mapped with a task variable that is not present, so it should not be present
        assertThat(outPutVariables).doesNotContainKey("property-with-no-default-value");
    }

    @Test
    public void calculateOutputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1",
                            "varone");
        entityVariables.put("non-mapped-output_variable_name_2",
                            "vartwo");

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        //then
        assertThat(outPutVariables).isEqualTo(entityVariables);
    }

    @Test
    public void calculateOutputVariablesShouldNotPassAnyVariablesWhenTheMappingIsEmpty() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-extensions.json"),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1",
                            "varone");
        entityVariables.put("non-mapped-output_variable_name_2",
                            "vartwo");

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                               entityVariables);

        //then
        assertThat(inputVariables).isEmpty();
    }

    private DelegateExecution initExpressionResolverTest(String fileName) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/expressions/" + fileName),
                                                                  ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
        ExpressionResolverHelper.initContext(execution,
                                             extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     new ExpressionResolver());

        return execution;
    }

    @Test
    public void should_notSubstituteExpressions_when_thereAreNoExpressions() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("no-expression.json");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    "variable_value_1"),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                          Map.Entry::getValue)
                .containsOnly(tuple("process_variable_3",
                                    "variable_value_1"),
                              tuple("process_variable_4",
                                    "static_value_2"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInConstants() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-constants.json");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "${process_variable_1}"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    "variable_value_1"),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                          Map.Entry::getValue)
                .containsOnly(tuple("process_variable_3",
                                    "variable_value_1"),
                              tuple("process_variable_4",
                                    "static_value_2"));
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInInputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-input-value.json");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    "variable_value_1"),
                              tuple("task_input_variable_name_2",
                                    "variable_value_1"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInInputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-input-variable.json");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInOutputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-value.json");

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                          Map.Entry::getValue)
                .containsOnly(tuple("process_variable_3",
                                    "variable_value_1"),
                              tuple("process_variable_4",
                                    "${process_variable_1}"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInOutputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-variable.json");

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                          Map.Entry::getValue)
                .containsOnly(tuple("process_variable_4",
                                    "static_value_2"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInProperties() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-properties.json");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    "${process_variable_2}"),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                                                                entityVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                          Map.Entry::getValue)
                .containsOnly(tuple("process_variable_3",
                                    "variable_value_1"),
                              tuple("process_variable_4",
                                    "static_value_2"));
    }
}
