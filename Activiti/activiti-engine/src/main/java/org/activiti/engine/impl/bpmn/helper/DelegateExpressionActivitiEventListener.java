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
package org.activiti.engine.impl.bpmn.helper;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.el.NoExecutionVariableScope;

/**
 * An {@link ActivitiEventListener} implementation which resolves an expression to a delegate {@link ActivitiEventListener} instance and uses this for event notification. <br>
 * <br>
 * In case an entityClass was passed in the constructor, only events that are {@link ActivitiEntityEvent}'s that target an entity of the given type, are dispatched to the delegate.
 * 

 */
public class DelegateExpressionActivitiEventListener extends BaseDelegateEventListener {

  protected Expression expression;
  protected boolean failOnException = false;

  public DelegateExpressionActivitiEventListener(Expression expression, Class<?> entityClass) {
    this.expression = expression;
    setEntityClass(entityClass);
  }

  @Override
  public void onEvent(ActivitiEvent event) {
    if (isValidEvent(event)) {
      Object delegate = DelegateExpressionUtil.resolveDelegateExpression(expression, new NoExecutionVariableScope());
      if (delegate instanceof ActivitiEventListener) {
        // Cache result of isFailOnException() from delegate-instance
        // until next event is received. This prevents us from having to resolve
        // the expression twice when an error occurs.
        failOnException = ((ActivitiEventListener) delegate).isFailOnException();

        // Call the delegate
        ((ActivitiEventListener) delegate).onEvent(event);
      } else {

        // Force failing, since the exception we're about to throw
        // cannot be ignored, because it did not originate from the listener itself
        failOnException = true;
        throw new ActivitiIllegalArgumentException("Delegate expression " + expression + " did not resolve to an implementation of " + ActivitiEventListener.class.getName());
      }
    }
  }

  @Override
  public boolean isFailOnException() {
    return failOnException;
  }

}
