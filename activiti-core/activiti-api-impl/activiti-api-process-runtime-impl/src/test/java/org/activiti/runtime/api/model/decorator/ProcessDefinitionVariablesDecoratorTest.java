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
package org.activiti.runtime.api.model.decorator;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.runtime.model.impl.ProcessDefinitionImpl;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessDefinitionVariablesDecoratorTest {

    @InjectMocks
    private ProcessDefinitionVariablesDecorator processDefinitionVariablesDecorator;

    @Mock
    private ProcessExtensionService processExtensionService;

    @ParameterizedTest
    @CsvSource({"variables, true", "VARIABLES, true", "else, false"})
    void should_applyToVariablesParam(String includeParam, boolean shouldApply) {
        boolean applies = processDefinitionVariablesDecorator.applies(includeParam);
        assertThat(applies).isEqualTo(shouldApply);
    }

    @Test
    void should_decorateProcessDefinitions() {
        ProcessDefinitionImpl givenProcessDefinition = new ProcessDefinitionImpl();
        givenProcessDefinition.setId("PROC_ID");
        Extension extension = new Extension();
        VariableDefinition givenVariableDefinition = new VariableDefinition();
        givenVariableDefinition.setId("VAR_ID");
        givenVariableDefinition.setName("var1");
        givenVariableDefinition.setDescription("Variable no 1");
        givenVariableDefinition.setType("string");
        givenVariableDefinition.setRequired(true);
        givenVariableDefinition.setDisplay(true);
        givenVariableDefinition.setDisplayName("Variable 1");
        extension.setProperties(Map.of("var1", givenVariableDefinition));

        when(processExtensionService.getExtensionsForId("PROC_ID")).thenReturn(extension);

        ProcessDefinition decoratedProcessDefinition = processDefinitionVariablesDecorator.decorate(givenProcessDefinition);

        assertThat(decoratedProcessDefinition.getVariableDefinitions()).hasSize(1);
        org.activiti.api.process.model.VariableDefinition variableDefinition = decoratedProcessDefinition.getVariableDefinitions().get(0);
        assertThat(variableDefinition.getId()).isEqualTo("VAR_ID");
        assertThat(variableDefinition.getName()).isEqualTo("var1");
        assertThat(variableDefinition.getDescription()).isEqualTo("Variable no 1");
        assertThat(variableDefinition.getType()).isEqualTo("string");
        assertThat(variableDefinition.isRequired()).isEqualTo(true);
        assertThat(variableDefinition.getDisplay()).isEqualTo(true);
        assertThat(variableDefinition.getDisplayName()).isEqualTo("Variable 1");
    }

    @Test
    void should_decorateProcessDefinitionsWithDisplayableVariables() {
        ProcessDefinitionImpl givenProcessDefinition = new ProcessDefinitionImpl();
        givenProcessDefinition.setId("PROC_ID");
        Extension extension = new Extension();
        VariableDefinition givenVariableDefinition1 = new VariableDefinition();
        givenVariableDefinition1.setDisplay(false);
        givenVariableDefinition1.setDisplayName("Variable 1");
        VariableDefinition givenVariableDefinition2 = new VariableDefinition();
        givenVariableDefinition1.setDisplay(true);
        givenVariableDefinition1.setDisplayName("Variable 2");
        extension.setProperties(Map.of("var1", givenVariableDefinition1, "var2", givenVariableDefinition2));

        when(processExtensionService.getExtensionsForId("PROC_ID")).thenReturn(extension);

        ProcessDefinition decoratedProcessDefinition = processDefinitionVariablesDecorator.decorate(givenProcessDefinition);

        assertThat(decoratedProcessDefinition.getVariableDefinitions()).hasSize(1);
        org.activiti.api.process.model.VariableDefinition variableDefinition = decoratedProcessDefinition.getVariableDefinitions().get(0);
        assertThat(variableDefinition.getDisplay()).isEqualTo(true);
        assertThat(variableDefinition.getDisplayName()).isEqualTo("Variable 2");
    }
}
