package org.activiti.runtime.api.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

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

}
