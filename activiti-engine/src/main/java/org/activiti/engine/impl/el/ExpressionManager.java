/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.el;

import java.util.Map;
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;

import de.odysseus.el.ExpressionFactoryImpl;
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
    // Default implementation (does nothing)
    protected ELContext parsingElContext = new ParsingElContext();
    protected Map<Object, Object> beans;

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
        if(initFactory) {
            expressionFactory = new ExpressionFactoryImpl();
        }
        this.beans = beans;
    }

    public Expression createExpression(String expression) {
        ValueExpression valueExpression = expressionFactory.createValueExpression(parsingElContext,
                                                                                  expression.trim(),
                                                                                  Object.class);
        return new JuelExpression(valueExpression,
                                  expression);
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory) {
        this.expressionFactory = expressionFactory;
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
        ELResolver elResolver = createElResolver(variableScope);
        return new ActivitiElContext(elResolver);
    }

    protected ELResolver createElResolver(VariableScope variableScope) {
        CompositeELResolver elResolver = new CompositeELResolver();
        elResolver.add(new VariableScopeElResolver(variableScope));

        if (beans != null) {
            // ACT-1102: Also expose all beans in configuration when using
            // standalone activiti, not
            // in spring-context
            elResolver.add(new ReadOnlyMapELResolver(beans));
        }

        elResolver.add(new ArrayELResolver());
        elResolver.add(new ListELResolver());
        elResolver.add(new MapELResolver());
        elResolver.add(new JsonNodeELResolver());
        elResolver.add(new DynamicBeanPropertyELResolver(ItemInstance.class,
                                                         "getFieldValue",
                                                         "setFieldValue")); // TODO: needs verification
        elResolver.add(new BeanELResolver());
        return elResolver;
    }

    public Map<Object, Object> getBeans() {
        return beans;
    }

    public void setBeans(Map<Object, Object> beans) {
        this.beans = beans;
    }
}
