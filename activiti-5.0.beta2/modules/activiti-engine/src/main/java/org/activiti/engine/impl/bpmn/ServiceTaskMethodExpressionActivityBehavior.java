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

import org.activiti.engine.impl.el.ActivitiMethodExpression;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityExecution;


/**
 * @author Tom Baeyens
 */
public class ServiceTaskMethodExpressionActivityBehavior extends AbstractBpmnActivity implements ActivityBehavior {

  protected ActivitiMethodExpression methodExpression;

  public ServiceTaskMethodExpressionActivityBehavior(ActivitiMethodExpression methodExpression) {
    this.methodExpression = methodExpression;
  }

  public void execute(ActivityExecution execution) throws Exception {
    methodExpression.invoke(execution);
    leave(execution);
  }
}
