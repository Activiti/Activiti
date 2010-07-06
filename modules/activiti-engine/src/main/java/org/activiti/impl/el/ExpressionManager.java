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
package org.activiti.impl.el;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import org.activiti.impl.execution.ExecutionImpl;


/** central manager for all expressions.
 * 
 * Process parsers will use this to build expression objects that are stored in the
 * process definitions.
 * 
 * Then also this class is used as an entry point for runtime evaluation of the 
 * expressions.
 * 
 * This class is configurable in the ProcessEngineConfiguration.  So all methods and 
 * functionalities that are potentially customizable, are realized as overridable methods
 * in this class. 
 * 
 * Using this class, also the runtime engine doesn't create a hard dependency on the 
 * javax.el library as long as you don't use expressions. 
 * 
 * @author Tom Baeyens
 */
public class ExpressionManager {

  public static final String UEL_VALUE = "uel-value";
  public static final String UEL_METHOD = "uel_method";
  public static final String DEFAULT_EXPRESSION_LANGUAGE = UEL_VALUE;
  
  ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
  ELContext parsingElContext = new ParsingElContext(); // Default implementation (does nothing)

  public ActivitiValueExpression createValueExpression(String expression) {
    ValueExpression valueExpression = expressionFactory.createValueExpression(parsingElContext, expression, Object.class);
    return new ActivitiValueExpression(valueExpression, this);
  }
  
  public ActivitiMethodExpression createMethodExpression(String expression) {
    MethodExpression methodExpression = expressionFactory.createMethodExpression(parsingElContext, expression, Object.class, null);
    return new ActivitiMethodExpression(methodExpression, this);
  }
  
  public ExpressionFactory getExpressionFactory() {
    return expressionFactory;
  }
  
  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }
  
  public ELContext getElContext(ExecutionImpl execution) {
    ELContext elContext = null;
    synchronized (execution) {
      elContext = (ELContext) execution.getCachedElContext();
      if (elContext!=null) {
        return elContext;
      }
      elContext = createExecutionElContext(execution);
      execution.setCachedElContext(elContext);
    }
    return elContext;
  }

  protected ExecutionELContext createExecutionElContext(ExecutionImpl execution) {
    return new ExecutionELContext(execution);
  }
}
