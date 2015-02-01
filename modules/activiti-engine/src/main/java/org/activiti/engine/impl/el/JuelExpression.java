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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExpressionGetInvocation;
import org.activiti.engine.impl.delegate.ExpressionSetInvocation;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;
import org.activiti.engine.impl.javax.el.MethodNotFoundException;
import org.activiti.engine.impl.javax.el.PropertyNotFoundException;
import org.activiti.engine.impl.javax.el.ValueExpression;


/**
 * Expression implementation backed by a JUEL {@link ValueExpression}.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class JuelExpression implements Expression {

  protected String expressionText;
  protected ValueExpression valueExpression;
  
  public JuelExpression(ValueExpression valueExpression, String expressionText) {
    this.valueExpression = valueExpression;
    this.expressionText = expressionText;
  }

  public Object getValue(VariableScope variableScope) {
    ELContext elContext = Context.getProcessEngineConfiguration().getExpressionManager().getElContext(variableScope);
    try {
      ExpressionGetInvocation invocation = new ExpressionGetInvocation(valueExpression, elContext);
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
      return invocation.getInvocationResult();      
    } catch (PropertyNotFoundException pnfe) {
      throw new ActivitiException("Unknown property used in expression: " + expressionText, pnfe);
    } catch (MethodNotFoundException mnfe) {
      throw new ActivitiException("Unknown method used in expression: " + expressionText, mnfe);
    } catch(ELException ele) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, ele);
    } catch (Exception e) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, e);
    }
  }
    
  public void setValue(Object value, VariableScope variableScope) {
    ELContext elContext = Context.getProcessEngineConfiguration().getExpressionManager().getElContext(variableScope);
    try {
      ExpressionSetInvocation invocation = new ExpressionSetInvocation(valueExpression, elContext, value);
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(invocation);
    } catch (Exception e) {
      throw new ActivitiException("Error while evaluating expression: " + expressionText, e);
    }
  }
  
  @Override
  public String toString() {
    if(valueExpression != null) {
      return valueExpression.getExpressionString();
    }
    return super.toString();
  }
  
  public String getExpressionText() {
    return expressionText;
  }
}
