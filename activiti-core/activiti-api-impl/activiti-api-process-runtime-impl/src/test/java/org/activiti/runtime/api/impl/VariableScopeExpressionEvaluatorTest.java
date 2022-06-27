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
package org.activiti.runtime.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VariableScopeExpressionEvaluatorTest {

    @Mock
    private  ExpressionManager expressionManager;

    @Mock
    private DelegateInterceptor delegateInterceptor;

    @Test
    public void evaluate_should_returnResultOfGetValueWithVariableScope() {
        //given
        VariableScope variableScope = mock(VariableScope.class);
        VariableScopeExpressionEvaluator evaluator = new VariableScopeExpressionEvaluator(
            variableScope);

        Expression expression = mock(Expression.class);
        given(expression.getValue(variableScope)).willReturn("London");

        //when
        Object value = evaluator.evaluate(expression, expressionManager, delegateInterceptor);

        //then
        assertThat(value).isEqualTo("London");
    }
}
