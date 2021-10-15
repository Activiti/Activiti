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
package org.activiti.runtime.api.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext.buildMappingExecutionContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.spring.process.ProcessVariablesInitiator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class MappingAwareCallActivityBehaviorTest {

    @InjectMocks
    private MappingAwareCallActivityBehavior behavior;

    @Mock
    private ExtensionsVariablesMappingProvider mappingProvider;

    @Mock
    private ProcessVariablesInitiator processVariablesInitiator;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void calculateInboundVariablesShouldTakeIntoAccountMappingProviderAndProcessVariablesInitiator() {
        //given
        DelegateExecution execution = buildExecution();
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);
        Map<String, Object> providerVariables = singletonMap("var1", "v1");
        given(mappingProvider.calculateInputVariables(execution))
            .willReturn(providerVariables);

        HashMap<String, Object> initiatorVariables = new HashMap<>(
            providerVariables
        );
        initiatorVariables.put("var2", "default");
        given(
            processVariablesInitiator.calculateVariablesFromExtensionFile(
                processDefinition,
                providerVariables
            )
        )
            .willReturn(initiatorVariables);

        //when
        Map<String, Object> inboundVariables = behavior.calculateInboundVariables(
            execution,
            processDefinition
        );
        //then
        assertThat(inboundVariables).isEqualTo(initiatorVariables);
    }

    private DelegateExecution buildExecution() {
        return mock(DelegateExecution.class);
    }

    @Test
    public void calculateOutBoundVariablesShouldReturnValueFromMappingProvider() {
        //given
        DelegateExecution execution = buildExecution();
        Map<String, Object> availableVariables = emptyMap();
        Map<String, Object> providerVariables = singletonMap("var", "value");
        given(
            mappingProvider.calculateOutPutVariables(
                buildMappingExecutionContext(execution),
                availableVariables
            )
        )
            .willReturn(providerVariables);

        //when
        Map<String, Object> outBoundVariables = behavior.calculateOutBoundVariables(
            execution,
            availableVariables
        );
        //then
        assertThat(outBoundVariables).isEqualTo(providerVariables);
    }
}
