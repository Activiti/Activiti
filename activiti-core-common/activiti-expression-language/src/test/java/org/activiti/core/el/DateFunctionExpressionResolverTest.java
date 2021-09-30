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
package org.activiti.core.el;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import java.util.Collections;
import org.junit.Test;

public class DateFunctionExpressionResolverTest {

    @Test
    public void should_returnDate_when_expressionIsTodayFunction() {
        //given
        String expressionString = "${today()}";
        DateFunctionExpressionResolver expressionResolver = new DateFunctionExpressionResolver();

        //when
        String value = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), String.class);

        //then
        assertThat(value, is(notNullValue()));
        assertThat(value, matchesPattern("([0-9]{4})\\-([0-9]{2})\\-([0-9]{2})"));
    }

    @Test
    public void should_returnDate_when_expressionIsCurrentFunction() {
        //given
        String expressionString = "${current()}";
        DateFunctionExpressionResolver expressionResolver = new DateFunctionExpressionResolver();

        //when
        String value = expressionResolver.resolveExpression(expressionString, Collections.emptyMap(), String.class);

        //then
        assertThat(value, is(notNullValue()));
        assertThat(value, matchesPattern(
            "^(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-"
                + "(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|"
                + "(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:\\.\\d{1,9})?(?:Z|[+-][01]\\d:[0-5]\\d)$"));
    }
}
