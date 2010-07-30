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
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;

import org.activiti.engine.ActivitiException;
import org.activiti.pvm.activity.ActivityContext;
import org.activiti.pvm.impl.runtime.ExecutionContextImpl;
import org.activiti.pvm.impl.runtime.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ActivitiMethodExpression {

  protected MethodExpression methodExpression;
  protected ExpressionManager expressionManager;

  public ActivitiMethodExpression(MethodExpression methodExpression, ExpressionManager expressionManager) {
    this.methodExpression = methodExpression;
    this.expressionManager = expressionManager;
  }

  public Object invoke(ActivityContext activityContext) {
    return invoke(((ExecutionContextImpl)activityContext).getScopeInstance());
  }

  public Object invoke(ScopeInstanceImpl scopeInstance) {
    ELContext elContext = expressionManager.getElContext(scopeInstance);
    try {
      return methodExpression.invoke(elContext, null);
    } catch (MethodNotFoundException e) {
      throw new ActivitiException("Unknown method used in expression", e);
    }
  }
}
