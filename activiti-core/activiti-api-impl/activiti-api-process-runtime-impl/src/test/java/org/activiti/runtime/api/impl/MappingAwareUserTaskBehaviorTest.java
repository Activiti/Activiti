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

package org.activiti.runtime.api.impl;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.activiti.runtime.api.impl.MappingExecutionContext.buildMappingExecutionContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class MappingAwareUserTaskBehaviorTest {

    @InjectMocks
    private MappingAwareUserTaskBehavior behavior;

    @Mock
    private VariablesMappingProvider mappingProvider;

    @BeforeEach
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void calculateInputVariablesShouldReturnValueFromMappingProvider() {
        //given
        DelegateExecution execution = buildExecution();
        Map<String, Object> providerVariables = singletonMap("var", "value");
        given(mappingProvider.calculateInputVariables(execution)).willReturn(providerVariables);

        //when
        Map<String, Object> inputVariables = behavior.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isEqualTo(providerVariables);
    }

    @Test
    public void calculateOutBoundVariablesShouldReturnValueFromMappingProvider() {
        //given
        DelegateExecution execution = buildExecution();
        Map<String, Object> availableVariables = emptyMap();
        Map<String, Object> providerVariables = singletonMap("var", "value");
        given(mappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution),
                                                       availableVariables))
                .willReturn(providerVariables);
        //when
        Map<String, Object> outBoundVariables = behavior.calculateOutBoundVariables(execution,
                                                                                    availableVariables);
        //then
        assertThat(outBoundVariables).isEqualTo(providerVariables);
    }

    private DelegateExecution buildExecution() {
        return mock(DelegateExecution.class);
    }
}
