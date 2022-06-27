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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class JuelResolverTest {

    @Test
    public void should_returnNull_when_nullExpressionIsPassed() {
        //given
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        Object value = expressionResolver.resolveExpression(null, Collections.emptyMap(), Object.class);

        //then
        assertThat(value).isNull();
    }

    @Test
    public void should_returnSameValue_when_StringWithoutJuelExpressionIsPassed() {
        //given
        String expressionString = "string with no JUEL expression";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        String value = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), String.class);

        //then
        assertThat(value).isEqualTo(expressionString);
    }

    @Test
    public void should_returnStringVariable_when_knownVariableIsReferenced() {
        //given
        Map<String, Object> availableVariables = Collections.singletonMap("name", "jon doe");
        String expressionString = "${name.toString()}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        String value = expressionResolver.resolveExpression(expressionString, availableVariables, String.class);

        //then
        assertThat(value).isEqualTo("jon doe");
    }

    @Test
    public void should_returnBoolean_when_expressionIsAPredicate() {
        //given
        String expressionString = "${1 > 0}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        boolean value = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), Boolean.class);

        //then
        assertThat(value).isTrue();
    }

    @Test
    public void should_throwException_when_unknownVariableIsReferenced() {

        //given
        Map<String, Object> availableVariables = Collections.singletonMap("name", "jon doe");
        String expressionString = "${nameeee}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //then
        assertThatExceptionOfType(PropertyNotFoundException.class)
            .as("Referencing an unknown variable")
            .isThrownBy(() -> expressionResolver.resolveExpression(expressionString, availableVariables, Object.class))
            .withMessage("Cannot resolve identifier 'nameeee'");
    }

    @Test
    public void should_returnDate_when_expressionIsNowFunction() {
        //given
        String expressionString = "${now()}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        Date value = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), Date.class);

        //then
        MatcherAssert.assertThat(value, is(notNullValue()));
    }

    @Test
    public void should_throwException_when_unknownFunctionIsReferenced() {
        //given
        String expressionString = "${current()}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //then
        assertThatExceptionOfType(ELException.class)
            .as("Referencing an unknown function")
            .isThrownBy(() -> expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), Date.class))
            .withMessage("Could not resolve function 'current'");
    }

    @Test
    public void should_returnList_when_expressionIsListFunction() {
        //given
        String expressionString = "${list(1,'item',3)}";
        ExpressionResolver expressionResolver = new JuelExpressionResolver();

        //when
        List result = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), List.class);

        //then
        assertThat(result).contains(1l, "item", 3l);
    }
}
