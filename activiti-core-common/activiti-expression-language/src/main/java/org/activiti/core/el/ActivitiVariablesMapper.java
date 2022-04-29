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
package org.activiti.core.el;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

public class ActivitiVariablesMapper extends VariableMapper {

    Map<String, ValueExpression> map = Collections.emptyMap();

    public ActivitiVariablesMapper() {
    }

    public ActivitiVariablesMapper(Map<String, ValueExpression> map) {
        this.map = map;
    }

    @Override
    public ValueExpression resolveVariable(String variable) {
        return map.get(variable);
    }

    @Override
    public ValueExpression setVariable(String variable, ValueExpression expression) {
        if (map.isEmpty()) {
            map = new HashMap<>();
        }
        return map.put(variable, expression);
    }
}
