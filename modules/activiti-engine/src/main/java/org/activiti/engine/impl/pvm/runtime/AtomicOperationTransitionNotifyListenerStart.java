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
package org.activiti.engine.impl.pvm.runtime;

import org.activiti.engine.delegate.EventListener;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionNotifyListenerStart extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(ExecutionImpl execution) {
    return execution.getActivity();
  }

  @Override
  protected String getEventName() {
    return EventListener.EVENTNAME_START;
  }

  @Override
  protected void eventNotificationsCompleted(ExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();
    ActivityImpl destination = transition.getDestination();
    ActivityImpl activity = execution.getActivity();
    if (activity!=destination) {
      ActivityImpl nextScope = AtomicOperationTransitionNotifyListenerTake.findNextScope(activity, destination);
      execution.setActivity(nextScope);
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    } else {
      execution.setTransition(null);
      execution.setActivity(destination);
      execution.performOperation(ACTIVITY_EXECUTE);
    }
  }
}
