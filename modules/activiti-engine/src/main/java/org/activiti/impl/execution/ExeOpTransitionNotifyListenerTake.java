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
import org.activiti.impl.definition.ScopeElementImpl;
import org.activiti.impl.definition.TransitionImpl;
import org.activiti.pvm.Listener;


/**
 * @author Tom Baeyens
 */
public class ExeOpTransitionNotifyListenerTake implements ExeOp {

  public void execute(ExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();
    List<Listener> eventListeners = transition.getListeners();
    int eventListenerIndex = execution.getEventListenerIndex();
    
    if (eventListeners.size()>eventListenerIndex) {
      Listener listener = eventListeners.get(eventListenerIndex);
      listener.notify(execution);
      execution.setEventListenerIndex(eventListenerIndex+1);
      execution.performOperation(this);

    } else {
      execution.setEventListenerIndex(0);
      ActivityImpl activity = execution.getActivity();
      ActivityImpl nextScope = findNextScope(activity, transition.getDestination());
      execution.setActivity(nextScope);
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    }
  }

  /** finds the next scope to enter.  the most outer scope is found first */
  public static ActivityImpl findNextScope(ScopeElementImpl outerScopeElement, ActivityImpl destination) {
    ActivityImpl nextScope = destination;
    while( (nextScope.getParent() instanceof ActivityImpl)
           && (nextScope.getParent() != outerScopeElement)
         ) {
      nextScope = (ActivityImpl) nextScope.getParent();
    }
    return nextScope;
  }

  public boolean isAsync() {
    return false;
  }

  public String toString() {
    return "TransitionNotifyListenerTake";
  }
}
