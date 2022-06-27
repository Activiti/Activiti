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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExpressionResolverTest {

    private ExpressionResolver expressionResolver;

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ExpressionManager expressionManager;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private DelegateInterceptor delegateInterceptor;

    @BeforeEach
    public void setUp() {
        expressionResolver = new ExpressionResolver(expressionManager,
                                                    mapper, delegateInterceptor);
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
    public void containsExpression_should_returnFalse_when_ObjectNodeDoesNotContainExpressionPattern()
                                                                                                       throws Exception {
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
        Map<String, Integer> source = singletonMap("age", 10);

        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_MapValueContainsExpressionPattern() {
        //given
        Map<String, String> source = singletonMap("age", "${age}");

        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void containsExpression_should_returnFalse_when_ListDoesNotContainExpressionPattern() {
        //given
        List<String> source = asList("first", "second");
        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_ListContainsExpressionPattern() {
        //given
        List<String> source = asList("first", "${position}", "third");
        //when
        boolean containsExpression = expressionResolver.containsExpression(source);

        //then
        assertThat(containsExpression).isTrue();
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_stringIsAnExpression() {
        //given
        Expression expression = buildExpression("${name}");
        given(expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor)).willReturn("John");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("name", "${name}"));
        //then
        assertThat(result).containsEntry("name", "John");
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_stringContainsAnExpression() {
        //given
        Expression nameExpression = buildExpression("${name}");
        given(expressionEvaluator.evaluate(nameExpression, expressionManager, delegateInterceptor)).willReturn("John");

        Expression placeExpression = buildExpression("${place}");
        given(expressionEvaluator.evaluate(placeExpression, expressionManager, delegateInterceptor)).willReturn("London");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("welcomeMessage", "Welcome to ${place}, ${name}!"));
        //then
        assertThat(result).containsEntry("welcomeMessage", "Welcome to London, John!");
    }

    @Test
    public void resolveExpressionsMap_should_returnItself_when_stringIsEmpty() {
        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("empty", ""));

        //then
        assertThat(result).containsEntry("empty", "");
    }

    @Test
    public void resolveExpressionsMap_should_returnItself_when_stringIsNull() {
        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("empty", null));
        //then
        assertThat(result).containsEntry("empty", null);
    }

    @Test
    public void resolveExpressionsMap_should_keepExpressionContent_when_notAbleToResolveExpressionInString() {
        //given
        Expression expression = buildExpression("${nonResolvableExpression}");
        given(expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor)).willThrow(new ActivitiException("Invalid property"));

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("result", "Welcome to ${nonResolvableExpression}!"));
        //then
        assertThat(result).containsEntry("result", "Welcome to ${nonResolvableExpression}!");
    }

    @Test
    public void resolveExpressionsMap_should_keepExpressionContent_when_notAbleToResolveIt() {
        //given
        Expression expression = buildExpression("${nonResolvableExpression}");
        given(expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor)).willThrow(new ActivitiException("Invalid property"));

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("result", "${nonResolvableExpression}"));
        //then
        assertThat(result).containsEntry("result",
                                         "${nonResolvableExpression}");

    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_ObjectNodeContainsAnExpression() throws IOException {
        //given
        Expression nameExpression = buildExpression("${name}");
        given(expressionEvaluator.evaluate(nameExpression, expressionManager, delegateInterceptor)).willReturn("John");

        Expression placeExpression = buildExpression("${place}");
        given(expressionEvaluator.evaluate(placeExpression, expressionManager, delegateInterceptor)).willReturn(null);

        Expression ageExpression = buildExpression("${age}");
        given(expressionEvaluator.evaluate(ageExpression, expressionManager, delegateInterceptor)).willReturn(30);

        JsonNode node = mapper.readTree("{\"name\":\"${name}\",\"place\":\"${place}\",\"age\":\"${age}\"}");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("node", node));
        //then
        assertThat(result).containsEntry("node", map(
            "name", "John",
            "place", null,
            "age", 30
        ));
    }

    @Test
    public void
           resolveExpressionsMap_should_keepExpressionContent_when_ObjecNodeContainsAnExpressionUnableToBeResolved() throws IOException {
        //given
        Expression nameExpression = buildExpression("${name}");
        given(expressionEvaluator.evaluate(nameExpression, expressionManager, delegateInterceptor)).willThrow(new ActivitiException("Invalid property"));

        JsonNode node = mapper.readTree("{\"name\":\"${name}\",\"age\": 30}");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("node", node));
        //then
        assertThat(result).containsEntry("node", map(
            "name", "${name}",
            "age", 30
        ));
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_ListContainsAnExpression() {
        //given
        Expression placeExpression = buildExpression("${place}");
        given(expressionEvaluator.evaluate(placeExpression, expressionManager, delegateInterceptor)).willReturn("London");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("places", asList("${place}",
                                                                                                            "Paris",
                                                                                                            "Berlin")));
        //then
        assertThat(result).containsEntry("places",
                                         asList("London",
                                                "Paris",
                                                "Berlin"));
    }

    @Test
    public void resolveExpressionsMap_should_keepExpressionContent_when_ListContainsAnExpressionUnableToBeResolved() {
        //given
        Expression placeExpression = buildExpression("${place}");
        given(expressionEvaluator.evaluate(placeExpression, expressionManager, delegateInterceptor)).willThrow(new ActivitiException("Invalid property"));

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("places",
                                                                                                       asList("${place}",
                                                                                                                     "Paris",
                                                                                                                     "Berlin")));
        //then
        assertThat(result).containsEntry("places",
                                         asList("${place}",
                                                       "Paris",
                                                       "Berlin"));
    }

    @Test
    public void resolveExpressionsMap_should_replaceExpressionByValue_when_MapContainsAnExpression() {
        //given
        Expression playerExpression = buildExpression("${player}");
        given(expressionEvaluator.evaluate(playerExpression, expressionManager, delegateInterceptor)).willReturn("Agatha");

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("players",
                                                                              map(
                                                                                  "Red", "John",
                                                                                  "Green", "Peter",
                                                                                  "Blue", "Mary",
                                                                                  "Yellow", "${player}"
                                                                              )));

        //then
        assertThat(result).containsEntry("players", map(
            "Red", "John",
            "Green", "Peter",
            "Blue", "Mary",
            "Yellow", "Agatha"
        ));
    }

    @Test
    public void resolveExpressionsMap_should_keepExpressionContent_when_MapContainsAnExpressionUnableToBeResolved() {
        //given

        Expression playerExpression = buildExpression("${player}");
        given(expressionEvaluator.evaluate(playerExpression, expressionManager, delegateInterceptor)).willThrow(new ActivitiException("Invalid property"));

        Map<String, Object> players = map(
            "Red", "John",
            "Green", "Peter",
            "Blue", "Mary",
            "Yellow", "${player}"
        );

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(expressionEvaluator,
                                                                              singletonMap("players",players));

        //then
        assertThat(result).containsEntry("players", players);
    }

    private Expression buildExpression(String expressionContent) {
        Expression expression = mock(Expression.class);
        given(expressionManager.createExpression(expressionContent)).willReturn(expression);
        return expression;
    }
}
