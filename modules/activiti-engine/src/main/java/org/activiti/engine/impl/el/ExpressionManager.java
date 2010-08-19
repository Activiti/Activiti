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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.pvm.impl.runtime.ExecutionImpl;

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
 * <p>
 * Using this class, also the runtime engine doesn't create a hard dependency on
 * the javax.el library as long as you don't use expressions.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Dave Syer
 */
public class ExpressionManager implements ProcessEngineConfigurationAware {

  public static final String UEL_VALUE = "uel-value";
  public static final String UEL_METHOD = "uel-method";
  public static final String DEFAULT_EXPRESSION_LANGUAGE = UEL_VALUE;

  private ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
  // Default implementation (does nothing)
  private ELContext parsingElContext = new ParsingElContext();
  private ELResolver elResolver;

  /**
   * A custom variable resolver for expressions in process definitions. It will
   * have second highest priority after the native Activiti resolver based on
   * process instance variables. Could be used, for instance, to resolve a set
   * of global variables in a static engine wide scope. Defaults to null (so no
   * custom variables).
   */

  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.elResolver = processEngineConfiguration.getElResolver();
  }

  public ActivitiValueExpression createValueExpression(String expression) {
    ValueExpression valueExpression = expressionFactory.createValueExpression(parsingElContext, expression, Object.class);
    return new ActivitiValueExpression(valueExpression, this);
  }

  public ActivitiMethodExpression createMethodExpression(String expression) {
    // Returntype: passing in a value of null indicates the caller does not care
    // what the return type is, and the check is disabled.
    MethodExpression methodExpression = expressionFactory.createMethodExpression(parsingElContext, expression, null, new Class< ? >[] {});
    return new ActivitiMethodExpression(methodExpression, this);
  }

  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  public ELContext getElContext(ExecutionImpl execution) {
    if (! (execution instanceof ExecutionEntity)) {
      return createExecutionElContext(execution);
    }
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ELContext elContext = null;
    synchronized (execution) {
      elContext = (ELContext) executionEntity.getCachedElContext();
      if (elContext != null) {
        return elContext;
      }
      elContext = createExecutionElContext(execution);
      executionEntity.setCachedElContext(elContext);
    }
    return elContext;
  }

  protected ExecutionELContext createExecutionElContext(ExecutionImpl execution) {
    ExecutionELContext context = new ExecutionELContext(execution);
    if (elResolver != null) {
      context.setElResolver(elResolver);
    }
    return context;
  }
}
