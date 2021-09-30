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

import java.util.Map;
import java.util.Objects;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import de.odysseus.el.ExpressionFactoryImpl;

public class DateFunctionExpressionResolver implements ExpressionResolver {

    private final ExpressionFactory expressionFactory;

    public DateFunctionExpressionResolver(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
    }

    public DateFunctionExpressionResolver() {
        this(new ExpressionFactoryImpl());
    }

    @Override
    public <T> T resolveExpression(String expression, Map<String, Object> variables, Class<T> type) {
        if (Objects.isNull(expression)) {
            return null;
        }
        final ELContext context = buildContext();
        final ValueExpression valueExpression = expressionFactory.createValueExpression(context, expression, type);
        return (T) valueExpression.getValue(context);
    }

    protected ELContext buildContext() {
        ActivitiElContext context = (ActivitiElContext) new ELContextBuilder().build();
        try {
            context.setFunction("", "today", DateResolverHelper.class.getMethod("today"));
            context.setFunction("", "current", DateResolverHelper.class.getMethod("current"));
        } catch (NoSuchMethodException e) {
        }
        return context;
    }
}
