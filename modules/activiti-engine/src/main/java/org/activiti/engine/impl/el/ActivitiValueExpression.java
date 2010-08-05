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
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.runtime.ExecutionEntity;
import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ActivitiValueExpression {

  ValueExpression valueExpression;
  ExpressionManager expressionManager;

  public ActivitiValueExpression(ValueExpression valueExpression, ExpressionManager expressionManager) {
    this.valueExpression = valueExpression;
    this.expressionManager = expressionManager;
  }

  public Object getValue(ExecutionImpl execution) {
    ELContext elContext = expressionManager.getElContext((ExecutionEntity) execution);
    try {
      return valueExpression.getValue(elContext);
    } catch (PropertyNotFoundException e) {
      throw new ActivitiException("Unknown property used in expression", e);
    }
  }

  public void setValue(Object value, ExecutionImpl execution) {
    ELContext elContext = expressionManager.getElContext((ExecutionEntity) execution);
    valueExpression.setValue(elContext, value);
  }
}
