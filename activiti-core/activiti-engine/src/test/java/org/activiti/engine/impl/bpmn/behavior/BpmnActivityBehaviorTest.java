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

package org.activiti.engine.impl.bpmn.behavior;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.Map;
import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

public class BpmnActivityBehaviorTest {

    @Spy
    @InjectMocks
    private BpmnActivityBehavior bpmnActivityBehavior;

    @Mock
    private VariablesCalculator variablesCalculator;

    @Mock
    private ActivitiEngineAgenda agenda;

    @Before
    public void setUp() {
        initMocks(this);
        doReturn(agenda).when(bpmnActivityBehavior).getAgenda();
    }

    @Test
    public void performDefaultOutgoingBehavior_should_propagateVariablesToParentAndPlanContinuation() {
        ExecutionEntity parentExecution = mock(ExecutionEntity.class);
        Map<String, Object> variablesLocal = Collections.singletonMap(
            "myVar",
            "value"
        );
        ExecutionEntity execution = buildExecution(
            parentExecution,
            variablesLocal
        );

        Map<String, Object> calculatedVariables = Collections.singletonMap(
            "mappedVar",
            "mappedValue"
        );
        given(
            variablesCalculator.calculateOutPutVariables(
                MappingExecutionContext.buildMappingExecutionContext(execution),
                variablesLocal
            )
        )
            .willReturn(calculatedVariables);

        //when
        bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);

        //then
        verify(parentExecution).setVariables(calculatedVariables);
        verify(agenda).planTakeOutgoingSequenceFlowsOperation(execution, true);
    }

    private ExecutionEntity buildExecution(
        ExecutionEntity parentExecution,
        Map<String, Object> variablesLocal
    ) {
        ExecutionEntity execution = mock(ExecutionEntity.class);
        given(execution.getProcessDefinitionId()).willReturn("procDefId");
        given(execution.getActivityId()).willReturn("activityId");
        given(execution.getParent()).willReturn(parentExecution);
        given(execution.getVariablesLocal()).willReturn(variablesLocal);
        return execution;
    }
}
