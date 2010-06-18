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
package org.activiti.impl.variable;

import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.pvm.Listener;
import org.activiti.pvm.ListenerExecution;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class VariableInitializeWithExpression implements Listener {
  
  protected String destinationVariableName;
  protected String sourceValueExpression;
  protected String sourceValueExpressionLanguage;
  
  public VariableInitializeWithExpression(String destinationVariableName, 
          String sourceValueExpression, String sourceValueExpressionLanguage) {
    this.destinationVariableName = destinationVariableName;
    this.sourceValueExpression = sourceValueExpression;
    this.sourceValueExpressionLanguage = sourceValueExpressionLanguage;
  }

  public void notify(ListenerExecution execution) {
    ExecutionImpl innerScope = (ExecutionImpl) execution;
    ExecutionImpl outerScope = innerScope.getParent();
    
    ScriptingEngines scriptingEngines = ScriptingEngines.getScriptingEngines();
    Object value = scriptingEngines.evaluate(sourceValueExpression, sourceValueExpressionLanguage, outerScope);
    innerScope.setVariable(destinationVariableName, value);
  }
}
