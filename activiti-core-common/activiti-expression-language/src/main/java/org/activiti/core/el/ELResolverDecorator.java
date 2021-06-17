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

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Generic Decorator for {@link ELResolver} implementations.
 */
public abstract class ELResolverDecorator extends ELResolver {

    private final ELResolver decoratedResolver;

    public ELResolverDecorator(ELResolver resolver) {
        this.decoratedResolver = resolver;
    }


    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        return decoratedResolver.getValue(context, base, property);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return decoratedResolver.getType(context, base, property);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        decoratedResolver.setValue(context, base, property, value);
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        return decoratedResolver.invoke(context, base, method, paramTypes, params);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return decoratedResolver.isReadOnly(context, base, property);
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return decoratedResolver.getFeatureDescriptors(context, base);
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return decoratedResolver.getCommonPropertyType(context, base);
    }
}
