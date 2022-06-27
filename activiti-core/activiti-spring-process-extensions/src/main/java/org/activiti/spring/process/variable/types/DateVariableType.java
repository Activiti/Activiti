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

import java.util.List;
import org.activiti.common.util.DateFormatterProvider;
import org.activiti.engine.ActivitiException;

/**
 * Basic date type for setting default date values for vars in extension json
 */
public class DateVariableType extends JavaObjectVariableType {

    private final DateFormatterProvider dateFormatterProvider;

    public DateVariableType(Class clazz, DateFormatterProvider dateFormatterProvider) {
        super(clazz);
        this.dateFormatterProvider = dateFormatterProvider;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        super.validate(var, errors);
    }

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {

        try {
            if (isExpression(value)) {
                return value;
            }
            return dateFormatterProvider.toDate(value);
        } catch (Exception e) {
            throw new ActivitiException("Error parsing date value " + value, e);
        }
    }
}
