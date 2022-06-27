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

package org.activiti.engine.impl.bpmn.behavior;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class VariablesPropagatorTest {

    @Spy
    @InjectMocks
    private VariablesPropagator variablesPropagator;

    @Mock
    private VariablesCalculator variablesCalculator;

    @Mock
    private ExecutionEntityManager executionEntityManager;

    private AutoCloseable autoCloseable;

    @Before
    public void setUp() {
        autoCloseable = openMocks(this);
        doReturn(executionEntityManager).when(variablesPropagator).getExecutionEntityManager();
    }

    @After
    public void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void should_setProcessInstanceVariablesAfterResolvingMapping_when_parentIsNotMultiInstanceRoot() {
        //given
        final String processInstanceId = UUID.randomUUID().toString();
        final DelegateExecution execution = buildExecution(processInstanceId, false);
        final ExecutionEntity processInstanceEntity = mock(ExecutionEntity.class);
        given(executionEntityManager.findById(processInstanceId)).willReturn(processInstanceEntity);

        final Map<String, Object> availableVariables = Collections.singletonMap("beforeMapping", "value");
        final Map<String, Object> outboundVariables = Collections.singletonMap("mapped", "value");
        given(variablesCalculator.calculateOutPutVariables(MappingExecutionContext.buildMappingExecutionContext(execution), availableVariables))
            .willReturn(outboundVariables);


        //when
        variablesPropagator.propagate(execution, availableVariables);

        //then
        verify(processInstanceEntity).setVariables(outboundVariables);
    }

    private DelegateExecution buildExecution(String processInstanceId, boolean multiInstanceRoot) {
        final DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn(processInstanceId);
        when(execution.getCurrentActivityId()).thenReturn("myTask");
        when(execution.getProcessDefinitionId()).thenReturn(UUID.randomUUID().toString());

        final DelegateExecution parentExecution = mock(DelegateExecution.class);
        when(execution.getParent()).thenReturn(parentExecution);
        when(parentExecution.isMultiInstanceRoot()).thenReturn(multiInstanceRoot);
        return execution;
    }

    @Test
    public void should_propagateAvailableVariablesToLocalScope_when_parentIsMultiInstanceRoot() {
        //given
        final String processInstanceId = UUID.randomUUID().toString();
        final DelegateExecution execution = buildExecution(processInstanceId, true);
        final Map<String, Object> availableVariables = Collections.singletonMap("beforeMapping", "value");


        //when
        variablesPropagator.propagate(execution, availableVariables);

        //then
        verify(execution).setVariablesLocal(availableVariables);
    }

}
