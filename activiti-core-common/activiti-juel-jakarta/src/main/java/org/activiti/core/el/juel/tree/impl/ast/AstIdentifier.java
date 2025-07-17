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
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import jakarta.el.ValueReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.activiti.core.el.juel.misc.LocalMessages;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.tree.IdentifierNode;

public class AstIdentifier extends AstNode implements IdentifierNode {

    private final String name;
    private final int index;
    private final boolean ignoreReturnType;

    public AstIdentifier(String name, int index) {
        this(name, index, false);
    }

    public AstIdentifier(String name, int index, boolean ignoreReturnType) {
        this.name = name;
        this.index = index;
        this.ignoreReturnType = ignoreReturnType;
    }

    public Class<?> getType(Bindings bindings, ELContext context) {
        ValueExpression expression = bindings.getVariable(index);
        if (expression != null) {
            return expression.getType(context);
        }
        context.setPropertyResolved(false);
        Class<?> result = context.getELResolver().getType(context, null, name);
        if (!context.isPropertyResolved()) {
            throw new PropertyNotFoundException(
                LocalMessages.get("error.identifier.property.notfound", name)
            );
        }
        return result;
    }

    public boolean isLeftValue() {
        return true;
    }

    public boolean isMethodInvocation() {
        return false;
    }

    public boolean isLiteralText() {
        return false;
    }

    public ValueReference getValueReference(
        Bindings bindings,
        ELContext context
    ) {
        ValueExpression expression = bindings.getVariable(index);
        if (expression != null) {
            return expression.getValueReference(context);
        }
        return new ValueReference(null, name);
    }

    @Override
    public Object eval(Bindings bindings, ELContext context) {
        ValueExpression expression = bindings.getVariable(index);
        if (expression != null) {
            return expression.getValue(context);
        }
        context.setPropertyResolved(false);
        Object result = context.getELResolver().getValue(context, null, name);
        if (!context.isPropertyResolved()) {
            throw new PropertyNotFoundException(
                LocalMessages.get("error.identifier.property.notfound", name)
            );
        }
        return result;
    }

    public void setValue(Bindings bindings, ELContext context, Object value) {
        ValueExpression expression = bindings.getVariable(index);
        if (expression != null) {
            expression.setValue(context, value);
            return;
        }
        context.setPropertyResolved(false);
        Class<?> type = context.getELResolver().getType(context, null, name);
        if (context.isPropertyResolved()) {
            if (type != null && (value != null || type.isPrimitive())) {
                value = bindings.convert(value, type);
            }
            context.setPropertyResolved(false);
        }
        context.getELResolver().setValue(context, null, name, value);
        if (!context.isPropertyResolved()) {
            throw new PropertyNotFoundException(
                LocalMessages.get("error.identifier.property.notfound", name)
            );
        }
    }

    public boolean isReadOnly(Bindings bindings, ELContext context) {
        ValueExpression expression = bindings.getVariable(index);
        if (expression != null) {
            return expression.isReadOnly(context);
        }
        context.setPropertyResolved(false);
        boolean result = context
            .getELResolver()
            .isReadOnly(context, null, name);
        if (!context.isPropertyResolved()) {
            throw new PropertyNotFoundException(
                LocalMessages.get("error.identifier.property.notfound", name)
            );
        }
        return result;
    }

    protected MethodExpression getMethodExpression(
        Bindings bindings,
        ELContext context,
        Class<?> returnType,
        Class<?>[] paramTypes
    ) {
        Object value = eval(bindings, context);
        if (value == null) {
            throw new MethodNotFoundException(
                LocalMessages.get("error.identifier.method.notfound", name)
            );
        }
        if (value instanceof Method) {
            final Method method = findAccessibleMethod((Method) value);
            if (method == null) {
                throw new MethodNotFoundException(
                    LocalMessages.get("error.identifier.method.notfound", name)
                );
            }
            if (
                !ignoreReturnType &&
                returnType != null &&
                !returnType.isAssignableFrom(method.getReturnType())
            ) {
                throw new MethodNotFoundException(
                    LocalMessages.get(
                        "error.identifier.method.returntype",
                        method.getReturnType(),
                        name,
                        returnType
                    )
                );
            }
            if (!Arrays.equals(method.getParameterTypes(), paramTypes)) {
                throw new MethodNotFoundException(
                    LocalMessages.get("error.identifier.method.notfound", name)
                );
            }
            return new MethodExpression() {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isLiteralText() {
                    return false;
                }

                @Override
                public String getExpressionString() {
                    return null;
                }

                @Override
                public int hashCode() {
                    return 0;
                }

                @Override
                public boolean equals(Object obj) {
                    return obj == this;
                }

                @Override
                public Object invoke(ELContext context, Object[] params) {
                    try {
                        return method.invoke(null, params);
                    } catch (IllegalAccessException e) {
                        throw new ELException(
                            LocalMessages.get(
                                "error.identifier.method.access",
                                name
                            )
                        );
                    } catch (IllegalArgumentException e) {
                        throw new ELException(
                            LocalMessages.get(
                                "error.identifier.method.invocation",
                                name,
                                e
                            )
                        );
                    } catch (InvocationTargetException e) {
                        throw new ELException(
                            LocalMessages.get(
                                "error.identifier.method.invocation",
                                name,
                                e.getCause()
                            )
                        );
                    }
                }

                @Override
                public MethodInfo getMethodInfo(ELContext context) {
                    return new MethodInfo(
                        method.getName(),
                        method.getReturnType(),
                        method.getParameterTypes()
                    );
                }
            };
        } else if (value instanceof MethodExpression) {
            return (MethodExpression) value;
        }
        throw new MethodNotFoundException(
            LocalMessages.get(
                "error.identifier.method.notamethod",
                name,
                value.getClass()
            )
        );
    }

    public MethodInfo getMethodInfo(
        Bindings bindings,
        ELContext context,
        Class<?> returnType,
        Class<?>[] paramTypes
    ) {
        return getMethodExpression(bindings, context, returnType, paramTypes)
            .getMethodInfo(context);
    }

    public Object invoke(
        Bindings bindings,
        ELContext context,
        Class<?> returnType,
        Class<?>[] paramTypes,
        Object[] params
    ) {
        return getMethodExpression(bindings, context, returnType, paramTypes)
            .invoke(context, params);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void appendStructure(StringBuilder b, Bindings bindings) {
        b.append(
            bindings != null && bindings.isVariableBound(index) ? "<var>" : name
        );
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getCardinality() {
        return 0;
    }

    public AstNode getChild(int i) {
        return null;
    }
}
