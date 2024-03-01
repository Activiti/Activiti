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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class BigDecimalVariableType extends VariableType {

    public static final String VALIDATION_ERROR_FORMAT = "%s is not a numeric type";

    private static final Logger logger = LoggerFactory.getLogger(BigDecimalVariableType.class);

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {

        if(value instanceof BigDecimal) {
            return value;
        }
        try {
            if (value instanceof String) {
                return new BigDecimal((String) value);
            }
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } catch (ClassCastException | NumberFormatException e) {
            throw new ActivitiException("Error parsing bigdecimal value from " + value + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        if(!Number.class.isAssignableFrom(var.getClass())){
            String message = String.format(VALIDATION_ERROR_FORMAT, var.getClass());
            errors.add(new ActivitiException(message));
            logger.error(message);
        }
    }

}
