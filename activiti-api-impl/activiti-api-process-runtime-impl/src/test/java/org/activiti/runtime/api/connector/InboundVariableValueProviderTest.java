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

package org.activiti.runtime.api.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class InboundVariableValueProviderTest {

    @InjectMocks
    private InboundVariableValueProvider inboundVariableValueProvider;

    @Mock
    private ProcessExtensionService processExtensionService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void calculateStaticValuesShouldResolveMappingsOfTypeStaticValue() {
        //given
        String serviceTaskUUID = "serviceTaskId";
        DelegateExecution execution = buildExecution(serviceTaskUUID,
                                                     "procDefId");

        Map<String, Mapping> inputs = new HashMap<>();
        inputs.put("staticValue1",
                   buildStaticMapping("firstStatic"));
        inputs.put("staticValue2",
                   buildStaticMapping("secondStatic"));
        inputs.put("var",
                   buildVariableMapping("myVar"));
        inputs.put("value",
                   buildValueMapping("inValue"));

        given(processExtensionService.getExtensionsForId(execution.getProcessDefinitionId()))
                .willReturn(buildExtension(serviceTaskUUID, inputs));


        //when
        Map<String, Object> staticValues = inboundVariableValueProvider.calculateStaticValues(execution);

        //then
        assertThat(staticValues)
                .containsEntry("staticValue1",
                               "firstStatic")
                .containsEntry("staticValue2",
                               "secondStatic")
                .hasSize(2);
    }

    private DelegateExecution buildExecution(String serviceTaskUUID,
                                             String procDefId) {
        DelegateExecution execution = mock(DelegateExecution.class);
        given(execution.getProcessDefinitionId()).willReturn(procDefId);
        given(execution.getCurrentActivityId()).willReturn(serviceTaskUUID);
        return execution;
    }

    private ProcessExtensionModel buildExtension(String serviceTaskUUID,
                                                 Map<String, Mapping> inputs) {
        ProcessExtensionModel processExtensionModel = new ProcessExtensionModel();
        Extension extension = new Extension();
        ProcessVariablesMapping processVariablesMapping = new ProcessVariablesMapping();
        processVariablesMapping.setInputs(inputs);
        extension.setMappings(Collections.singletonMap(serviceTaskUUID,
                                                       processVariablesMapping));
        processExtensionModel.setExtensions(extension);
        return processExtensionModel;
    }

    private Mapping buildStaticMapping(String value) {
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.STATIC_VALUE);
        mapping.setValue(value);
        return mapping;
    }

    private Mapping buildVariableMapping(String value) {
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(value);
        return mapping;
    }

    private Mapping buildValueMapping(String value) {
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VALUE);
        mapping.setValue(value);
        return mapping;
    }
}