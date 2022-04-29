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
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.junit.Test;

public class AssigneeUserELResolverTest {

    private static final String ASSIGNEE_USER_KEY = "assignee";

    private final AssigneeUserELResolver resolver = new AssigneeUserELResolver();

    @Test
    public void canResolve_should_returnTrueWhenPropertyIsAssigneeAndVariableScopeIsATask() {
        //when
        boolean canResolve = resolver.canResolve(ASSIGNEE_USER_KEY, mock(TaskEntity.class));
        //then
        assertThat(canResolve).isTrue();
    }

    @Test
    public void canResolve_should_returnFalseWhenPropertyIsAssigneeAndVariableScopeIsNotATask() {
        //when
        boolean canResolve = resolver.canResolve(ASSIGNEE_USER_KEY, mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void canResolve_should_returnFalseWhenPropertyIsNotAssigneeAndVariableScopeIsATAsk() {
        //when
        boolean canResolve = resolver.canResolve("anyOtherProperty", mock(TaskEntity.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void canResolve_should_returnFalseWhenPropertyIsNotAssigneeAndVariableScopeIsNotATAsk() {
        //when
        boolean canResolve = resolver.canResolve("anyOtherProperty", mock(VariableScope.class));
        //then
        assertThat(canResolve).isFalse();
    }

    @Test
    public void resolve_should_returnAssigneeUser() {
        //given
        TaskEntityImpl variableScope = new TaskEntityImpl();
        variableScope.setAssignee("user");

        //when
        Object result = resolver.resolve(ASSIGNEE_USER_KEY, variableScope);

        //then
        assertThat(result).isEqualTo("user");
    }
}
