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

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LocalDateTimeType implements VariableType {

    public String getTypeName() {
        return "localDateTime";
    }

    public boolean isCachable() {
        return true;
    }

    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }
        return LocalDateTime.class.isAssignableFrom(value.getClass());
    }

    public Object getValue(ValueFields valueFields) {
        Long longValue = valueFields.getLongValue();
        if (longValue != null) {
            return LocalDateTime.ofEpochSecond(longValue, 0, ZoneOffset.UTC);
        }
        return null;
    }

    public void setValue(Object value, ValueFields valueFields) {
        if (value != null) {
            valueFields.setLongValue(((LocalDateTime) value).toEpochSecond(ZoneOffset.UTC));
        } else {
            valueFields.setLongValue(null);
        }
    }
}
