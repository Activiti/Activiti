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
import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityContext;

/**
 * @author dsyer
 */
public class ServiceInvocationActivityBehaviour implements ActivityBehavior {

  private final ActivitiValueExpression expression;

  public ServiceInvocationActivityBehaviour(ActivitiValueExpression expression) {
    this.expression = expression;
  }

  public void start(ActivityContext activityContext) throws Exception {

    // FIXME: downcast
    Object object = expression.getValue(activityContext);

    if (object instanceof String) {
      String className = (String) object;
      if (className != null) {
        object = ReflectUtil.instantiate(className);
      }
    }

    if (object instanceof ActivityBehavior) {
      ((ActivityBehavior) object).start(activityContext);
    } else {
      throw new ActivitiException("Service " + object + " is used in a serviceTask, but does not" + " implement the "
              + ActivityBehavior.class.getCanonicalName() + " interface");
    }

  }

}
