/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class ProcessVariablesPayloadValidatorTest {

    @Mock
    private ProcessExtensionService processExtensionService;

    private DateFormatterProvider dateFormatterProvider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");
    private ObjectMapper objectMapper = new ObjectMapper();
    private VariableNameValidator variableNameValidator = new VariableNameValidator();

    private ProcessVariablesPayloadValidator processVariablesValidator;
    private VariableValidationService variableValidationService;

    private ExpressionResolver expressionResolver = new ExpressionResolver(new ExpressionManager(),
        objectMapper,
        new DefaultDelegateInterceptor());

    @BeforeEach
    public void setUp() {
        initMocks(this);

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

        variableValidationService = new VariableValidationService(mapOfClass(VariableType.class,
            "boolean", new JavaObjectVariableType(Boolean.class),
            "string", new JavaObjectVariableType(String.class),
            "integer", new JavaObjectVariableType(Integer.class),
            "json", new JsonObjectVariableType(objectMapper),
            "file", new JsonObjectVariableType(objectMapper),
            "date", new DateVariableType(Date.class, dateFormatterProvider),
            "datetime", new DateVariableType(Date.class, dateFormatterProvider)));

        processVariablesValidator = new ProcessVariablesPayloadValidator(dateFormatterProvider,
                                                                         processExtensionService,
                                                                         variableValidationService,
                                                                         variableNameValidator,
                                                                         expressionResolver);
        Extension extension = new Extension();
        extension.setProperties(mapOfClass(VariableDefinition.class,
            "name", variableDefinitionName,
            "age", variableDefinitionAge,
            "subscribe", variableDefinitionSubscribe,
            "mydate", variableDefinitionDate,
            "mydatetime", variableDefinitionDatetime
        ));
        given(processExtensionService.getExtensionsForId(any())).willReturn(extension);
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithWrongType() {

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(map(
                                                                                    "name", "Alice",
                                                                                    "age", "24",
                                                                                    "subscribe", "false"
                                                                                ))
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("subscribe, age");
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithNameWrongType() {
        String expectedTypeErrorMessage = "age";

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(map(
                                                                                    "name", "Alice",
                                                                                    "gender", "female",
                                                                                    "age", "24",
                                                                                    "subs", true,
                                                                                    "subscribe", true,
                                                                                    "mydate", "2019-08-26T10:20:30.000Z"
                                                                                ))
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(expectedTypeErrorMessage);
    }

    @Test
    public void should_returnError_when_setVariablesWithWrongDateFormat() {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(singletonMap(
                                                                                    "mydate", "2019-08-26TT10:20:30.000Z"
                                                                                ))
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_returnError_when_setVariablesWithWrongDatetimeFormat() {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(singletonMap(
                                                                                    "mydatetime", "2019-08-26TT10:20:30.000Z"
                                                                                ))
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_returnErrorList_when_setVariableWithWrongCharactersInName() {
        String expectedTypeErrorMessage = "gen-der";

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(map(
                                                                                    "name", "Alice",
                                                                                    "gen-der", "female"
                                                                                ))
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(expectedTypeErrorMessage);
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInStringVariable() {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
            ProcessPayloadBuilder.setVariables().withVariables(map(
                "expression_string", "${variable}",
                "variable", "no-expression"
            )).build(),
            "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInObjectVariable() throws IOException {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
            ProcessPayloadBuilder.setVariables().withVariables(map(
                "expression_object", objectMapper.createObjectNode()
                    .put("attr1", "value1")
                    .put("attr2", "${variable}"),
                "variable", "no-expression"
            )).build(),
            "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInList() throws IOException {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
            ProcessPayloadBuilder.setVariables().withVariables(map(
                "expression_list", objectMapper.createObjectNode()
                    .put("attr1", "value1")
                    .set("attr2", objectMapper.createArrayNode()
                        .add("1")
                        .add("${variable}")
                        .add("2")
                    )
                ,
                "variable", "no-expression"
            )).build(), "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expression");
    }

    @Test
    public void should_throwIllegalStateException_when_payloadVariableWithExpressionInMap() {
        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
            ProcessPayloadBuilder.setVariables().withVariables(
                singletonMap("expression_map", singletonMap("expression_string", "${variable}")))
                .build(), "10"));

        assertThat(throwable)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expression");
    }
}
