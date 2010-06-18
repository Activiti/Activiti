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
package org.activiti.impl.scripting;

import org.activiti.ActivitiException;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.Condition;


/**
 * @author Tom Baeyens
 */
public class ExpressionCondition implements Condition {

  String expression;
  String language = ScriptingEngines.DEFAULT_EXPRESSION_LANGUAGE;

  public ExpressionCondition(String expression) {
    this.expression = expression;
  }

  public ExpressionCondition(String expression, String language) {
    this.expression = expression;
    this.language = language;
  }

  public boolean evaluate(ActivityExecution execution) {
    ScriptingEngines scriptingEngines = ScriptingEngines.getScriptingEngines();
    Object result = scriptingEngines.evaluate(expression, language, (ExecutionImpl) execution);
    if (result==null) {
      throw new ActivitiException("condition expression returns null: "+expression);
    }
    if (! (result instanceof Boolean)) {
      throw new ActivitiException("condition expression returns non-Boolean: "+result+" ("+result.getClass().getName()+")");
    }
    return (Boolean) result;
  }

}
