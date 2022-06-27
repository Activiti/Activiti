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
package org.activiti.core.el;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;

/**
 * Builder of {@link ELContext} instances.
 */
public final class CommonELResolversUtil {

    private CommonELResolversUtil() {
        // Not intended to be instantiated
    }

    public static ELResolver arrayResolver() {
        return new ArrayELResolver();
    }

    public static ELResolver listResolver() {
        return new ListELResolver();
    }

    public static ELResolver mapResolver() {
        return new MapELResolver();
    }

    public static ELResolver jsonNodeResolver() {
        return new JsonNodeELResolver();
    }

    public static ELResolver beanResolver() {
        return new ELResolverReflectionBlockerDecorator(new BeanELResolver());
    }

}
