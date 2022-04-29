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

package org.activiti.engine.impl.variable;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.junit.Test;

public class BigDecimalTypeTest {

    private BigDecimalType bigDecimalType = new BigDecimalType();

    @Test
    public void getTypeName_should_return_bigdecimal() {
        assertThat(bigDecimalType.getTypeName()).isEqualTo("bigdecimal");
    }

    @Test
    public void isCachable_should_return_true() {
        assertThat(bigDecimalType.isCachable()).isTrue();
    }

    @Test
    public void getValue_should_convertTextValueToBigDecimal() {
        //given
        ValueFields valueFields = new VariableInstanceEntityImpl();
        valueFields.setTextValue("0.1000");

        //when
        Object convertedValue = bigDecimalType.getValue(valueFields);

        //then
        assertThat(convertedValue).isEqualTo(BigDecimal.valueOf(1000, 4));
    }

    @Test
    public void setValue_should_setTextValueWithStringRepresentationOfBigDecimal() {
        //given
        ValueFields valueFields = new VariableInstanceEntityImpl();

        //when
        bigDecimalType.setValue(BigDecimal.valueOf(1000,4), valueFields);

        //then
        assertThat(valueFields.getTextValue()).isEqualTo("0.1000");
    }

    @Test
    public void isAbleToStore_should_returnTrue_when_itsBigDecimal() {
        assertThat(bigDecimalType.isAbleToStore(BigDecimal.valueOf(1))).isTrue();
    }

    @Test
    public void isAbleToStore_should_returnTrue_when_itsNull() {
        assertThat(bigDecimalType.isAbleToStore(null)).isTrue();
    }

    @Test
    public void isAbleToStore_should_returnFalse_when_itsNotBigDecimal() {
        assertThat(bigDecimalType.isAbleToStore("Anything that's not bigDecimal")).isFalse();
    }

    @Test
    public void getValue_should_convertNullValue() {
        //given
        ValueFields valueFields = new VariableInstanceEntityImpl();
        valueFields.setTextValue(null);

        //when
        Object convertedValue = bigDecimalType.getValue(valueFields);

        //then
        assertThat(convertedValue).isNull();
    }

    @Test
    public void setValue_should_setNullValue() {
        //given
        ValueFields valueFields = new VariableInstanceEntityImpl();
        valueFields.setTextValue("someValue");

        //when
        bigDecimalType.setValue(null, valueFields);

        //then
        assertThat(valueFields.getTextValue()).isNull();
    }

}
