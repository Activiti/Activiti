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
package org.activiti.spring.process.variable.types;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.activiti.engine.ActivitiException;

/**
 * Base variable type for types as defined in extension json files. Used to validate variables against definition.
 */
public abstract class VariableType {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("^\\$\\{(.|\\n)*[\\}]$");
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    abstract public void validate(Object var, List<ActivitiException> errors);

    public Object parseFromValue(Object value) throws ActivitiException {
        return value;
    }

    protected boolean isExpression(Object var) {
        return Objects.nonNull(var) && var.getClass().isAssignableFrom(String.class) && EXPRESSION_PATTERN.matcher((CharSequence) var).matches();
    }
}
