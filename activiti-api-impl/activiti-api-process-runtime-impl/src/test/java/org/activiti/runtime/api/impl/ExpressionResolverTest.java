/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ExpressionResolverTest {

    private ExpressionResolver expressionResolver;

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ExpressionManager expressionManager;

    @Before
    public void setUp() {
        initMocks(this);
        expressionResolver = new ExpressionResolver(expressionManager,
                                                    mapper);
    }

    @Test
    public void containsExpression_should_returnFalse_when_sourceIsNull() {
        //given

        //when
        boolean containsExpression = expressionResolver.containsExpression(null);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnFalse_when_stringDoesNotContainExpressionPattern() {
        //given

        //when
        boolean containsExpression = expressionResolver.containsExpression("{just string with brackets, this is not an expression}");

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_stringContainsExpressionPattern() {
        //given

        //when
        boolean containsExpression = expressionResolver.containsExpression("${this is an expression}");

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void containsExpression_should_returnFalse_when_ObjectNodeDoesNotContainExpressionPattern() throws Exception {
        //given
        ObjectNode objectNode = mapper.readValue("{\"name\":  \"Peter\"}",
                                                 ObjectNode.class);

        //when
        boolean containsExpression = expressionResolver.containsExpression(objectNode);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_ObjectNodeContainsExpressionPattern() throws Exception {
        //given
        ObjectNode objectNode = mapper.readValue("{\"name\":  \"${name}\"}",
                                                 ObjectNode.class);

        //when
        boolean containsExpression = expressionResolver.containsExpression(objectNode);

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void containsExpression_should_returnFalse_when_MapValueDoesNotContainExpressionPattern() {
        //given
        Map<String, Integer> source = Collections.singletonMap("age",
                                                               10);

        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_MapValueContainsExpressionPattern() {
        //given
        Map<String, String> source = Collections.singletonMap("age",
                                                              "${age}");

        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void containsExpression_should_returnFalse_when_ListDoesNotContainExpressionPattern() {
        //given
        List<String> source = Arrays.asList("first",
                                            "second");
        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_ListContainsExpressionPattern() {
        //given
        List<String> source = Arrays.asList("first",
                                            "${position}",
                                            "third");
        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_stringIsAnExpression() {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);
        Expression expression = buildExpression("${name}");
        given(expression.getValue(execution)).willReturn("John");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(execution,
                                                                              Collections.singletonMap("name",
                                                                                                       "${name}"));
        //then
        assertThat(result).containsEntry("name",
                                         "John");
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_stringContainsAnExpression() {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);

        Expression nameExpression = buildExpression("${name}");
        given(nameExpression.getValue(execution)).willReturn("John");

        Expression placeExpression = buildExpression("${place}");
        given(placeExpression.getValue(execution)).willReturn("London");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(execution,
                                                                              Collections.singletonMap("welcomeMessage",
                                                                                                       "Welcome to ${place}, ${name}!"));
        //then
        assertThat(result).containsEntry("welcomeMessage",
                                         "Welcome to London, John!");
    }

    @Test
    public void resolveExpressionsMap_should_returnItself_when_stringIsEmpty() {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(execution,
                                                                              Collections.singletonMap("empty",
                                                                                                       ""));
        //then
        assertThat(result).containsEntry("empty",
                                         "");
    }

    @Test
    public void resolveExpressionsMap_should_keepExpressionContent_when_notAbleToResolveIt() {
        //given
        DelegateExecution execution = mock(DelegateExecution.class);
        Expression expression = buildExpression("${nonResolvableExpression}");
        given(expression.getValue(execution)).willThrow(new ActivitiException("Invalid property"));

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(execution,
                                                                              Collections.singletonMap("result",
                                                                                                       "${nonResolvableExpression}"));
        //then
        assertThat(result).containsEntry("result",
                                         "${nonResolvableExpression}");
    }

    private Expression buildExpression(String expressionContent) {
        Expression expression = mock(Expression.class);
        given(expressionManager.createExpression(expressionContent)).willReturn(expression);
        return expression;
    }
}
