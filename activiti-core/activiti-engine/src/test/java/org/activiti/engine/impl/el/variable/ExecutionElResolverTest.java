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
package org.activiti.engine.impl.el.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.Test;

public class ExecutionElResolverTest {

    private static final String EXECUTION_KEY = "execution";

    private ExecutionElResolver resolver = new ExecutionElResolver();

    @Test
    public void canResolve_should_returnTrueWhenItsExecutionEntityAndPropertyIsExecution() {
        //when
        boolean canResolve = resolver.canResolve(EXECUTION_KEY, new ExecutionEntityImpl());
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnTrueWhenItsTaskEntityAndPropertyIsExecution() {
        //when
        boolean canResolve = resolver.canResolve(EXECUTION_KEY, new TaskEntityImpl());
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsExecutionEntityAndPropertyIsNotExecution() {
        //when
        boolean canResolve = resolver.canResolve("anyOtherProperty", new ExecutionEntityImpl());
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsNotExecutionOrTaskEntityAndPropertyIsExecution() {
        //when
        boolean canResolve = resolver.canResolve(EXECUTION_KEY, mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnVariableScopeWhenItsExecutionEntity() {
        //given
        ExecutionEntity variableScope = new ExecutionEntityImpl();

        //when
        Object result = resolver.resolve(EXECUTION_KEY, variableScope);

        //then
        assertThat(result).isEqualTo(variableScope);
    }

    @Test
    public void resolve_should_returnVariableScopeExecutionWhenItsTaskEntity() {
        //given
        ExecutionEntityImpl execution = new ExecutionEntityImpl();
        TaskEntityImpl variableScope = new TaskEntityImpl();
        variableScope.setExecution(execution);

        //when
        Object result = resolver.resolve(EXECUTION_KEY, variableScope);

        //then
        assertThat(result).isEqualTo(execution);
    }
}
