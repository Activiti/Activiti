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
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ELException;
import org.activiti.javax.el.MethodExpression;
import org.activiti.javax.el.MethodNotFoundException;
import org.activiti.javax.el.ValueExpression;
import org.activiti.pvm.delegate.DelegateExecution;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class ActivitiMethodExpression {

  protected MethodExpression methodExpression;
  protected ExpressionManager expressionManager;
  protected ValueExpression valueExpression;

  public ActivitiMethodExpression(MethodExpression methodExpression, ExpressionManager expressionManager) {
    this.methodExpression = methodExpression;
    this.expressionManager = expressionManager;
  }
  
  public ActivitiMethodExpression(ValueExpression valueExpression, ExpressionManager expressionManager) {
    this.valueExpression = valueExpression;
    this.expressionManager = expressionManager;
  }

  public Object invoke(DelegateExecution execution) {
    ELContext elContext = expressionManager.getElContext((ExecutionEntity) execution);
    if(valueExpression != null) {
      try {
        return valueExpression.getValue(elContext);        
      } catch(ELException ele) {
        throw new ActivitiException("Error occured while invoking method '" +valueExpression + "'", ele);
      }
    } else if(methodExpression != null) { 
      try {
        return methodExpression.invoke(elContext, null);
      } catch (MethodNotFoundException e) {
        throw new ActivitiException("Unknown method used in expression '"+methodExpression+"'", e);
      }
    } else {
      throw new ActivitiException("Nothing to invoke, both methodExpression and valueExpression are null");
    }
  }
}
