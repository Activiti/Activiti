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

import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.TaskTemplateDefinition;
import org.activiti.spring.process.model.TemplateDefinition;
import org.activiti.spring.process.model.TemplatesDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.InputStream;

import static org.activiti.spring.process.model.TemplateDefinition.TemplateType.FILE;
import static org.activiti.spring.process.model.TemplateDefinition.TemplateType.VARIABLE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "spring.main.banner-mode=off"
)
public class ProcessExtensionResourceReaderIT {

    private static String FROM = "no-reply@activiti.org";

    @MockBean
    private RepositoryService repositoryService;

    @Autowired
    private ProcessExtensionResourceReader reader;

    @Test
    public void shouldReadExtensionFromJsonFile() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("processes/initial-vars-extensions.json")) {
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("initialVarsProcess");
            assertThat(
                processExtensionModel.getExtensions("Process_initialVarsProcess").getProperties())
                .containsKey("d440ff7b-0ac8-4a97-b163-51a6ec49faa1");
        }
    }

    @Test
    public void shouldReadTemplateExtensionFromJsonFile() throws Exception {
        try (InputStream inputStream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("processes/template-mapping-extensions.json")) {
            //when
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);

            //then
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("emailTemplateMapping");

            TemplatesDefinition templates = processExtensionModel
                .getExtensions("processDefinitionId")
                .getTemplates();
            TaskTemplateDefinition defaultTemplate = templates.getDefaultTemplate();
            assertThat(defaultTemplate).isNotNull();
            assertThat(defaultTemplate.getAssignee())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue,
                    TemplateDefinition::getFrom,
                    TemplateDefinition::getSubject)
                .containsExactly(
                    FILE,
                    "classpath:templates/email.html",
                    FROM,
                    "Default assignee subject"
                );
            assertThat(defaultTemplate.getCandidate())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue,
                    TemplateDefinition::getFrom,
                    TemplateDefinition::getSubject
                ).containsExactly(
                VARIABLE,
                "myCandidateTemplateVariable",
                FROM,
                "Default candidate subject"
            );

            assertThat(templates.getTasks())
                .containsOnlyKeys("myTaskId1", "myTaskId2", "myTaskId3");

            assertThat(templates.getTasks().get("myTaskId1").getAssignee())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue,
                    TemplateDefinition::getFrom,
                    TemplateDefinition::getSubject)
                .containsExactly(
                    FILE,
                    "https://github.com/leemunroe/responsive-html-email-template/blob/master/email.html",
                    null,
                    null
                );

            assertThat(templates.getTasks().get("myTaskId1").getCandidate())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue,
                    TemplateDefinition::getFrom,
                    TemplateDefinition::getSubject)
                .containsExactly(
                    FILE,
                    "https://github.com/leemunroe/responsive-html-email-template/blob/master/email-inlined.html",
                    null,
                    null
                );

            assertThat(templates.getTasks().get("myTaskId2").getAssignee())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue)
                .containsExactly(
                    VARIABLE,
                    "myAssigneeTemplateVariable"
                );
            assertThat(templates.getTasks().get("myTaskId2").getCandidate())
                .isNull();
            assertThat(templates.getTasks()
                                .get("myTaskId2")
                                .getAssignee().getFrom()).isEqualTo(FROM);
            assertThat(templates.getTasks()
                                .get("myTaskId2")
                                .getAssignee()
                                .getSubject()).isEqualTo("myTaskId2 assignee subject");

            assertThat(templates.getTasks().get("myTaskId3").getAssignee())
                .isNull();
            assertThat(templates.getTasks().get("myTaskId3").getCandidate())
                .isNotNull()
                .extracting(
                    TemplateDefinition::getType,
                    TemplateDefinition::getValue,
                    TemplateDefinition::getFrom,
                    TemplateDefinition::getSubject)
                .containsExactly(
                    VARIABLE,
                    "myCandidateTemplateVariable",
                    FROM,
                    "myTaskId3 candidate subject"
                );
        }
    }

    @Test
    public void shouldReadAnalyticsExtensionFromJsonFile() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                                             .getResourceAsStream("processes/initial-vars-extensions.json")) {
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("initialVarsProcess");
            assertThat(
                processExtensionModel.getExtensions("Process_initialVarsProcess").getProperties())
                    .extracting(stringVariableDefinitionMap -> stringVariableDefinitionMap.get("379dc1a1-481d-4617-a027-ef39fdadf6667"))
                    .extracting(VariableDefinition::getName, VariableDefinition::isAnalytics)
                    .containsOnly("trackedId", true);

            assertThat(
                processExtensionModel.getExtensions("Process_initialVarsProcess").getProperties())
                    .extracting(stringVariableDefinitionMap -> stringVariableDefinitionMap.get("379dc1a1-481d-4617-a027-ef39fdadf6668"))
                    .extracting(VariableDefinition::getName, VariableDefinition::isAnalytics)
                    .containsOnly("notTrackedId", false);
        }
    }

    @Test
    void should_read_propertyEphemeral_fromExtensions_when_present() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("processes/vars-extensions.json")) {
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("varsProcess");
            assertThat(
                processExtensionModel.getExtensions("Process_varsProcess").getProperties())
                .extracting(stringVariableDefinitionMap -> stringVariableDefinitionMap.get("ea640de8-4683-4298-84a5-1e2d59c7219a"))
                .extracting(VariableDefinition::getName, VariableDefinition::isEphemeral)
                .containsOnly("testVariable", true);

        }
    }

    @Test
    void shouldBeFalse_propertyEphemeral_when_notPresent() throws Exception {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("processes/vars-extensions.json")) {
            ProcessExtensionModel processExtensionModel = reader.read(inputStream);
            assertThat(processExtensionModel).isNotNull();
            assertThat(processExtensionModel.getId()).isEqualTo("varsProcess");
            assertThat(
                processExtensionModel.getExtensions("Process_varsProcess").getProperties())
                .extracting(stringVariableDefinitionMap -> stringVariableDefinitionMap.get("f04b8b72-26ca-411e-8b2b-729a059bb06a"))
                .extracting(VariableDefinition::getName, VariableDefinition::isEphemeral)
                .containsOnly("testVariable2", false);

        }
    }

}
