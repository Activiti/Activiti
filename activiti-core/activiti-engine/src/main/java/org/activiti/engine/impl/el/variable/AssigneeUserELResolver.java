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

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

public class AssigneeUserELResolver implements VariableScopeItemELResolver {

    private static final String ASSIGNEE_USER_KEY = "assignee";

    @Override
    public boolean canResolve(String property, VariableScope variableScope) {
        return ASSIGNEE_USER_KEY.equals(property) && variableScope instanceof TaskEntity;
    }

    @Override
    public Object resolve(String property, VariableScope variableScope) {

        TaskEntity taskEntity = ((TaskEntity) variableScope);
        return taskEntity != null ? taskEntity.getAssignee() : null;
    }
}
