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
package org.activiti.engine.impl.delegate;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.DelegateExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;

public class ThrowMessageDelegateExpression implements ThrowMessageDelegate {

    private final Expression delegateExpression;
    private final List<FieldDeclaration> fieldDeclarations;

    public ThrowMessageDelegateExpression(Expression delegateExpression,
                                          List<FieldDeclaration> fieldDeclarations) {
        this.delegateExpression = delegateExpression;
        this.fieldDeclarations = fieldDeclarations;
    }

    @Override
    public boolean send(DelegateExecution execution, ThrowMessage message) {

        Object delegate = DelegateExpressionUtil.resolveDelegateExpression(delegateExpression,
                                                                           execution,
                                                                           fieldDeclarations);
        if(ThrowMessageDelegate.class.isInstance(delegate)) {
            return ThrowMessageDelegate.class.cast(delegate)
                                             .send(execution, message);
        }

        return false;
    }
}
