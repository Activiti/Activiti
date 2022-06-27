/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessVariablesInitiatorIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessVariablesInitiator processVariablesInitiator;

    @MockBean
    private ProcessExtensionService processExtensionService;

    @MockBean
    private UserGroupManager userGroupManager;

    @MockBean
    private RepositoryService repositoryService;

    @MockBean
    private RuntimeService runtimeService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private ManagementService managementService;

    @SpringBootApplication
    static class Application {

    }

    @Test
    public void calculateVariablesFromExtensionFileShouldReturnVariablesWithDefaultValues() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/default-vars-extensions.json")) {
            ProcessExtensionModel extension = objectMapper.readValue(inputStream,
                                                                     ProcessExtensionModel.class);

            ProcessDefinition processDefinition = mock(ProcessDefinition.class);
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension.getExtensions("Process_DefaultVarsProcess"));
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);
            given(processDefinition.getKey()).willReturn("Process_DefaultVarsProcess");

            //when
            Map<String, Object> variables = processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                          null);

            //then
            assertThat(variables)
                    .containsEntry("name",
                                   "Nobody")
                    .containsEntry("positionInTheQueue",
                                   10)
                    .doesNotContainKeys("age"); // age has no default value, so it won't be created
        }
    }

    @Test
    public void calculateVariablesFromExtensionFileShouldGivePriorityToProvidedValuesOverDefaultValues() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/default-vars-extensions.json")) {
            ProcessExtensionModel extension = objectMapper.readValue(inputStream,
                                                                     ProcessExtensionModel.class);

            ProcessDefinition processDefinition = mock(ProcessDefinition.class);
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension.getExtensions("Process_DefaultVarsProcess"));
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);
            given(processDefinition.getKey()).willReturn("Process_DefaultVarsProcess");

            //when
            Map<String, Object> variables = processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                          singletonMap("name",
                                                                                                                                   "Peter"));

            //then
            assertThat(variables)
                    .containsEntry("name", // value for variable "name" has been provided,
                                   "Peter") // so default value should be ignored.
                    .containsEntry("positionInTheQueue",
                                   10);
        }
    }

    @Test
    public void calculateVariablesFromExtensionFileShouldThrowExceptionWhenMandatoryVariableIsMissing() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/initial-vars-extensions.json")) {
            ProcessExtensionModel extension = objectMapper.readValue(inputStream,
                                                                     ProcessExtensionModel.class);

            ProcessDefinition processDefinition = mock(ProcessDefinition.class);
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension.getExtensions("Process_initialVarsProcess"));
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);
            given(processDefinition.getKey()).willReturn("Process_initialVarsProcess");

            //when
            Throwable thrownException = catchThrowable(() -> processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                                     emptyMap())
            );

            //then
            assertThat(thrownException)
                    .isInstanceOf(ActivitiException.class)
                    .hasMessageContaining("Can't start process")
                    .hasMessageContaining("without required variables - age");
        }
    }

    @Test
    public void calculateVariablesFromExtensionFileShouldThrowExceptionWhenProvidedValueHasNotTheSameTypeAsInTheDefinition() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/initial-vars-extensions.json")) {
            ProcessExtensionModel extension = objectMapper.readValue(inputStream,
                                                                     ProcessExtensionModel.class);

            ProcessDefinition processDefinition = mock(ProcessDefinition.class);
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension.getExtensions("Process_initialVarsProcess"));
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);
            given(processDefinition.getKey()).willReturn("Process_initialVarsProcess");

            //when
            Throwable thrownException = catchThrowable(() -> processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                                     singletonMap("age", "invalidNumber"))
            );

            //then
            assertThat(thrownException)
                    .isInstanceOf(ActivitiException.class)
                    .hasMessageContaining("Can't start process")
                    .hasMessageContaining("as variables fail type validation - age");
        }
    }
}
