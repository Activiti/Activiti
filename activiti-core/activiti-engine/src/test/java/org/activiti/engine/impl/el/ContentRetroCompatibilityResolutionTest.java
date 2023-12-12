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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.junit.Test;

public class ContentRetroCompatibilityResolutionTest {

    private final ExpressionManager expressionManager = new ExpressionManager();

    private final Map<String, Object> availableVariables = Map.of("content", Map.of("id", 1, "0", Map.of("id", 1)));

    private Expression expression;

    private String expressionString;

    @Test
    public void should_returnObject_when_expressionReferencesContentRetroCompatibilityObjectAndPropertyIsUsed() {
        //given
        expressionString = "${content.id}";
        expression = expressionManager.createExpression(expressionString);

        //when
        Object value = expression.getValue(expressionManager, new DefaultDelegateInterceptor(), availableVariables);

        //then
        assertThat(value).isEqualTo(1);
    }

    @Test
    public void should_returnObject_when_expressionReferencesContentRetroCompatibilityObjectAndIndexIsUsed() {
        //given
        expressionString = "${content[0].id}";
        expression = expressionManager.createExpression(expressionString);

        //when
        Object value = expression.getValue(expressionManager, new DefaultDelegateInterceptor(), availableVariables);

        //then
        assertThat(value).isEqualTo(1);
    }

}
