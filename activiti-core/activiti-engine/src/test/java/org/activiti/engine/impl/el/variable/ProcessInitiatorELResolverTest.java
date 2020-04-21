/*
 * Copyright 2020 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.engine.impl.el.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.junit.Test;

public class ProcessInitiatorELResolverTest {

    private static final String INITIATOR = "initiator";

    private ProcessInitiatorELResolver resolver = new ProcessInitiatorELResolver();

    @Test
    public void canResolve_should_returnTrueWhenItsExecutionEntityAndPropertyIsInitiator() {
        //when
        boolean canResolve = resolver.canResolve(INITIATOR, new ExecutionEntityImpl());
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsExecutionEntityAndPropertyIsNotInitiator() {
        //when
        boolean canResolve = resolver.canResolve("anyOtherProperty", new ExecutionEntityImpl());
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsNotExecutionEntityAndPropertyIsInitiator() {
        //when
        boolean canResolve = resolver.canResolve(INITIATOR, mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnProcessInitiator() {
        //given
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        given(processInstance.getStartUserId()).willReturn("peter");
        ExecutionEntity variableScope = buildVariableScope(processInstance);

        //when
        Object result = resolver.resolve(INITIATOR, variableScope);

        //then
        assertThat(result).isEqualTo("peter");
    }

    @Test
    public void resolve_should_returnNullWhenVariableScopeDontHaveProcessInstance() {
        //given
        ExecutionEntity variableScope = buildVariableScope(null);

        //when
        Object result = resolver.resolve(INITIATOR, variableScope);

        //then
        assertThat(result).isNull();
    }

    private ExecutionEntity buildVariableScope(ExecutionEntity processInstance) {
        ExecutionEntity variableScope = mock(ExecutionEntity.class);
        given(variableScope.getProcessInstance()).willReturn(processInstance);
        return variableScope;
    }
}
