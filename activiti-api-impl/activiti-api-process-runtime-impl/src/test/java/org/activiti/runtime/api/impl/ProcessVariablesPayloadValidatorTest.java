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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;
import org.activiti.spring.process.variable.types.DateVariableType;
import org.activiti.spring.process.variable.types.JavaObjectVariableType;
import org.activiti.spring.process.variable.types.JsonObjectVariableType;
import org.activiti.spring.process.variable.types.VariableType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ProcessVariablesPayloadValidatorTest {
 
    @Mock
    private Map<String, ProcessExtensionModel> processExtensionModelMap;
    
    private DateFormatterProvider dateFormatterProvider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");
    private ObjectMapper objectMapper = new ObjectMapper();
    private VariableNameValidator variableNameValidator = new VariableNameValidator();
    
    private Map<String, VariableType> variableTypeMap;
    
    private ProcessVariablesPayloadValidator processVariablesValidator;
    private VariableValidationService variableValidationService;

    @Before
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

        Map<String, VariableDefinition> properties = new HashMap<>();
        properties.put("name", variableDefinitionName);
        properties.put("age", variableDefinitionAge);
        properties.put("subscribe", variableDefinitionSubscribe);
        properties.put("mydate", variableDefinitionDate);

       
        variableTypeMap = new HashMap<>();
        variableTypeMap.put("boolean", new JavaObjectVariableType(Boolean.class));
        variableTypeMap.put("string", new JavaObjectVariableType(String.class));
        variableTypeMap.put("integer", new JavaObjectVariableType(Integer.class));
        variableTypeMap.put("json", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("file", new JsonObjectVariableType(objectMapper));
        variableTypeMap.put("date", new DateVariableType(Date.class, dateFormatterProvider));
        
        variableValidationService = new VariableValidationService(variableTypeMap); 
        
        processVariablesValidator = new ProcessVariablesPayloadValidator(dateFormatterProvider,
                                                                         processExtensionModelMap,
                                                                         variableValidationService,
                                                                         variableNameValidator);
        
        Extension extension = new Extension();
        extension.setProperties(properties);

        ProcessExtensionModel processExtensionModel = new ProcessExtensionModel();
        processExtensionModel.setId("1");
        processExtensionModel.setExtensions(extension);
        
        given(processExtensionModelMap.get(any()))
                   .willReturn(processExtensionModel);
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithWrongType() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", "24");
        variables.put("subscribe", "false");

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(variables)
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        
        assertThat(throwable.getMessage())
            .contains("subscribe",
                      "age");
    }

    @Test
    public void should_returnErrorList_when_setVariablesWithNameWrongType() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("gender", "female");
        variables.put("age", "24");
        variables.put("subs", true);
        variables.put("subscribe", true);
        variables.put("mydate", "2019-08-26T10:20:30.000Z");
        
        String expectedTypeErrorMessage = "age";

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(variables)
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class); 
        
        assertThat(throwable.getMessage())
            .contains(expectedTypeErrorMessage);
        
    }
  
    @Test
    public void should_returnError_when_setVariablesWithWrongDateFormat() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("mydate", "2019-08-26TT10:20:30.000Z");

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(variables)
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);
        
    }
    
    @Test
    public void should_returnErrorList_when_setVariableWithWrongCharactersInName() throws Exception {

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("gen-der", "female");
  
        String expectedTypeErrorMessage = "gen-der";

        Throwable throwable = catchThrowable(() -> processVariablesValidator.checkPayloadVariables(
                                                                            ProcessPayloadBuilder
                                                                                .setVariables()
                                                                                .withVariables(variables)
                                                                                .build(),
                                                                            "10"));

        assertThat(throwable).isInstanceOf(IllegalStateException.class); 
        
        assertThat(throwable.getMessage())
            .contains(expectedTypeErrorMessage);
        
    }
}