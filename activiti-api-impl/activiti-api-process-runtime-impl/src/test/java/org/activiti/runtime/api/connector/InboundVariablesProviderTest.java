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
import java.util.Map;

import org.activiti.core.common.model.connector.ActionDefinition;
import org.activiti.core.common.model.connector.VariableDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class InboundVariablesProviderTest {

    @InjectMocks
    private InboundVariablesProvider inboundVariablesProvider;

    @Mock
    private InboundVariableValueProvider mappedValueProvider;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void calculateVariablesShouldReturnExecutionVariablesMergedWithStaticValuesWhenActionDefinitionIsNull() {
        //given
        Map<String, Object> executionVariables = Collections.singletonMap("key",
                                                         "value");
        DelegateExecution execution = mock(DelegateExecution.class);
        given(execution.getVariables()).willReturn(executionVariables);

        Map<String, Object> staticValues = Collections.singletonMap("myStatic",
                                                         "st");
        given(mappedValueProvider.calculateStaticValues(execution))
                .willReturn(staticValues);

        //when
        Map<String, Object> inboundVariables = inboundVariablesProvider.calculateVariables(execution,
                                                                                          null);

        //then
        assertThat(inboundVariables)
                .containsAllEntriesOf(executionVariables)
                .containsAllEntriesOf(staticValues)
                .hasSize(2);
    }

    @Test
    public void calculateVariablesShouldMapConnectorsInputsAndMergeStaticValuesWhenActionDefinitionIsAvailable() {
        //given
        VariableDefinition variableDefinition = new VariableDefinition();
        variableDefinition.setName("inputVar");

        ActionDefinition actionDefinition = new ActionDefinition();
        actionDefinition.setInputs(Collections.singletonList(variableDefinition));

        DelegateExecution execution = mock(DelegateExecution.class);
        given(mappedValueProvider.calculateMappedValue(variableDefinition,
                                                       execution))
                .willReturn("inValue");

        Map<String, Object> staticValues = Collections.singletonMap("myStatic",
                                                                    "st");
        given(mappedValueProvider.calculateStaticValues(execution))
                .willReturn(staticValues);

        //when
        Map<String, Object> inboundVariables = inboundVariablesProvider.calculateVariables(execution,
                                                                                          actionDefinition);

        //then
        assertThat(inboundVariables)
                .hasSize(2)
                .containsEntry("inputVar", "inValue")
                .containsAllEntriesOf(staticValues);
    }
}