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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * {@link ELResolverDecorator} that blocks invocations using reflection or native calls.
 */
public class ELResolverReflectionBlockerDecorator extends ELResolverDecorator {

    private static final String JAVA_REFLECTION_PACKAGE = "java.lang.reflect";
    private static final Predicate<Method> IS_FINAL = method -> Modifier.isFinal(method.getModifiers());
    private static final Predicate<Method> IS_NATIVE = method -> Modifier.isNative(method.getModifiers());
    private static final Set<String> NATIVE_METHODS = Arrays.stream(Object.class.getMethods())
                                                            .filter(IS_FINAL.or(IS_NATIVE))
                                                            .map(Method::getName)
                                                            .collect(Collectors.toSet());

    public ELResolverReflectionBlockerDecorator(ELResolver resolver) {
        super(resolver);
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        final String basePackageName = base.getClass().getPackageName();
        if(JAVA_REFLECTION_PACKAGE.equals(basePackageName)) {
            throw new IllegalArgumentException("Illegal use of Reflection in a JUEL Expression");
        }

        if(NATIVE_METHODS.contains(method)) {
            throw new IllegalArgumentException("Illegal use of Native Method in a JUEL Expression");
        }
        return super.invoke(context, base, method, paramTypes, params);
    }
}
