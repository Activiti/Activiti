package org.activiti.spring.process.model;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExtensionTest {

    @Mock
    private ProcessVariablesMapping processVariablesMapping;

    @Before
    public void setUp() {
        initMocks(this);
        given(processVariablesMapping.getInputs()).willReturn(Collections.emptyMap());
        given(processVariablesMapping.getOutputs()).willReturn(Collections.emptyMap());
    }

    @Test
    public void hasEmptyInputsMappingShouldReturnFalseWhenInputsMapIsEmpty(){
        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyInputsMapping("elementId")).isTrue();
    }

    @Test
    public void hasEmptyOutputsMappingShouldReturnFalseWhenOutputsMapIsEmpty(){
        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyOutputsMapping("elementId")).isTrue();
    }

    @Test
    public void hasNoMappingShouldReturnFalseWhenThereIsMapping(){
        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasNoMapping("elementId")).isFalse();
    }

}
