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
package org.activiti.core.el;

import jakarta.el.ELContext;
import jakarta.el.MapELResolver;
import java.util.Map;

public class ContentELResolver extends MapELResolver {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (context == null) {
            throw new NullPointerException();
        } else if (base instanceof Map && isNumeric(property) && containsIndex(base,property)) {
            context.setPropertyResolved(base, property);
            return getIndex(base, property);
        } else {
            return null;
        }
    }

    private boolean isNumeric(Object property) {
        if (property == null) {
            return false;
        }
        try {
            Integer.parseInt(property.toString());
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean containsIndex(Object base, Object property) {
        return ((Map)base).containsKey(property.toString());
    }

    private Object getIndex(Object base, Object property) {
        return ((Map)base).get(property.toString());
    }

}
