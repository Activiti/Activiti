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
package org.activiti.standalone.rules;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.BusinessRuleTaskDelegate;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class CustomBusinessRuleTask extends AbstractBpmnActivityBehavior implements BusinessRuleTaskDelegate {
  
  private static final long serialVersionUID = 1L;
  
  public static List<Expression> ruleInputVariables = new ArrayList<Expression>();
  public static List<Expression> ruleIds = new ArrayList<Expression>();
  public static Boolean exclude;
  public static String resultVariableName;

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    execution.setVariable("test", "test2");
    leave(execution);
  }

  @Override
  public void addRuleVariableInputIdExpression(Expression inputId) {
    ruleInputVariables.add(inputId);
  }

  @Override
  public void addRuleIdExpression(Expression inputId) {
    ruleIds.add(inputId);
  }

  @Override
  public void setExclude(boolean exclude) {
    CustomBusinessRuleTask.exclude = exclude;
  }

  @Override
  public void setResultVariable(String resultVariableName) {
    CustomBusinessRuleTask.resultVariableName = resultVariableName;
  }

}
