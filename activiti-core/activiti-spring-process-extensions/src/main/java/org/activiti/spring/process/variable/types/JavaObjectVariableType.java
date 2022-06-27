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
import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For defining types to match json extension files where types correspond to java classes
 */
public class JavaObjectVariableType extends VariableType {

    private static final Logger logger = LoggerFactory.getLogger(JavaObjectVariableType.class);

    public Class clazz;

    public JavaObjectVariableType(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void validate(Object var, List<ActivitiException> errors) {

        if (var != null && !(var).getClass().isAssignableFrom(clazz) && !isExpression(var)) {
            String message = var.getClass() + " is not assignable from " + clazz;
            errors.add(new ActivitiException(message));
            logger.error(message);
        }
    }
}
