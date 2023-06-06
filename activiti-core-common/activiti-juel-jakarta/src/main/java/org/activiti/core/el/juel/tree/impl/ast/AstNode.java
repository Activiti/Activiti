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

package org.activiti.core.el.juel.tree.impl.ast;

import jakarta.el.ELContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.tree.ExpressionNode;

public abstract class AstNode implements ExpressionNode {

    /**
     * evaluate and return the (optionally coerced) result.
     */
    public final Object getValue(
        Bindings bindings,
        ELContext context,
        Class<?> type
    ) {
        Object value = eval(bindings, context);
        if (type != null) {
            value = bindings.convert(value, type);
        }
        return value;
    }

    public abstract void appendStructure(
        StringBuilder builder,
        Bindings bindings
    );

    public abstract Object eval(Bindings bindings, ELContext context);

    public final String getStructuralId(Bindings bindings) {
        StringBuilder builder = new StringBuilder();
        appendStructure(builder, bindings);
        return builder.toString();
    }

    /**
     * Find accessible method. Searches the inheritance tree of the class declaring
     * the method until it finds a method that can be invoked.
     * @param method method
     * @return accessible method or <code>null</code>
     */
    private static Method findPublicAccessibleMethod(Method method) {
        if (method == null || !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        if (
            method.isAccessible() ||
            Modifier.isPublic(method.getDeclaringClass().getModifiers())
        ) {
            return method;
        }
        for (Class<?> cls : method.getDeclaringClass().getInterfaces()) {
            Method mth = null;
            try {
                mth =
                    findPublicAccessibleMethod(
                        cls.getMethod(
                            method.getName(),
                            method.getParameterTypes()
                        )
                    );
                if (mth != null) {
                    return mth;
                }
            } catch (NoSuchMethodException ignore) {
                // do nothing
            }
        }
        Class<?> cls = method.getDeclaringClass().getSuperclass();
        if (cls != null) {
            Method mth = null;
            try {
                mth =
                    findPublicAccessibleMethod(
                        cls.getMethod(
                            method.getName(),
                            method.getParameterTypes()
                        )
                    );
                if (mth != null) {
                    return mth;
                }
            } catch (NoSuchMethodException ignore) {
                // do nothing
            }
        }
        return null;
    }

    protected Method findAccessibleMethod(Method method) {
        Method result = findPublicAccessibleMethod(method);
        if (
            result == null &&
            method != null &&
            Modifier.isPublic(method.getModifiers())
        ) {
            result = method;
            try {
                method.setAccessible(true);
            } catch (SecurityException e) {
                result = null;
            }
        }
        return result;
    }
}
