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

import static org.activiti.core.el.CommonELResolversUtil.arrayResolver;
import static org.activiti.core.el.CommonELResolversUtil.beanResolver;
import static org.activiti.core.el.CommonELResolversUtil.jsonNodeResolver;
import static org.activiti.core.el.CommonELResolversUtil.listResolver;
import static org.activiti.core.el.CommonELResolversUtil.mapResolver;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.ValueExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JuelExpressionResolver implements ExpressionResolver {

    private final ExpressionFactory expressionFactory;
    private final List<CustomFunctionProvider> customFunctionProviders;

    public JuelExpressionResolver() {
        this(ExpressionFactory.newInstance());
    }

    public JuelExpressionResolver(ExpressionFactory expressionFactory) {
        this(expressionFactory, new ArrayList<>());
    }

    public JuelExpressionResolver(ExpressionFactory expressionFactory, List<CustomFunctionProvider> customFunctionProviders) {
        this.expressionFactory = expressionFactory;
        this.customFunctionProviders = customFunctionProviders;
    }

    @Override
    public <T> T resolveExpression(String expression, Map<String, Object> variables, Class<T> type) {
        if(expression == null) {
            return null;
        }
        final ELContext context = buildContext(variables);
        final ValueExpression valueExpression = expressionFactory.createValueExpression(context, expression, type);
        return (T)valueExpression.getValue(context);
    }

    protected ELContext buildContext (Map<String, Object> variables) {
        return new ELContextBuilder()
            .withResolvers(
                arrayResolver(),
                listResolver(),
                mapResolver(),
                jsonNodeResolver(),
                beanResolver()
            )
            .withVariables(variables)
            .buildWithCustomFunctions(customFunctionProviders);
    }
}
