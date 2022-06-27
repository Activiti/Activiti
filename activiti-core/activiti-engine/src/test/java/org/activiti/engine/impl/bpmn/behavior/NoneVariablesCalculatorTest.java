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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;

public class NoneVariablesCalculatorTest {

    private NoneVariablesCalculator variablesCalculator = new NoneVariablesCalculator();

    @Test
    public void calculateOutPutVariables_should_returnEmptyMap() {
        //given
        MappingExecutionContext mappingExecutionContext = MappingExecutionContext
            .buildMappingExecutionContext("procDefId", "activityId");
        Map<String, Object> availableVariables = Collections.singletonMap("any", "any");

        //when
        Map<String, Object> calculatedVariables = variablesCalculator.calculateOutPutVariables(
            mappingExecutionContext,
            availableVariables);

        //then
        assertThat(calculatedVariables).isEmpty();
    }

    @Test
    public void calculateInputVariables_should_return_emptyMap() {
        //given
        DelegateExecution mock = mock(DelegateExecution.class);

        //when
        Map<String, Object> calculatedVariables = variablesCalculator
            .calculateInputVariables(mock);

        //then
        assertThat(calculatedVariables).isEmpty();
    }

}
