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
package org.activiti.spring.resolver;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;

public class EnvironmentVariableELResolver extends ELResolver {

    private static final String VAR_PREFIX = "vars";
    public static final String VAR_PREFIX_WITH_DOT = VAR_PREFIX + ".";

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null && VAR_PREFIX.equals(property)) {
            context.setPropertyResolved(true);
            return new EnvVar();
        }
        if (base instanceof EnvVar && property != null) {
            String env = System.getenv(VAR_PREFIX_WITH_DOT + property);
                context.setPropertyResolved(true);
                return env;
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext elContext, Object o, Object o1) {
        return String.class;
    }

    @Override
    public void setValue(ELContext elContext, Object o, Object o1, Object o2) {
        throw new UnsupportedOperationException("Environment variables are read-only");
    }

    @Override
    public boolean isReadOnly(ELContext elContext, Object o, Object o1) {
        return true;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext elContext, Object o) {
        return String.class;
    }
}
