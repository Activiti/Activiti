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

import java.lang.reflect.Method;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 *
 */
public class ActivitiElContext extends ELContext {

    protected ELResolver elResolver;
    private ActivitiFunctionMapper functions;
    private ActivitiVariablesMapper variables;

    public ActivitiElContext() {
        this(null);
    }

    public ActivitiElContext(ELResolver elResolver) {
        this.elResolver = elResolver;
    }

    public ELResolver getELResolver() {
        return elResolver;
    }

    public FunctionMapper getFunctionMapper() {
        if (functions == null) {
            functions = new ActivitiFunctionMapper();
        }
        return functions;
    }

    public VariableMapper getVariableMapper() {
        if (variables == null) {
            variables = new ActivitiVariablesMapper();
        }
        return variables;
    }

    public void setFunction(String prefix, String localName, Method method) {
        if (functions == null) {
            functions = new ActivitiFunctionMapper();
        }
        functions.setFunction(prefix, localName, method);
    }

    public ValueExpression setVariable(String name, ValueExpression expression) {
        if (variables == null) {
            variables = new ActivitiVariablesMapper();
        }
        return variables.setVariable(name, expression);
    }

}
