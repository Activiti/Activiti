/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
public class ExpressionResolverTest {

    private static final JsonMapper MAPPER = new JsonMapper();

    private ExpressionResolver expressionResolver;

    @Mock
    private ExpressionManager expressionManager;

    @Mock
    private ExpressionEvaluator expressionEvaluator;

    @Mock
    private DelegateInterceptor delegateInterceptor;

    @BeforeEach
    public void setUp() {
        expressionResolver = new ExpressionResolver(expressionManager, MAPPER, delegateInterceptor);
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
        boolean containsExpression = expressionResolver.containsExpression(
            "{just string with brackets, this is not an expression}"
        );

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
        ObjectNode objectNode = MAPPER.readValue("{\"name\":  \"Peter\"}", ObjectNode.class);

        //when
        boolean containsExpression = expressionResolver.containsExpression(objectNode);

        //then
        assertThat(containsExpression).isFalse();
    }

    @Test
    public void containsExpression_should_returnTrue_when_ObjectNodeContainsExpressionPattern() throws Exception {
        //given
        ObjectNode objectNode = MAPPER.readValue("{\"name\":  \"${name}\"}", ObjectNode.class);

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
    public void findVariableNamesContainingExpressions_should_returnEmptyList_when_sourceIsNull() {
        //when
        List<String> result = expressionResolver.findVariableNamesContainingExpressions(null);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    public void findVariableNamesContainingExpressions_should_returnEmptyList_when_noVariableContainsExpression() {
        //given
        Map<String, Object> source = map("name", "John", "age", 30);

        //when
        List<String> result = expressionResolver.findVariableNamesContainingExpressions(source);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    public void findVariableNamesContainingExpressions_should_returnVariableNames_when_someVariablesContainExpressions() {
        //given
        Map<String, Object> source = map(
            "name",
            "${name}",
            "age",
            30,
            "place",
            "${place}",
            "list",
            asList("first", "${item}")
        );

        //when
        List<String> result = expressionResolver.findVariableNamesContainingExpressions(source);

        //then
        assertThat(result).containsExactlyInAnyOrder("name", "place", "list");
    }

    @ParameterizedTest
    @MethodSource("stringResolutionCases")
    public void resolveExpressionsMap_should_handleStringValues(
        String variableName,
        Object sourceValue,
        Map<String, Object> resolvedExpressions,
        List<String> unresolvableExpressions,
        Object expectedValue
    ) {
        //given
        stubResolvedExpressions(resolvedExpressions);
        stubUnresolvableExpressions(unresolvableExpressions);

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(
            expressionEvaluator,
            singletonMap(variableName, sourceValue)
        );

        //then
        assertThat(result).containsEntry(variableName, expectedValue);
    }

    @ParameterizedTest
    @MethodSource("structuredResolutionCases")
    public void resolveExpressionsMap_should_handleStructuredValues(
        String variableName,
        Object sourceValue,
        Map<String, Object> resolvedExpressions,
        List<String> unresolvableExpressions,
        Object expectedValue
    ) {
        //given
        stubResolvedExpressions(resolvedExpressions);
        stubUnresolvableExpressions(unresolvableExpressions);

        //when
        Map<String, Object> result = expressionResolver.resolveExpressionsMap(
            expressionEvaluator,
            singletonMap(variableName, sourceValue)
        );

        //then
        assertThat(result).containsEntry(variableName, expectedValue);
    }

    private static Stream<Arguments> stringResolutionCases() {
        return Stream.of(
            Arguments.of("name", "${name}", singletonMap("${name}", "John"), List.<String>of(), "John"),
            Arguments.of(
                "welcomeMessage",
                "Welcome to ${place}, ${name}!",
                map("${name}", "John", "${place}", "London"),
                List.<String>of(),
                "Welcome to London, John!"
            ),
            Arguments.of("empty", "", Map.<String, Object>of(), List.<String>of(), ""),
            Arguments.of("empty", null, Map.<String, Object>of(), List.<String>of(), null),
            Arguments.of(
                "result",
                "Welcome to ${nonResolvableExpression}!",
                Map.<String, Object>of(),
                List.of("${nonResolvableExpression}"),
                "Welcome to !"
            ),
            Arguments.of(
                "result",
                "${nonResolvableExpression}",
                Map.<String, Object>of(),
                List.of("${nonResolvableExpression}"),
                null
            )
        );
    }

    private static Stream<Arguments> structuredResolutionCases() {
        Map<String, Object> players = map("Red", "John", "Green", "Peter", "Blue", "Mary", "Yellow", "${player}");
        Map<String, Object> expectedPlayers = new HashMap<>(players);
        expectedPlayers.put("Yellow", null);

        return Stream.of(
            Arguments.of(
                "node",
                readNode("{\"name\":\"${name}\",\"place\":\"${place}\",\"age\":\"${age}\"}"),
                map("${name}", "John", "${place}", null, "${age}", 30),
                List.<String>of(),
                map("name", "John", "place", null, "age", 30)
            ),
            Arguments.of(
                "node",
                readNode("{\"name\":\"${name}\",\"age\": 30}"),
                Map.<String, Object>of(),
                List.of("${name}"),
                map("name", null, "age", 30)
            ),
            Arguments.of(
                "places",
                asList("${place}", "Paris", "Berlin"),
                singletonMap("${place}", "London"),
                List.<String>of(),
                asList("London", "Paris", "Berlin")
            ),
            Arguments.of(
                "places",
                asList("${place}", "Paris", "Berlin"),
                Map.<String, Object>of(),
                List.of("${place}"),
                asList(null, "Paris", "Berlin")
            ),
            Arguments.of(
                "players",
                map("Red", "John", "Green", "Peter", "Blue", "Mary", "Yellow", "${player}"),
                singletonMap("${player}", "Agatha"),
                List.<String>of(),
                map("Red", "John", "Green", "Peter", "Blue", "Mary", "Yellow", "Agatha")
            ),
            Arguments.of("players", players, Map.<String, Object>of(), List.of("${player}"), expectedPlayers)
        );
    }

    private static JsonNode readNode(String json) {
        return MAPPER.readTree(json);
    }

    private void stubResolvedExpressions(Map<String, Object> resolvedExpressions) {
        resolvedExpressions.forEach((expressionContent, resolvedValue) -> {
            Expression expression = buildExpression(expressionContent);
            given(expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor)).willReturn(
                resolvedValue
            );
        });
    }

    private void stubUnresolvableExpressions(List<String> unresolvableExpressions) {
        unresolvableExpressions.forEach(expressionContent -> {
            Expression expression = buildExpression(expressionContent);
            given(expressionEvaluator.evaluate(expression, expressionManager, delegateInterceptor)).willThrow(
                new ActivitiException("Invalid property")
            );
        });
    }

    private Expression buildExpression(String expressionContent) {
        Expression expression = mock(Expression.class);
        given(expressionManager.createExpression(expressionContent)).willReturn(expression);
        return expression;
    }
}
