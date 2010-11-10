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

package org.activiti.engine.impl.bpmn;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.task.TaskListener;


/**
 * @author Joram Barrez
 */
public class ExpressionTaskListener implements TaskListener {
  
  protected Expression expression;
  
  public ExpressionTaskListener(Expression expression) {
    this.expression = expression;
  }
  
  public void notify(DelegateTask delegateTask) {
    ExecutionEntity execution = ((TaskEntity) delegateTask).getExecution();
    if (execution != null) {
      expression.getValue(execution);
    } else {
      throw new ActivitiException("Expressions are not usable outside a execution context");
    }
  }

}
