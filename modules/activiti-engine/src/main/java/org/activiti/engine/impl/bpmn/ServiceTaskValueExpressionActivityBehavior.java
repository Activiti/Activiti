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

import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityExecution;


/**
 * @author Tom Baeyens
 * @author Christian Stettler
 */
public class ServiceTaskValueExpressionActivityBehavior extends AbstractBpmnActivity implements ActivityBehavior {
  
  protected ActivitiValueExpression activitiValueExpression;
  protected String resultVariableName;

  public ServiceTaskValueExpressionActivityBehavior(ActivitiValueExpression activitiValueExpression, String resultVariableName) {
    this.activitiValueExpression = activitiValueExpression;
    this.resultVariableName = resultVariableName;
  }

  public void execute(ActivityExecution execution) throws Exception {
    Object value = activitiValueExpression.getValue(execution);

    if (resultVariableName != null) {
      execution.setVariable(resultVariableName, value);
    }

    leave(execution);
  }
}
