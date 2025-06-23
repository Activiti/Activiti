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

import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.ValueExpression;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.activiti.core.el.ActivitiElContext;
import org.activiti.core.el.CustomFunctionProvider;
import org.activiti.core.el.ELContextBuilder;
import org.activiti.core.el.ELResolverReflectionBlockerDecorator;
import org.activiti.core.el.ReadOnlyMapELResolver;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.persistence.entity.VariableScopeImpl;

/**
 * <p>
 * Central manager for all expressions.
 * </p>
 * <p>
 * Process parsers will use this to build expression objects that are stored in the process definitions.
 * </p>
 * <p>
 * Then also this class is used as an entry point for runtime evaluation of the expressions.
 * </p>
 */
public class ExpressionManager {

    protected ExpressionFactory expressionFactory;
    protected Map<Object, Object> beans;
    protected List<CustomFunctionProvider> customFunctionProviders;
    protected List<ELResolver> customELResolvers;

    public ExpressionManager() {
        this(null);
    }

    public ExpressionManager(boolean initFactory) {
        this(null,
             initFactory);
    }

    public ExpressionManager(Map<Object, Object> beans) {
        this(beans,
             true);
    }

    public ExpressionManager(Map<Object, Object> beans,
                             boolean initFactory) {
        // Use the ExpressionFactoryImpl in activiti build in version of juel,
        // with parametrised method expressions enabled
        if (initFactory) {
            expressionFactory = ExpressionFactory.newInstance();
        }
        this.beans = beans;
    }

    public Expression createExpression(String expression) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(getElContext(Collections.emptyMap()),
            expression.trim(),
            Object.class);
        return new JuelExpression(valueExpression,
            expression);
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
    }

    public List<CustomFunctionProvider> getCustomFunctionProviders() {
        return customFunctionProviders;
    }

    public void setCustomFunctionProviders(List<CustomFunctionProvider> customFunctionProviders) {
        this.customFunctionProviders = customFunctionProviders;
    }

    public List<ELResolver> getCustomELResolvers() {
        return customELResolvers;
    }

    public void setCustomELResolvers(List<ELResolver> customELResolvers) {
        this.customELResolvers = customELResolvers;
    }

    public ELContext getElContext(VariableScope variableScope) {
        ELContext elContext = null;
        if (variableScope instanceof VariableScopeImpl) {
            VariableScopeImpl variableScopeImpl = (VariableScopeImpl) variableScope;
            elContext = variableScopeImpl.getCachedElContext();
        }

        if (elContext == null) {
            elContext = createElContext(variableScope);
            if (variableScope instanceof VariableScopeImpl) {
                ((VariableScopeImpl) variableScope).setCachedElContext(elContext);
            }
        }

        return elContext;
    }

    protected ActivitiElContext createElContext(VariableScope variableScope) {
        return (ActivitiElContext) new ELContextBuilder().withResolvers(createElResolver(variableScope)).buildWithCustomFunctions(customFunctionProviders);
    }

    protected ELResolver createElResolver(VariableScope variableScope) {
        CompositeELResolver elResolver = new CompositeELResolver();
        elResolver.add(new VariableScopeElResolver(variableScope));
        if (customELResolvers != null) {
            customELResolvers.forEach(elResolver::add);
        }
        addBeansResolver(elResolver);
        addBaseResolvers(elResolver);
        return elResolver;
    }

    protected void addBeansResolver(CompositeELResolver elResolver) {
        if (beans != null) {
            // ACT-1102: Also expose all beans in configuration when using
            // standalone activiti, not
            // in spring-context
            elResolver.add(new ReadOnlyMapELResolver(beans));
        }
    }

    private void addBaseResolvers(CompositeELResolver elResolver) {
        elResolver.add(new ArrayELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new MapELResolver());
        elResolver.add(new CustomMapperJsonNodeELResolver());
        elResolver.add(new DynamicBeanPropertyELResolver(ItemInstance.class,
                                                         "getFieldValue",
                                                         "setFieldValue")); // TODO: needs verification
        elResolver.add(new ELResolverReflectionBlockerDecorator(new BeanELResolver()));
    }

    public Map<Object, Object> getBeans() {
        return beans;
    }

    public void setBeans(Map<Object, Object> beans) {
        this.beans = beans;
    }

    public ELContext getElContext(Map<String, Object> availableVariables) {
        CompositeELResolver elResolver = new CompositeELResolver();
        addBaseResolvers(elResolver);
        return new ELContextBuilder().withResolvers(elResolver).withVariables(availableVariables).buildWithCustomFunctions(customFunctionProviders);
    }
}
