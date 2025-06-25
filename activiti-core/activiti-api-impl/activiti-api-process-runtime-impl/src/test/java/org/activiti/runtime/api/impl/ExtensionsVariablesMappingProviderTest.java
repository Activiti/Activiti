/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.runtime.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.activiti.core.el.ActivitiElContext;
import org.activiti.core.el.CustomFunctionProvider;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext.buildMappingExecutionContext;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.activiti.engine.impl.util.CollectionUtil.singletonMap;
import static org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider.JSON_PATCH_MAPPING_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class ExtensionsVariablesMappingProviderTest {

    private final static String EXPRESSION_TEST_FILES_PATH = "src/test/resources/expressions/";

    private final static String JSONPATCH_TEST_FILES_PATH = "src/test/resources/jsonPatch/";

    @InjectMocks
    @Autowired
    private ExtensionsVariablesMappingProvider variablesMappingProvider;

    @Mock
    private ProcessExtensionService processExtensionService;

    @Test
    public void calculateInputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskVarMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        given(execution.getVariable("process_variable_inputmap_1")).willReturn("new-input-value");
        given(execution.getVariable("property-with-no-default-value")).willReturn(null);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
            processExtensions);

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

    private DelegateExecution buildExecution(Extension extensions) {
        return buildExecution(extensions, "simpleTask");
    }

    private DelegateExecution buildExecution(Extension extensions, String taskName) {
        DelegateExecution execution = mock(DelegateExecution.class);
        String processDefinitionId = "procDefId";
        given(execution.getProcessDefinitionId()).willReturn(processDefinitionId);
        given(execution.getCurrentActivityId()).willReturn(taskName);

        given(processExtensionService.getExtensionsForId(processDefinitionId)).willReturn(extensions);
        return execution;
    }

    @Test
    public void calculateInputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskVariableNoMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
            processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        Map<String, Object> variables = map(
            "var-one", "one",
            "var-two", 2
        );

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

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableEmptyMapping"));

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

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableEmptyMappingWithContants"));

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1_2", "constant_2_value"),
                tuple("process_constant_inputmap_2", "constant_value"));

    }

    @Test
    public void calculateOutputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskVarMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        Map<String, Object> entityVariables = singletonMap("task_output_variable_name_1", "var-one");

        ExpressionResolverHelper.setExecutionVariables(execution, entityVariables);

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
        ExpressionResolver expressionResolver = mock(ExpressionResolver.class);
        given(expressionResolver.containsExpression(any())).willReturn(false);
        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-no-mapping-extensions.json"),
            ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableNoMapping"));

        Map<String, Object> taskVariables = map(
            "task_output_variable_name_1", "var-one",
            "non-mapped-output_variable_name_2", "var-two"
        );

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            taskVariables);

        //then
        assertThat(outPutVariables).isEqualTo(taskVariables);
    }

    @Test
    public void calculateOutputVariablesShouldNotPassAnyVariablesWhenTheMappingIsEmpty() throws Exception {

        //given
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File("src/test/resources/task-variable-empty-mapping-extensions.json"),
            ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableEmptyMapping"));

        Map<String, Object> taskVariables = map(
            "task_output_variable_name_1", "var-one",
            "non-mapped-output_variable_name_2", "var-two"
        );

        //when
        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            taskVariables);

        //then
        assertThat(outputVariables).isEmpty();
    }

    @Test
    public void calculateOutputVariablesShouldConvertValueFromDoubleToBigDecimal() {

        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String doubleOutputName = "double-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(doubleOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        double doubleValue = 2.3;
        BigDecimal bigDecimalValue = BigDecimal.valueOf(doubleValue);
        Map<String, Object> availableVariables = singletonMap(doubleOutputName, doubleValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            availableVariables);

        //then
        assertThat(outPutVariables.get(processVariableName)).isEqualTo(bigDecimalValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertValueFromIntegerToBigDecimal() {

        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String integerOutputName = "integer-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        Integer intValue = 2;
        BigDecimal bigDecimalValue = BigDecimal.valueOf(intValue);

        Map<String, Object> availableVariables = singletonMap(integerOutputName, intValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            availableVariables);

        //then
        assertThat(outPutVariables.get(processVariableName)).asInstanceOf(InstanceOfAssertFactories.BIG_DECIMAL).isEqualByComparingTo(bigDecimalValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertLocalDateToDate() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "localdate-process-variable";
        String integerOutputName = "date-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition localDateProcessVariable = new VariableDefinition();
        localDateProcessVariable.setType("date");
        localDateProcessVariable.setName(processVariableName);
        localDateProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, localDateProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        LocalDate localDateValue = LocalDate.now();
        Date dateValue = Date.from(localDateValue.atStartOfDay(ZoneOffset.UTC).toInstant());

        Map<String, Object> availableVariables = singletonMap(integerOutputName, localDateValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            availableVariables);

        //then
        assertThat(outPutVariables.get(processVariableName)).asInstanceOf(InstanceOfAssertFactories.DATE).isEqualTo(dateValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertLocalDateTimeToDate() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "localdate-process-variable";
        String integerOutputName = "date-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition localDateProcessVariable = new VariableDefinition();
        localDateProcessVariable.setType("date");
        localDateProcessVariable.setName(processVariableName);
        localDateProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, localDateProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        LocalDateTime localDateTimeValue = LocalDateTime.now();
        Date dateValue = Date.from(localDateTimeValue.atZone(ZoneOffset.UTC).toInstant());

        Map<String, Object> availableVariables = singletonMap(integerOutputName, localDateTimeValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            availableVariables);

        //then
        assertThat(outPutVariables.get(processVariableName)).asInstanceOf(InstanceOfAssertFactories.DATE).isEqualTo(dateValue);
    }


    @Test
    public void calculateOutputVariablesShouldConvertValueFromStringToBigDecimal() {

        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String stringOutputName = "string-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(stringOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        String stringValue = "4.1";
        Map<String, Object> availableVariables = singletonMap(stringOutputName, stringValue);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, extension);
        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);
        ExpressionResolverHelper.setExecutionVariables(execution, availableVariables);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            availableVariables);

        //then
        assertThat(outPutVariables.get(processVariableName)).asInstanceOf(InstanceOfAssertFactories.BIG_DECIMAL).isEqualByComparingTo(stringValue);
    }

    @Test
    public void should_calculateOutputVariables_when_usingJsonPatchVariablesMapping() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "jsonPatch-in-mapping-output.json", "Process_jsonPatchMappingOutput");

        Map<String, Object> outputVariables = executeCalculateOutputVariables(execution);

        assertOutputVariables(outputVariables);
    }

    @Test
    public void should_calculateOutputVariables_when_jsonPatchOriginalVariableIsEmptyJson() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "jsonPatch-in-mapping-output.json", "Process_jsonPatchMappingOutput");
        when(execution.getVariable(eq("process_variable_empty_json"))).thenReturn(NullNode.getInstance());

        Map<String, Object> outputVariables = executeCalculateOutputVariables(execution);

        assertOutputVariables(outputVariables);
    }

    private Map<String, Object> executeCalculateOutputVariables(DelegateExecution execution) {
        return variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            map(
                "task_input_variable_name_1", "variable_value_1",
                "task_input_variable_name_2", Map.of("firstname", "Bob")));
    }

    private void assertOutputVariables(Map<String, Object> outputVariables) {
        Map<String, Object> expectedAddress0 = Map.of("street", "123 Main St");
        Map<String, Object> expectedAddress1 = Map.of("street", "456 Elm St");
        Map<String, Object> expectedAddress2 = Map.of("street", "Ha-Ha Road", "new-street-field", "Street Name");
        Map<String, Object> expectedAddress3 = Map.of("address", Map.of("street", "Ha-Ha Road"));
        Map<String, Object> expectedAddress5 = Map.of("street", "123 Main St", "propertyFromVariable", "Street Name");
        Map<String, Object> expectedAddress6 = Map.of("street", "456 Elm St", "propertyFromVariable", "Street Name");

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_variable_person_simple_cases", Map.of("firstname", "Bob", "lastname", "Miracle",
                    "addresses", List.of(expectedAddress0, expectedAddress1))),
                tuple("process_variable_empty_json", Map.of("firstname", "John", "address", Map.of("street", "Ha-Ha Road"))),
                tuple("variable_invalid_object", Map.of("street2", "Ha-Ha Road")),
                tuple("process_variable_person_array_cases", Map.of("firstname", "Bob",
                    "addresses", List.of(expectedAddress0, expectedAddress2, expectedAddress3))),
                tuple("process_variable_person_variable_cases", Map.of("firstname", "Bob", "propertyFromVariable", "Miracle", "process_variable_name_equals_value", "Miracle",
                    "addresses", List.of(expectedAddress5, expectedAddress6, expectedAddress3))),
                tuple("process_variable_empty_inner_array", Map.of("people", List.of(Map.of("name", "John"))))
            );
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_JsonPatchDefinitionIsInvalid() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "invalid-jsonPatch-in-mapping-output.json","Process_jsonPatchMappingOutput");
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () -> variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            null));

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsInvalidPathVariableType() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "jsonPatch-invalid-path-variable-type.json","Process_jsonPatchMappingOutput");
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () -> variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            null));

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat("Variable process_variable_json of type 'json' is not allowed in JsonPatch mapping. Only string and integer types are allowed").isEqualTo(exception.getCause().getMessage());
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsEmptyPathVariable() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "jsonPatch-invalid-path-variable-empty.json","Process_jsonPatchMappingOutput");
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () -> variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            null));

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat("Path variable $process_variable_empty used in JsonPatch mapping should not be empty").isEqualTo(exception.getCause().getMessage());
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsUndefinedPathVariable() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(JSONPATCH_TEST_FILES_PATH, "jsonPatch-invalid-path-variable-undefined.json","Process_jsonPatchMappingOutput");
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () -> variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            null));

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat("Path variable $undefined used in JsonPatch mapping is not defined for the current process").isEqualTo(exception.getCause().getMessage());
    }

    private DelegateExecution initExpressionResolverTest(String fileName, String processDefinitionKey) throws IOException {
        return initExpressionResolverTest(fileName, processDefinitionKey, new ArrayList<>());
    }

    private DelegateExecution initExpressionResolverTest(String filePath, String fileName, String processDefinitionKey) throws IOException {
        return initExpressionResolverTest(filePath, fileName, processDefinitionKey, new ArrayList<>());
    }

    private DelegateExecution initExpressionResolverTest(String fileName, String processDefinitionKey,
                                                         List<CustomFunctionProvider> customFunctionProviders) throws IOException {
        return initExpressionResolverTest(EXPRESSION_TEST_FILES_PATH, fileName, processDefinitionKey, customFunctionProviders);
    }


    private DelegateExecution initExpressionResolverTest(String filePath, String fileName, String processDefinitionKey,
                                                         List<CustomFunctionProvider> customFunctionProviders) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(new File(filePath + fileName),
            ProcessExtensionModel.class);

        DelegateExecution execution = buildExecution(extensions.getExtensions(processDefinitionKey));
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
            extensions.getExtensions(processDefinitionKey),
            customFunctionProviders,new ArrayList<>());

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        return execution;
    }

    @Test
    public void should_notSubstituteExpressions_when_thereAreNoExpressions() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("no-expression.json", "Process_NoExpression");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                                                         Map.Entry::getValue)
                                             .containsOnly(tuple("process_constant_1", "constant_1_value"),
                                                           tuple("process_constant_2", "constant_2_value"),
                                                           tuple("task_input_variable_name_1", "variable_value_1"),
                                                           tuple("task_input_variable_name_2", "static_value_1"));

        Map<String, Object> taskVariables = map(
            "task_input_variable_name_1", "variable_value_1",
            "task_input_variable_name_2", "static_value_2"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            taskVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInConstants() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-constants.json",
            "Process_expression-in-constants");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey,
                Map.Entry::getValue)
            .containsOnly(tuple("process_constant_1", "${process_variable_1}"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", "variable_value_1"),
                tuple("task_input_variable_name_2", "static_value_1"));

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            map(
                "task_input_variable_name_1", "variable_value_1",
                "task_input_variable_name_2", "static_value_2"
            ));

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey,
                Map.Entry::getValue)
            .containsOnly(tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2"));
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInInputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-input-value.json",
            "Process_expressionMappingInputValue");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", "variable_value_1"),
                tuple("task_input_variable_name_2", "variable_value_1"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInInputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-input-variable.json",
            "Process_expressionMappingInputVariable");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_2", "static_value_1"));
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInOutputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            map(
                "task_input_variable_name_1", "variable_value_1",
                "task_input_variable_name_2", "static_value_2"
            ));

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2"));
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInOutputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-variable.json",
            "Process_expressionMappingOutputVariable");

        Map<String, Object> taskVariables = map(
            "task_input_variable_name_1", "variable_value_1",
            "task_input_variable_name_2", "static_value_2"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            taskVariables);

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_variable_4", "static_value_2"));
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInProperties() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-properties.json",
            "Process_expressionProperty");

        Map<String, Object> var1 = map(
            "prop1", "property 1",
            "prop2", "expressionResolved",
            "prop3", asList("1", "this expressionResolved is OK", "2")
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", var1),
                tuple("task_input_variable_name_2", "static_value_1"));
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_expressionIsOutputMapping() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue");

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() ->  variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                map(
                    "task_input_variable_name_1", "variable_value_1",
                    "task_input_variable_name_2", "${expression}"
                )));
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_expressionIsOutputMappingUsingMapAll() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-all-output-value.json",
            "Process_expressionMappingOutputValue");

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() ->  variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                map(
                    "task_input_variable_name_1", "variable_value_1",
                    "task_input_variable_name_2", "${expression}"
                )));
    }

    @Test
    public void should_returnResolveToNull_when_resolvingVariablesExpressionInTask() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue");

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
            null);

        assertThat(outputVariables).containsOnlyKeys("process_variable_4").containsValue(null);
    }

    @Test
    public void should_returnEmptyOutputMapping_when_thereIsAnEmptyValueInOutputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("no-value-in-output-mapping-variable.json",
            "Process_noValueOutputMappingVariable");

        Map<String, Object> outputMapping = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution), singletonMap("not_matching_variable", "variable_value_1"));

        assertThat(outputMapping).isEmpty();
    }

    @Test
    public void should_returnAllExecutionVariables_when_calculatingAnImplicitInputMapping()
        throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Two");
        Map<String, Object> executionVariables = map("process_variable_1", "value1", "process_variable_2", "value2");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
            processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        given(execution.getVariables()).willReturn(executionVariables);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isEqualTo(executionVariables);
    }

    @Test
    public void should_returnAllTaskVariables_when_calculatingAnImplicitOutputMapping()
        throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_One");
        Map<String, Object> taskVariables = map("task_variable_1", "value1", "task_variable_2", "value2");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        ExpressionResolverHelper.setExecutionVariables(execution, taskVariables);

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables);

        assertThat(outputVariables).isEqualTo(taskVariables);
    }


    @Test
    public void should_calculateInputVariables_when_variableIsInProcessInstanceContextButNotDefinedInExtensions()
        throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Three");
        given(execution.getVariable("process_variable_inputmap_1")).willReturn("new-input-value");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution,
            processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables.get("task_input_variable_name_1")).isEqualTo("new-input-value");
    }

    @Test
    public void should_calculateOutputVariables_when_variableIsInProcessInstanceContextButNotDefinedInExtensions()
        throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class);

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Three");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider,
            "expressionResolver",
            expressionResolver);

        Map<String, Object> entityVariables = singletonMap("task_output_variable_name_1", "var-one");

        ExpressionResolverHelper.setExecutionVariables(execution, entityVariables);
        given(execution.getVariable("process_variable_outputmap_1")).willReturn(("process-value"));

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            Map.of("task_output_variable_name_1", "task-value"));

        assertThat(outputVariables.get("process_variable_outputmap_1")).isEqualTo("task-value");
    }

    @Test
    public void should_resolveExpressionsBasedInExecutionContext_when_calculatingOutputMappingAndHasExecution()
        throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-based-in-execution-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue");

        VariableInstanceEntityImpl variableInstance = new VariableInstanceEntityImpl();
        variableInstance.setTypeName("string");
        variableInstance.setType(new StringType(255));
        variableInstance.setValue("variableValue");
        given(execution.getVariableInstance("process_variable_3")).willReturn(variableInstance);

        Map<String, Object> outputMapping = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            null);

        assertThat(outputMapping).containsOnlyKeys("process_variable_1", "process_variable_2");
        assertThat(outputMapping.get("process_variable_1")).isNotEqualTo("${authenticatedUserId}");
        assertThat(outputMapping.get("process_variable_2")).isEqualTo("This is the variableValue");
    }

    @Test
    public void should_substituteExpressions_when_customExpression() throws Exception {
        List<CustomFunctionProvider> customFunctionProviders = List.of(new TestCustomFunctionProvider());

        DelegateExecution execution = initExpressionResolverTest("custom-expression-in-mapping-input-value.json",
            "Process_expressionMappingInputValue", customFunctionProviders);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet()).extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", 1),
                tuple("task_input_variable_name_2", 2));
    }

    public static class TestCustomFunctionProvider implements CustomFunctionProvider {

        public static Integer plusOne(Integer number) {
            return number + 1;
        }

        @Override
        public void addCustomFunctions(ActivitiElContext elContext) {
            try {
                elContext.setFunction("", "plusOne",
                    TestCustomFunctionProvider.class.getMethod("plusOne", Integer.class));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
