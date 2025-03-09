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
package org.activiti.spring.process;

import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.VariableDefinition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessExtensionServiceTest {

    private ProcessExtensionRepository processExtensionRepository = Mockito.mock(ProcessExtensionRepository.class);


    private ProcessExtensionService processExtensionService = new ProcessExtensionService(processExtensionRepository);

    @Test
    void should_returnTrue_when_variableIsEphemeral() {
        String processDefinitionId = "processDefinitionId";
        String variableName = "variableName";
        Extension extension = mock(Extension.class);
        VariableDefinition variableDefinition = mock(VariableDefinition.class);

        when(processExtensionRepository.getExtensionsForId(processDefinitionId)).thenReturn(Optional.of(extension));
        when(extension.getPropertyByName(variableName)).thenReturn(variableDefinition);
        when(variableDefinition.isEphemeral()).thenReturn(true);

        boolean result = processExtensionService.hasEphemeralVariable(processDefinitionId, variableName);

        assertThat(result).isTrue();
    }

    @Test
    void should_returnFalse_when_extensionIsNull() {
        String processDefinitionId = "processDefinitionId";
        String variableName = "variableName";

        when(processExtensionRepository.getExtensionsForId(processDefinitionId)).thenReturn(Optional.empty());

        boolean result = processExtensionService.hasEphemeralVariable(processDefinitionId, variableName);

        assertThat(result).isFalse();
    }

    @Test
    void should_returnFalse_when_variableDefinitionIsNull() {
        String processDefinitionId = "processDefinitionId";
        String variableName = "variableName";
        Extension extension = mock(Extension.class);

        when(processExtensionRepository.getExtensionsForId(processDefinitionId)).thenReturn(Optional.of(extension));
        when(extension.getPropertyByName(variableName)).thenReturn(null);

        boolean result = processExtensionService.hasEphemeralVariable(processDefinitionId, variableName);

        assertThat(result).isFalse();
    }

    @Test
    void should_returnFalse_when_variableIsNotEphemeral() {
        String processDefinitionId = "processDefinitionId";
        String variableName = "variableName";
        Extension extension = mock(Extension.class);
        VariableDefinition variableDefinition = mock(VariableDefinition.class);

        when(processExtensionRepository.getExtensionsForId(processDefinitionId)).thenReturn(Optional.of(extension));
        when(extension.getPropertyByName(variableName)).thenReturn(variableDefinition);
        when(variableDefinition.isEphemeral()).thenReturn(false);

        boolean result = processExtensionService.hasEphemeralVariable(processDefinitionId, variableName);

        assertThat(result).isFalse();
    }

}
