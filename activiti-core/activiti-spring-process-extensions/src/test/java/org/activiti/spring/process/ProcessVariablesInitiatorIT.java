/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.spring.process;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessVariablesInitiatorIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProcessVariablesInitiator processVariablesInitiator;

    @MockBean
    private ProcessExtensionService processExtensionService;

    @MockBean
    private RepositoryService repositoryService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void calculateVariablesFromExtensionFileShouldReturnVariablesWithDefaultValues() throws Exception {
        //given
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("processes/default-vars-extensions.json")) {
            ProcessExtensionModel extension = objectMapper.readValue(inputStream,
                                                                     ProcessExtensionModel.class);

            ProcessDefinition processDefinition = mock(ProcessDefinition.class);
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension);
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);

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
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension);
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);

            //when
            Map<String, Object> variables = processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                          Collections.singletonMap("name",
                                                                                                                                   "Peter"));

            //then
            assertThat(variables)
                    .containsEntry("name", // value for variable "name" has been provided,
                                   "Peter") // so default value should should be ignored.
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
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension);
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);

            //when
            Throwable thrownException = catchThrowable(() -> processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                                     Collections.emptyMap())
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
            given(processExtensionService.getExtensionsFor(processDefinition)).willReturn(extension);
            given(processExtensionService.hasExtensionsFor(processDefinition)).willReturn(true);

            //when
            Throwable thrownException = catchThrowable(() -> processVariablesInitiator.calculateVariablesFromExtensionFile(processDefinition,
                                                                                                                     Collections.singletonMap("age", "invalidNumber"))
            );

            //then
            assertThat(thrownException)
                    .isInstanceOf(ActivitiException.class)
                    .hasMessageContaining("Can't start process")
                    .hasMessageContaining("as variables fail type validation - age");
        }
    }
}
