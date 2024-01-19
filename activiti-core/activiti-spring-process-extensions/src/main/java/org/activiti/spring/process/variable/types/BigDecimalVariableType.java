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

    private static final Logger logger = LoggerFactory.getLogger(BigDecimalVariableType.class);

    @Override
    public Object parseFromValue(Object value) throws ActivitiException {
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {
        if(!Number.class.isAssignableFrom(var.getClass())){
            String message = var.getClass() + "is not a numeric type";
            errors.add(new ActivitiException(message));
            logger.error(message);
        }
    }

}
