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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Optional;
import org.activiti.spring.process.model.ProcessVariablesMapping.MappingType;
import org.activiti.spring.process.model.TemplateDefinition.TemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;

public class ExtensionTest {

    @Mock
    private ProcessVariablesMapping processVariablesMapping;

    private final static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    }

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void should_bothHasMappingTypeInputsAndOutputsReturnTrue_when_thereIsMappingTypeMAP_ALL() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isTrue();
        assertThat(extension.shouldMapAllOutputs("elementId")).isTrue();

    }

    @Test
    public void should_onlyHasMappingTypeInputsReturnTrue_when_thereIsMappingTypeMAP_ALL_INPUTS() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL_INPUTS);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isTrue();
        assertThat(extension.shouldMapAllOutputs("elementId")).isFalse();

    }

    @Test
    public void should_onlyHasMappingTypeOutputsReturnTrue_when_thereIsMappingTypeMAP_ALL_OUTPUTS() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL_OUTPUTS);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isFalse();
        assertThat(extension.shouldMapAllOutputs("elementId")).isTrue();

    }

    @Test
    public void should_hasMappingReturnTrue_when_thereIsMapping() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasMapping("elementId")).isTrue();
    }

    @Test
    public void should_hasMappingReturnFalse_when_thereIsNoMapping() {
        Extension extension = new Extension();
        assertThat(extension.hasMapping("elementId")).isFalse();
    }

    @Test
    public void findAssigneeTemplateForTask_should_returnTemplate_when_templateIsDefined() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            TemplateDefinition templateDefinition = extensions.findAssigneeTemplateForTask("myTaskId1")
                .orElse(null);

            //then
            assertThat(templateDefinition)
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue)
                .containsExactly(
                    TemplateType.FILE,
                    "https://github.com/leemunroe/responsive-html-email-template/blob/master/email.html"
                );
        }
    }

    @Test
    public void findAssigneeTemplateForTask_should_returnEmpty_when_noTemplatesForTask() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            Optional<TemplateDefinition> templateDefinition = extensions.findAssigneeTemplateForTask("anyTaskWithoutAssigneeTemplate");

            //then
            assertThat(templateDefinition).isEmpty();
        }
    }

    @Test
    public void findAssigneeTemplateForTask_should_returnEmpty_when_onlyCandidateTemplate() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            Optional<TemplateDefinition> templateDefinition = extensions.findAssigneeTemplateForTask("myTaskId3");

            //then
            assertThat(templateDefinition).isEmpty();
        }
    }

    @Test
    public void findCandidateTemplateForTask_should_returnTemplate_when_templateIsDefined() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            TemplateDefinition templateDefinition = extensions.findCandidateTemplateForTask("myTaskId1")
                .orElse(null);

            //then
            assertThat(templateDefinition)
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue)
                .containsExactly(
                    TemplateType.FILE,
                    "https://github.com/leemunroe/responsive-html-email-template/blob/master/email-inlined.html"
                );
        }
    }

    @Test
    public void findCandidateTemplateForTask_should_returnEmpty_when_onlyAssigneeCandidate() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            Optional<TemplateDefinition> templateDefinition = extensions.findCandidateTemplateForTask("myTaskId2");

            //then
            assertThat(templateDefinition).isEmpty();
        }
    }

    @Test
    public void findCandidateTemplateForTask_should_returnEmpty_when_noTemplatesForTask() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            Optional<TemplateDefinition> templateDefinition = extensions.findCandidateTemplateForTask("anyTaskWithoutTemplates");

            //then
            assertThat(templateDefinition).isEmpty();
        }
    }
}
