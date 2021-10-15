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

package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import org.activiti.engine.impl.util.ReflectUtil;

/**
 * A {@link ELResolver} for dynamic bean properties
 *

 */
public class DynamicBeanPropertyELResolver extends ELResolver {

    protected Class<?> subject;

    protected String readMethodName;

    protected String writeMethodName;

    protected boolean readOnly;

    public DynamicBeanPropertyELResolver(
        boolean readOnly,
        Class<?> subject,
        String readMethodName,
        String writeMethodName
    ) {
        this.readOnly = readOnly;
        this.subject = subject;
        this.readMethodName = readMethodName;
        this.writeMethodName = writeMethodName;
    }

    public DynamicBeanPropertyELResolver(
        Class<?> subject,
        String readMethodName,
        String writeMethodName
    ) {
        this(false, subject, readMethodName, writeMethodName);
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (this.subject.isInstance(base)) {
            return Object.class;
        } else {
            return null;
        }
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(
        ELContext context,
        Object base
    ) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        if (base == null || this.getCommonPropertyType(context, base) == null) {
            return null;
        }

        context.setPropertyResolved(true);
        return Object.class;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null || this.getCommonPropertyType(context, base) == null) {
            return null;
        }

        String propertyName = property.toString();

        try {
            Object value = ReflectUtil.invoke(
                base,
                this.readMethodName,
                new Object[] { propertyName }
            );
            context.setPropertyResolved(true);
            return value;
        } catch (Exception e) {
            throw new ELException(e);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return this.readOnly;
    }

    @Override
    public void setValue(
        ELContext context,
        Object base,
        Object property,
        Object value
    ) {
        if (base == null || this.getCommonPropertyType(context, base) == null) {
            return;
        }

        String propertyName = property.toString();
        try {
            ReflectUtil.invoke(
                base,
                this.writeMethodName,
                new Object[] { propertyName, value }
            );
            context.setPropertyResolved(true);
        } catch (Exception e) {
            throw new ELException(e);
        }
    }
}
