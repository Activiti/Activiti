/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.activiti.engine.impl.util.CollectionUtil.mapOfClass;
import static org.activiti.engine.impl.util.CollectionUtil.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.BigDecimalVariableType;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
public class ProcessVariablesPayloadValidatorTest {

    @Mock
    private ProcessExtensionService processExtensionService;

    @Mock
    private RepositoryService repositoryService;

    private DateFormatterProvider dateFormatterProvider = new DateFormatterProvider(
        "yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]"
    );
    private JsonMapper jsonMapper = new JsonMapper();
    private VariableNameValidator variableNameValidator = new VariableNameValidator();

    private ProcessVariablesPayloadValidator processVariablesValidator;
    private VariableValidationService variableValidationService;
    private BpmnModel bpmnModel;

    private ExpressionResolver expressionResolver = new ExpressionResolver(
        new ExpressionManager(),
        jsonMapper,
        new DefaultDelegateInterceptor()
    );

    @BeforeEach
    public void setUp() {
        VariableDefinition variableDefinitionName = new VariableDefinition();
        variableDefinitionName.setName("name");
        variableDefinitionName.setType("string");

        VariableDefinition variableDefinitionAge = new VariableDefinition();
        variableDefinitionAge.setName("age");
        variableDefinitionAge.setType("integer");

        VariableDefinition variableDefinitionSubscribe = new VariableDefinition();
        variableDefinitionSubscribe.setName("subscribe");
        variableDefinitionSubscribe.setType("boolean");

        VariableDefinition variableDefinitionDate = new VariableDefinition();
        variableDefinitionDate.setName("mydate");
        variableDefinitionDate.setType("date");

        VariableDefinition variableDefinitionDatetime = new VariableDefinition();
        variableDefinitionDatetime.setName("mydatetime");
        variableDefinitionDatetime.setType("datetime");

        VariableDefinition variableDefinitionBigdecimal = new VariableDefinition();
        variableDefinitionBigdecimal.setName("amount");
        variableDefinitionBigdecimal.setType("bigdecimal");

        VariableDefinition variableDefinitionJson = new VariableDefinition();
        variableDefinitionJson.setName("metadata");
        variableDefinitionJson.setType("json");

        VariableDefinition variableDefinitionApprovalOutcome = new VariableDefinition();
        variableDefinitionApprovalOutcome.setName("approvalOutcome");
        variableDefinitionApprovalOutcome.setType("string");

        VariableDefinition variableDefinitionUnmappedOutcome = new VariableDefinition();
        variableDefinitionUnmappedOutcome.setName("unmappedOutcome");
        variableDefinitionUnmappedOutcome.setType("string");

        variableValidationService = new VariableValidationService(
            mapOfClass(
                VariableType.class,
                "boolean",
                new JavaObjectVariableType(Boolean.class),
                "string",
                new JavaObjectVariableType(String.class),
                "integer",
                new JavaObjectVariableType(Integer.class),
                "bigdecimal",
                new BigDecimalVariableType(),
                "json",
                new JsonObjectVariableType(jsonMapper),
                "file",
                new JsonObjectVariableType(jsonMapper),
                "date",
                new DateVariableType(Date.class, dateFormatterProvider),
                "datetime",
                new DateVariableType(Date.class, dateFormatterProvider)
            )
        );

        processVariablesValidator = new ProcessVariablesPayloadValidator(
            dateFormatterProvider,
            processExtensionService,
            variableValidationService,
            variableNameValidator,
            expressionResolver,
            repositoryService
        );
        Extension extension = new Extension();
        extension.setProperties(
            mapOfClass(
                VariableDefinition.class,
                "name",
                variableDefinitionName,
                "age",
                variableDefinitionAge,
                "subscribe",
                variableDefinitionSubscribe,
                "mydate",
                variableDefinitionDate,
                "mydatetime",
                variableDefinitionDatetime,
                "amount",
                variableDefinitionBigdecimal,
                "metadata",
                variableDefinitionJson,
                "approvalOutcome",
                variableDefinitionApprovalOutcome,
                "unmappedOutcome",
                variableDefinitionUnmappedOutcome
            )
        );
        Mapping approvalOutcomeMapping = new Mapping();
        approvalOutcomeMapping.setType(Mapping.SourceMappingType.VALUE);
        approvalOutcomeMapping.setValue("${approvalOutcome.name}");
        ProcessVariablesMapping startMapping = new ProcessVariablesMapping();
        startMapping.setOutputs(Map.of("approvalOutcome", approvalOutcomeMapping));
        extension.setMappings(Map.of("startEvent", startMapping));

        StartEvent firstStartEvent = new StartEvent();
        firstStartEvent.setId("firstStartEvent");
        Process firstProcess = new Process();
        firstProcess.setId("firstProcess");
        firstProcess.setInitialFlowElement(firstStartEvent);

        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        Process process = new Process();
        process.setId("targetProcess");
        process.setInitialFlowElement(startEvent);
        bpmnModel = new BpmnModel();
        bpmnModel.addProcess(firstProcess);
        bpmnModel.addProcess(process);

        given(processExtensionService.getExtensionsForId(any())).willReturn(extension);
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithWrongType() {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(map("name", "Alice", "age", "24", "subscribe", "false"))
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("subscribe, age");
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithNameWrongType() {
        String expectedTypeErrorMessage = "age";

        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(
                        map(
                            "name",
                            "Alice",
                            "gender",
                            "female",
                            "age",
                            "24",
                            "subs",
                            true,
                            "subscribe",
                            true,
                            "mydate",
                            "2019-08-26T10:20:30.000Z"
                        )
                    )
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining(expectedTypeErrorMessage);
    }

    @Test
    public void should_returnError_when_setVariablesWithWrongDateFormat() {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(singletonMap("mydate", "2019-08-26TT10:20:30.000Z"))
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_returnError_when_setVariablesWithWrongDatetimeFormat() {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(singletonMap("mydatetime", "2019-08-26TT10:20:30.000Z"))
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_returnErrorList_when_setVariableWithWrongCharactersInName() {
        String expectedTypeErrorMessage = "gen-der";

        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables().withVariables(map("name", "Alice", "gen-der", "female")).build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining(expectedTypeErrorMessage);
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInStringVariable() {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(map("expression_string", "${variable}", "variable", "no-expression"))
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInObjectVariable()
        throws IOException {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(
                        map(
                            "expression_object",
                            jsonMapper.createObjectNode().put("attr1", "value1").put("attr2", "${variable}"),
                            "variable",
                            "no-expression"
                        )
                    )
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInList() throws IOException {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(
                        map(
                            "expression_list",
                            jsonMapper
                                .createObjectNode()
                                .put("attr1", "value1")
                                .set("attr2", jsonMapper.createArrayNode().add("1").add("${variable}").add("2")),
                            "variable",
                            "no-expression"
                        )
                    )
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInMap() {
        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(singletonMap("expression_map", singletonMap("expression_string", "${variable}")))
                    .build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("expression");
    }

    @Test
    public void should_success_when_valueForJavaObjectIsNull() {
        assertDoesNotThrow(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables()
                    .withVariables(map("name", null, "age", null, "subscribe", null))
                    .build(),
                "10"
            )
        );
    }

    @Test
    public void should_success_when_startProcessPayloadHasNullValuesForAllTypes() {
        givenStartOutputMapping();

        assertDoesNotThrow(() ->
            processVariablesValidator.checkStartProcessPayloadVariables(
                ProcessPayloadBuilder.start()
                    .withVariables(
                        map(
                            "name",
                            null,
                            "age",
                            null,
                            "subscribe",
                            null,
                            "mydate",
                            null,
                            "mydatetime",
                            null,
                            "amount",
                            null,
                            "metadata",
                            null
                        )
                    )
                    .build(),
                "10"
            )
        );
    }

    @Test
    public void should_useStartedProcessDefinitionForStartOutputMapping() {
        givenStartOutputMapping();

        assertDoesNotThrow(() ->
            processVariablesValidator.checkStartProcessPayloadVariables(
                ProcessPayloadBuilder.start()
                    .withVariables(singletonMap("approvalOutcome", map("id", "approved", "name", "Approved")))
                    .build(),
                "10"
            )
        );
    }

    @Test
    public void should_returnError_when_startProcessPayloadHasUnmappedWrongTypeWithOutputMapping() {
        givenStartOutputMapping();

        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkStartProcessPayloadVariables(
                ProcessPayloadBuilder.start()
                    .withVariables(
                        map(
                            "approvalOutcome",
                            map("id", "approved", "name", "Approved"),
                            "unmappedOutcome",
                            map("id", "rejected", "name", "Rejected")
                        )
                    )
                    .build(),
                "10"
            )
        );

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Variables fail type validation: unmappedOutcome");
    }

    @Test
    public void should_returnErrors_when_startProcessPayloadHasInvalidNameAndExpression() {
        givenStartOutputMapping();

        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkStartProcessPayloadVariables(
                ProcessPayloadBuilder.start()
                    .withVariables(map("invalid-name", "value", "expression", "${value}"))
                    .build(),
                "10"
            )
        );

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid-name")
            .hasMessageContaining("expression");
    }

    @Test
    public void should_normalizeDate_when_startProcessPayloadHasDeclaredDateVariable() {
        givenStartOutputMapping();

        StartProcessPayload startProcessPayload = ProcessPayloadBuilder.start()
            .withVariables(singletonMap("mydate", "2019-08-26T10:20:30.000Z"))
            .build();

        processVariablesValidator.checkStartProcessPayloadVariables(startProcessPayload, "10");

        assertThat(startProcessPayload.getVariables().get("mydate")).isInstanceOf(Date.class);
    }

    @Test
    public void should_returnError_when_nonStartPayloadsHaveObjectForDeclaredStringVariable() {
        Map<String, Object> variables = singletonMap("approvalOutcome", map("id", "approved", "name", "Approved"));

        Throwable throwable = catchThrowable(() ->
            processVariablesValidator.checkPayloadVariables(
                ProcessPayloadBuilder.setVariables().withVariables(variables).build(),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("approvalOutcome");

        throwable = catchThrowable(() ->
            processVariablesValidator.checkStartMessagePayloadVariables(
                new StartMessagePayload("message", null, variables),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("approvalOutcome");

        throwable = catchThrowable(() ->
            processVariablesValidator.checkReceiveMessagePayloadVariables(
                new ReceiveMessagePayload("message", null, variables),
                "10"
            )
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("approvalOutcome");

        throwable = catchThrowable(() ->
            processVariablesValidator.checkSignalPayloadVariables(new SignalPayload("signal", variables), "10")
        );

        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining("approvalOutcome");
    }

    private void givenStartOutputMapping() {
        org.activiti.engine.repository.ProcessDefinition processDefinition = mock(
            org.activiti.engine.repository.ProcessDefinition.class
        );
        given(processDefinition.getKey()).willReturn("targetProcess");
        given(repositoryService.getProcessDefinition(any())).willReturn(processDefinition);
        given(repositoryService.getBpmnModel(any())).willReturn(bpmnModel);
    }
}
