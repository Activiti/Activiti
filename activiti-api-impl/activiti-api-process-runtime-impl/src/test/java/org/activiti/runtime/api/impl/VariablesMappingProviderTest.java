package org.activiti.runtime.api.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static org.activiti.runtime.api.impl.MappingExecutionContext.buildMappingExecutionContext;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

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
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
        given(execution.getVariable("process_variable_inputmap_1")).willReturn("new-input-value");
        given(execution.getVariable("property-with-no-default-value")).willReturn(null);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
                                                                                     extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     expressionResolver);
        
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
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
                ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
                                                                                     extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     expressionResolver);

        Map<String, Object> variables = new HashMap<>();
        variables.put("var-one", "one");
        variables.put("var-two", 2);
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
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-extensions.json"), ProcessExtensionModel.class);

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
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-with-constants-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                Map.Entry::getValue)
                .containsOnly(
                        tuple("process_constant_1_2", "constant_2_value"),
                        tuple("process_constant_inputmap_2", "constant_value")
                );

    }

    @Test
    public void calculateOutputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
                                                                                     extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     expressionResolver);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1", "var-one");

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                entityVariables);

        //then
        assertThat(outPutVariables.get("process_variable_outputmap_1")).isEqualTo("var-one");

        //mapped with a task variable that is not present, so it should not be present
        assertThat(outPutVariables).doesNotContainKey("property-with-no-default-value");
    }

    @Test
    public void calculateOutputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1", "var-one");
        entityVariables.put("non-mapped-output_variable_name_2", "var-two");

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
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-extensions.json"), ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions);

        Map<String, Object> entityVariables = new HashMap<>();
        entityVariables.put("task_output_variable_name_1", "var-one");
        entityVariables.put("non-mapped-output_variable_name_2", "var-two");

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
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
                                                                                             extensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
                                     "expressionResolver",
                                     expressionResolver);

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
    public void should_substituteExpressions_when_expressionIsInProperties() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-properties.json");

        String[] array = { "1", "this expressionResolved is OK", "2" };
        List<String> list = Arrays.asList(array);

        Map<String, Object> var1 = new HashMap<>();
        var1.put("prop1",
                 "property 1");
        var1.put("prop2",
                 "expressionResolved");
        var1.put("prop3",
                 list);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    var1),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));
    }

    @Test(expected = ActivitiIllegalArgumentException.class)
    public void should_throwActivitiIllegalArgumentException_when_expressionIsOutputMapping() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-properties.json");

        String[] array = { "1", "this expressionResolved is OK", "2" };
        List<String> list = Arrays.asList(array);

        Map<String, Object> var1 = new HashMap<>();
        var1.put("prop1",
                 "property 1");
        var1.put("prop2",
                 "expressionResolved");
        var1.put("prop3",
                 list);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                .containsOnly(tuple("process_constant_1",
                                    "constant_1_value"),
                              tuple("process_constant_2",
                                    "constant_2_value"),
                              tuple("task_input_variable_name_1",
                                    var1),
                              tuple("task_input_variable_name_2",
                                    "static_value_1"));

        Map<String, Object> entityVariables = execution.getVariables();
        entityVariables.put("task_input_variable_name_1",
                            "variable_value_1");
        entityVariables.put("task_input_variable_name_2",
                            "static_value_2");

        variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                          entityVariables);
    }

}
