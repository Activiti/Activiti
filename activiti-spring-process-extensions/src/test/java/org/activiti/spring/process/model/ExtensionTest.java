package org.activiti.spring.process.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashMap;

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
    public void hasEmptyInputsMappingShouldReturnTrueWhenInputsMapIsEmpty() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyInputsMapping("elementId")).isTrue();
    }

    @Test
    public void hasEmptyOutputsMappingShouldReturnTrueWhenOutputsMapIsEmpty() {
        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyOutputsMapping("elementId")).isTrue();
    }

    @Test
    public void hasMappingShouldReturnTrueWhenThereIsMapping() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasMapping("elementId")).isTrue();
    }

    @Test
    public void hasMappingShouldReturnFalseWhenThereIsNoMapping() {
        Extension extension = new Extension();

        assertThat(extension.hasMapping("elementId")).isFalse();
    }

}
