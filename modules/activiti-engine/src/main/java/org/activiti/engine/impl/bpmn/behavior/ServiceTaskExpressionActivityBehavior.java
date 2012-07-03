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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * ActivityBehavior that evaluates an expression when executed. Optionally, it sets the result 
 * of the expression as a variable on the execution.
 * 
 * @author Tom Baeyens
 * @author Christian Stettler
 * @author Frederik Heremans
 * @author Slawomir Wojtasiak (Patch for ACT-1159)
 * @author Falko Menge
 */
public class ServiceTaskExpressionActivityBehavior extends TaskActivityBehavior {

  protected Expression expression;
  protected String resultVariable;

  public ServiceTaskExpressionActivityBehavior(Expression expression, String resultVariable) {
    this.expression = expression;
    this.resultVariable = resultVariable;
  }

  public void execute(ActivityExecution execution) throws Exception {
	Object value = null;
	try {
		value = expression.getValue(execution);
		if (resultVariable != null) {
		    execution.setVariable(resultVariable, value);
		}
		leave(execution);
    } catch (Exception exc) {

      Throwable cause = exc;
      BpmnError error = null;
      while (cause != null) {
        if (cause instanceof BpmnError) {
          error = (BpmnError) cause;
          break;
        }
        cause = cause.getCause();
      }

      if (error != null) {
        ErrorPropagation.propagateError(error, execution);
      } else {
        throw exc;
      }
    }
  }
}
