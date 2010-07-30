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
package org.activiti.engine.impl.scripting;

import org.activiti.engine.ActivitiException;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Condition;

/**
 * @author Tom Baeyens
 */
public class ExpressionCondition implements Condition {

  private final String expression;
  private final String language;
  private final ScriptingEngines scriptingEngines;

  public ExpressionCondition(ScriptingEngines scriptingEngines, String expression, String language) {
    this.scriptingEngines = scriptingEngines;
    this.expression = expression;
    this.language = language;
  }

  public boolean evaluate(ActivityExecution execution) {
    Object result = scriptingEngines.evaluate(expression, language, (ExecutionImpl) execution);
    if (result == null) {
      throw new ActivitiException("condition expression returns null: " + expression);
    }
    if (!(result instanceof Boolean)) {
      throw new ActivitiException("condition expression returns non-Boolean: " + result + " (" + result.getClass().getName() + ")");
    }
    return (Boolean) result;
  }

}
