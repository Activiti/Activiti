/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.model.impl;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.engine.RepositoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import static org.activiti.runtime.api.model.impl.MockProcessDefinitionBuilder.processDefinitionBuilderBuilder;

public class APIProcessDefinitionConverterTest {

    @InjectMocks
    private APIProcessDefinitionConverter processDefinitionConverter;

    @Mock
    private RepositoryService repositoryService;

    @Before
    public void setUp() {
        initMocks(this);

        BpmnModel model = new BpmnModel();
        Process process = new Process();
        process.setId("processKey");
        StartEvent startEvent = new StartEvent();
        startEvent.setFormKey("AFormKey");
        process.setInitialFlowElement(startEvent);
        model.addProcess(process);

        when(repositoryService.getBpmnModel("anId")).thenReturn(model);
    }

    @Test
    public void convertFromProcessDefinitionShouldSetAllFieldsInTheConvertedProcessDefinition() {
        ProcessDefinition convertedProcessDefinition = processDefinitionConverter.from(
                processDefinitionBuilderBuilder()
                        .withId("anId")
                        .withKey("processKey")
                        .withName("Process Name")
                        .withDescription("process description")
                        .withVersion(3)
                        .build()
        );

        //THEN
        assertThat(convertedProcessDefinition)
                .isNotNull()
                .extracting(ProcessDefinition::getId,
                            ProcessDefinition::getKey,
                            ProcessDefinition::getName,
                            ProcessDefinition::getDescription,
                            ProcessDefinition::getVersion,
                            ProcessDefinition::getFormKey)
                .containsExactly(
                             "anId",
                             "processKey",
                             "Process Name",
                             "process description",
                             3,
                             "AFormKey");
    }
}
