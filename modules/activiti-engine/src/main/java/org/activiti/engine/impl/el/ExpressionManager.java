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

import org.activiti.engine.impl.javax.el.ArrayELResolver;
import org.activiti.engine.impl.javax.el.BeanELResolver;
import org.activiti.engine.impl.javax.el.CompositeELResolver;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.ListELResolver;
import org.activiti.engine.impl.javax.el.MapELResolver;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * <p>
 * Central manager for all expressions.
 * </p>
 * <p>
 * Process parsers will use this to build expression objects that are stored in
 * the process definitions.
 * </p>
 * <p>
 * Then also this class is used as an entry point for runtime evaluation of the
 * expressions.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Dave Syer
 * @author Frederik Heremans
 */
public class ExpressionManager {


  protected ExpressionFactory expressionFactory;
  // Default implementation (does nothing)
  protected ELContext parsingElContext = new ParsingElContext();
  
  
  public ExpressionManager() {
    // Use the ExpressionFactoryImpl in activiti-juel, with parametrised method expressions
    // enabled
    expressionFactory = new ExpressionFactoryImpl();
  }

 
  
  public Expression createExpression(String expression) {
    ValueExpression valueExpression = expressionFactory.createValueExpression(parsingElContext, expression, Object.class);
    return new JuelExpression(valueExpression, this);
  }

  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  public ELContext getElContext(ExecutionImpl execution) {
    if (! (execution instanceof ExecutionEntity)) {
      return createElContext(execution);
    }
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ELContext elContext = null;
    synchronized (execution) {
      elContext = (ELContext) executionEntity.getCachedElContext();
      if (elContext != null) {
        return elContext;
      }
      elContext = createElContext(execution);
      executionEntity.setCachedElContext(elContext);
    }
    return elContext;
  }

  protected ActivitiElContext createElContext(ExecutionImpl execution) {
    ELResolver elResolver = createElResolver(execution);
    return new ActivitiElContext(elResolver);
  }

  protected ELResolver createElResolver(ExecutionImpl execution) {
    CompositeELResolver elResolver = new CompositeELResolver();
    elResolver.add(new ExecutionVariableElResolver(execution));
    elResolver.add(new ArrayELResolver());
    elResolver.add(new ListELResolver());
    elResolver.add(new MapELResolver());
    elResolver.add(new BeanELResolver());
    return elResolver;
  }
}
