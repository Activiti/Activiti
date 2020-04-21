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
import static org.mockito.Mockito.mock;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.Test;

public class TaskElResolverTest {

    private static final String TASK_KEY = "task";

    private TaskElResolver resolver = new TaskElResolver();

    @Test
    public void canResolve_should_returnTrueWhenItsTaskEntityAndPropertyIsTask() {
        //when
        boolean canResolve = resolver.canResolve(TASK_KEY, new TaskEntityImpl());
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsTaskEntityAndPropertyIsNotTask() {
        //when
        boolean canResolve = resolver.canResolve("differentFromTask", new TaskEntityImpl());
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void canResolve_should_returnFalseWhenItsNotTaskEntityEntityAndPropertyIsTask() {
        //when
        boolean canResolve = resolver.canResolve(TASK_KEY, mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnVariableScope() {
        //given
        TaskEntity variableScope = new TaskEntityImpl();

        //when
        Object result = resolver.resolve(TASK_KEY, variableScope);

        //then
        assertThat(result).isEqualTo(variableScope);
    }

}
