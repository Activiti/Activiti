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
package org.activiti.impl.execution;

import java.util.List;

import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.pvm.Listener;


/**
 * @author Tom Baeyens
 */
public class ExeOpTransitionNotifyListenerStart implements ExeOp {

  public void execute(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    List<Listener> eventListeners = activity.getEventListeners(Listener.EVENTNAME_START);
    int eventListenerIndex = execution.getEventListenerIndex();
    
    if (eventListeners.size()>eventListenerIndex) {
      Listener listener = eventListeners.get(eventListenerIndex);
      listener.notify(execution);
      execution.setEventListenerIndex(eventListenerIndex+1);
      execution.performOperation(this);

    } else {
      execution.setEventListenerIndex(0);

      TransitionImpl transition = execution.getTransition();
      ActivityImpl destination = transition.getDestination();
      if (execution.getActivity()!=destination) {
        ActivityImpl nextScope = ExeOpTransitionNotifyListenerTake.findNextScope(activity, destination);
        execution.setActivity(nextScope);
        execution.performOperation(TRANSITION_CREATE_SCOPE);
      } else {
        execution.setTransition(null);
        execution.setActivity(destination);
        execution.performOperation(EXECUTE_CURRENT_ACTIVITY);
      }
    }
  }

  public boolean isAsync() {
    return false;
  }

  public String toString() {
    return "TransitionNotifyListenerStart";
  }
}
