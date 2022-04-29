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
package org.activiti.spring.process.variable.types;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateVariableTypeTest {

    private DateFormatterProvider provider = new DateFormatterProvider("yyyy-MM-dd[['T']HH:mm:ss[.SSS'Z']]");
    private List<ActivitiException> exceptionList;
    DateVariableType dateVariableType;

    @BeforeEach
    public void setUp() {
        dateVariableType = new DateVariableType(Date.class, provider);
        exceptionList = new ArrayList<>();
    }

    @Test
    public void should_returnDate_when_parseValidString() {
        Object result = dateVariableType.parseFromValue("1985-10-26T01:22:00.001Z");

        assertTrue(result.getClass().getName().equals(Date.class.getName()));
    }

    @Test
    public void should_throwException_when_parseInvalidString() {
        Throwable thrown = catchThrowable(() -> dateVariableType.parseFromValue("${now()"));

        Assertions.assertThat(thrown)
            .isInstanceOf(ActivitiException.class)
            .hasMessage("Error parsing date value ${now()");
    }

    @Test
    public void should_returnExpressionString_when_parseValidExpression() {
        String expression = "${now()}";
        Object result = dateVariableType.parseFromValue(expression);

        assertTrue(result.equals(expression));
    }
}
