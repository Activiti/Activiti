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
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.activiti.spring.process.model.ProcessVariablesMapping.MappingType;
import org.activiti.spring.process.model.TemplateDefinition.TemplateType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentEnum.ASSIGNEE;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentEnum.CANDIDATES;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentMode.MANUAL;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentMode.SEQUENTIAL;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentType.EXPRESSION;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentType.IDENTITY;
import static org.activiti.spring.process.model.AssignmentDefinition.AssignmentType.STATIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExtensionTest {

    @Mock
    private ProcessVariablesMapping processVariablesMapping;

    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .build();

    @Test
    void should_bothHasMappingTypeInputsAndOutputsReturnTrue_when_thereIsMappingTypeMAP_ALL() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isTrue();
        assertThat(extension.shouldMapAllOutputs("elementId")).isTrue();

    }

    @Test
    void should_onlyHasMappingTypeInputsReturnTrue_when_thereIsMappingTypeMAP_ALL_INPUTS() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL_INPUTS);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isTrue();
        assertThat(extension.shouldMapAllOutputs("elementId")).isFalse();

    }

    @Test
    void should_onlyHasMappingTypeOutputsReturnTrue_when_thereIsMappingTypeMAP_ALL_OUTPUTS() {
        given(processVariablesMapping.getMappingType()).willReturn(MappingType.MAP_ALL_OUTPUTS);

        Extension extension = new Extension();
        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.shouldMapAllInputs("elementId")).isFalse();
        assertThat(extension.shouldMapAllOutputs("elementId")).isTrue();

    }

    @Test
    void should_hasMappingReturnTrue_when_thereIsMapping() {
        Extension extension = new Extension();

        HashMap<String, ProcessVariablesMapping> mapping = new HashMap<>();
        mapping.put("elementId", processVariablesMapping);
        extension.setMappings(mapping);

        assertThat(extension.hasMapping("elementId")).isTrue();
    }

    @Test
    void should_hasMappingReturnFalse_when_thereIsNoMapping() {
        Extension extension = new Extension();
        assertThat(extension.hasMapping("elementId")).isFalse();
    }

    @Test
    void findAssigneeTemplateForTask_should_returnTemplate_when_templateIsDefined() throws Exception {
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
    void findAssigneeTemplateForTask_should_returnEmpty_when_noTemplatesForTask() throws Exception {
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
    void findAssigneeTemplateForTask_should_returnEmpty_when_onlyCandidateTemplate() throws Exception {
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
    void findCandidateTemplateForTask_should_returnTemplate_when_templateIsDefined() throws Exception {
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
    void findCandidateTemplateForTask_should_returnEmpty_when_onlyAssigneeCandidate() throws Exception {
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
    void findCandidateTemplateForTask_should_returnEmpty_when_noTemplatesForTask() throws Exception {
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

    @Test
    void should_readAssignmentDefinitionExtension() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread()
                                             .getContextClassLoader()
                                             .getResourceAsStream("processes/assignment-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");

            //when
            Map<String, AssignmentDefinition> assignments = extensions.getAssignments();

            //then
            assertThat(assignments).hasSize(3)
                .containsValues(new AssignmentDefinition("1", ASSIGNEE, STATIC, MANUAL),
                                new AssignmentDefinition("2", CANDIDATES, IDENTITY, MANUAL),
                                new AssignmentDefinition("3", CANDIDATES, EXPRESSION, SEQUENTIAL));
        }
    }

    @Test
    void should_enableEmailNotification_when_notSpecified() throws Exception {
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");
            TemplatesDefinition templates = extensions.getTemplates();

            boolean isEnabled = templates.isEmailNotificationEnabledForTask("myTaskId2");
            assertThat(isEnabled).isTrue();
        }
    }

    @Test
    void should_disableEmailNotification_when_setToFalse() throws Exception {
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");
            TemplatesDefinition templates = extensions.getTemplates();

            boolean isEnabled = templates.isEmailNotificationEnabledForTask("myTaskId1");
            assertThat(isEnabled).isFalse();
        }
    }

    @Test
    void should_enableEmailNotification_when_explicitlySetToTrue() throws Exception {
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");
            TemplatesDefinition templates = extensions.getTemplates();

            boolean isEnabled = templates.isEmailNotificationEnabledForTask("myTaskId3");
            assertThat(isEnabled).isTrue();
        }
    }

    @Test
    void should_returnTrue_when_taskHasNoTemplateDefinition() throws Exception {
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            ProcessExtensionModel processExtensionModel = MAPPER.readValue(inputStream, ProcessExtensionModel.class);
            Extension extensions = processExtensionModel
                .getExtensions("processDefinitionId");
            TemplatesDefinition templates = extensions.getTemplates();

            boolean isEnabled = templates.isEmailNotificationEnabledForTask("myTask");
            assertThat(isEnabled).isTrue();
        }
    }
}
