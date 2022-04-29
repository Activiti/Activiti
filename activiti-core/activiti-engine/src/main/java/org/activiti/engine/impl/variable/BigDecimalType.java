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

import java.math.BigDecimal;
import java.util.Optional;

public class BigDecimalType implements VariableType {

    @Override
    public String getTypeName() {
        return "bigdecimal";
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        return Optional.ofNullable(valueFields)
                       .map(ValueFields::getTextValue)
                       .map(BigDecimal::new)
                       .orElse(null);
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        String textValue = Optional.ofNullable(value)
                                   .map(Object::toString)
                                   .orElse(null);

        valueFields.setTextValue(textValue);
    }

    @Override
    public boolean isAbleToStore(Object value) {
        return Optional.ofNullable(value)
                       .map(Object::getClass)
                       .map(BigDecimal.class::isAssignableFrom)
                       .orElse(true);
    }
}
