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
package org.activiti.engine.impl.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import java.util.Collections;
import java.util.Map;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.junit.Test;

public class ELResolverReflectionBlockerDecoratorTest {


    @Test
    public void should_resolveExpressionCorrectly_when_noReflectionOrNativeMethodsAreUsed() {
        //given
        Map<String, Object> availableVariables = Collections.singletonMap("name", "jon doe");
        String expressionString = "${name.toString()}";
        ExpressionManager expressionManager = new ExpressionManager();

        //when
        Expression expression = expressionManager.createExpression(expressionString);
        Object value = expression.getValue(expressionManager, new DefaultDelegateInterceptor(), availableVariables);

        //then
        assertThat(value).isEqualTo("jon doe");
    }

    @Test
    public void should_throwException_when_nativeMethodIsUsed() {

        //given
        Map<String, Object> availableVariables = Collections.singletonMap("name", "jon doe");
        String expressionString = "${name.getClass().getName()}";
        ExpressionManager expressionManager = new ExpressionManager();

        //when
        Expression expression = expressionManager.createExpression(expressionString);

        //then
        assertThatExceptionOfType(ActivitiException.class)
            .as("Using Native Method: getClass in an expression")
            .isThrownBy(() -> expression.getValue(expressionManager, new DefaultDelegateInterceptor(), availableVariables))
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_throwException_when_reflectionIsUsed() {

        //given
        Map<String, Object> availableVariables = Collections.singletonMap("class", String.class);
        String expressionString = "${class.forName(\"java.lang.Runtime\").getMethods()[6].invoke()}";
        ExpressionManager expressionManager = new ExpressionManager();

        //when
        Expression expression = expressionManager.createExpression(expressionString);

        //then
        assertThatExceptionOfType(ActivitiException.class)
            .as("Using Reflection in an expression")
            .isThrownBy(() -> expression.getValue(expressionManager, new DefaultDelegateInterceptor(), availableVariables))
            .withCauseInstanceOf(IllegalArgumentException.class);
    }

}
