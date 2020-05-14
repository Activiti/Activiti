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


package org.activiti.engine.impl.el;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;

/**
 * Expression that always returns the same value when <code>getValue</code> is called. Setting of the value is not supported.
 *

 */
public class FixedValue implements Expression {

    private static final long serialVersionUID = 1L;
    private Object value;

    public FixedValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue(VariableScope variableScope) {
        return value;
    }

    @Override
    public void setValue(Object value, VariableScope variableScope) {
        throw new ActivitiException("Cannot change fixed value");
    }

    @Override
    public String getExpressionText() {
        return value.toString();
    }

    @Override
    public Object getValue(ExpressionManager expressionManager,
        DelegateInterceptor delegateInterceptor, Map<String, Object> availableVariables) {
        return value;
    }

}
