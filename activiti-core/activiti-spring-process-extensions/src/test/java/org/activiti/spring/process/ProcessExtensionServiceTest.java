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
