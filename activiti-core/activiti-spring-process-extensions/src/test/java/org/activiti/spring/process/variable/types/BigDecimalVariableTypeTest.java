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
package org.activiti.spring.process.variable.types;

import org.activiti.engine.ActivitiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BigDecimalVariableTypeTest {

    @Test
    public void should_returnException_when_validatingNotNumericType() {

        BigDecimalVariableType bigDecimalVariableType = new BigDecimalVariableType();
        List<ActivitiException> exceptionList = new ArrayList<>();

        String wrongType = "not a number";
        bigDecimalVariableType.validate(wrongType, exceptionList);

        assertThat(exceptionList.stream()).anyMatch(e ->
            e.getMessage().equals(String.format(BigDecimalVariableType.VALIDATION_ERROR_FORMAT, wrongType.getClass()))
        );
    }

    @Test
    public void should_returnEmptyExceptionList_when_validatingNumericType() {

        BigDecimalVariableType bigDecimalVariableType = new BigDecimalVariableType();
        List<ActivitiException> exceptionList = new ArrayList<>();

        bigDecimalVariableType.validate(1, exceptionList);
        bigDecimalVariableType.validate(2L, exceptionList);
        bigDecimalVariableType.validate(5.2, exceptionList);

        assertThat(exceptionList).isEmpty();
    }

    @Test
    public void should_returnAccurateResult_whenParsingDoubleValue() {

        BigDecimalVariableType bigDecimalVariableType = new BigDecimalVariableType();

        double value1 = 0.1;
        double value2 = 0.2;
        BigDecimal parsedValue1 = (BigDecimal) bigDecimalVariableType.parseFromValue(value1);
        BigDecimal parsedValue2 = (BigDecimal) bigDecimalVariableType.parseFromValue(value2);

        assertThat(parsedValue1.add(parsedValue2)).isEqualTo(new BigDecimal("0.3"));
    }

}
