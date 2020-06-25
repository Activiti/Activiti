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
package org.activiti.spring.process.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExtensionTest {

    @Mock
    private ProcessVariablesMapping processVariablesMapping;

    @BeforeEach
    void setUp() {
        initMocks(this);
        given(processVariablesMapping.getInputs()).willReturn(emptyMap());
        given(processVariablesMapping.getOutputs()).willReturn(emptyMap());
    }

    @Test
    void should_hasEmptyInputsMappingReturnTrue_when_InputsMapIsEmpty() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyInputsMapping("elementId")).isTrue();
    }

    @Test
    void should_hasEmptyOutputsMappingReturnTrue_when_OutputsMapIsEmpty() {
        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyOutputsMapping("elementId")).isTrue();
    }

    @Test
    void should_hasMappingReturnTrue_when_ThereIsMapping() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasMapping("elementId")).isTrue();
    }

    @Test
    void should_hasMappingReturnFalse_when_ThereIsNoMapping() {
        Extension extension = new Extension();

        assertThat(extension.hasMapping("elementId")).isFalse();
    }

    @Test
    void should_hasEmptyMappingReturnTrue_when_thereIsMappingWithNoInputsOrNoOutputs() {
        given(processVariablesMapping.getInputs()).willReturn(null);
        given(processVariablesMapping.getOutputs()).willReturn(null);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        ProcessVariablesMapping processVariablesMapping = new ProcessVariablesMapping();

        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyMapping("elementId")).isTrue();
    }

    @Test
    void should_hasEmptyMappingReturnFalse_when_thereIsMappingWithInputsOrOutputs() {
        given(processVariablesMapping.getInputs()).willReturn(null);
        given(processVariablesMapping.getOutputs()).willReturn(null);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        ProcessVariablesMapping processVariablesMapping = new ProcessVariablesMapping();

        processVariablesMapping.setInputs(new HashMap<>());
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasEmptyMapping("elementId")).isFalse();
    }

}
