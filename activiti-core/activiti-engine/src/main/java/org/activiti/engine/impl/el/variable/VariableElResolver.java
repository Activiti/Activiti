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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.variable.JsonType;
import org.activiti.engine.impl.variable.LongJsonType;

public class VariableElResolver implements VariableScopeItemELResolver {

    private final ObjectMapper objectMapper;

    public VariableElResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canResolve(String property, VariableScope variableScope) {
        return variableScope.hasVariable(property);
    }

    @Override
    public Object resolve(String property, VariableScope variableScope) {
        VariableInstance variableInstance = variableScope.getVariableInstance(property);
        Object value = variableInstance.getValue();
        if (hasJsonType(variableInstance) && (value instanceof JsonNode) &&
            ((JsonNode) value).isArray()) {
            return objectMapper.convertValue(value, List.class);
        } else {
            return value;
        }
    }

    private boolean hasJsonType(VariableInstance variableInstance) {
        return JsonType.JSON.equals(variableInstance.getTypeName()) ||
            LongJsonType.LONG_JSON.equals(variableInstance.getTypeName());
    }

}
