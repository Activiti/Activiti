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
package org.activiti.pvm.impl.runtime;

import java.util.List;
import java.util.logging.Logger;

import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ScopeImpl;
import org.activiti.pvm.impl.process.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionNotifyListenerTake implements AtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationTransitionNotifyListenerTake.class.getName());

  public void execute(ExecutionImpl execution) {
    TransitionImpl transition = execution.getTransition();
    
    List<EventListener> eventListeners = transition.getEventListeners();
    int eventListenerIndex = execution.getEventListenerIndex();
    
    if (eventListeners.size()>eventListenerIndex) {
      execution.setEventName(EventListener.EVENTNAME_TAKE);
      execution.setEventSource(transition);
      EventListener listener = eventListeners.get(eventListenerIndex);
      listener.notify(execution);
      execution.setEventListenerIndex(eventListenerIndex+1);
      execution.performOperation(this);

    } else {
      log.fine(execution+" takes transition "+transition);
      execution.setEventListenerIndex(0);
      execution.setEventName(null);
      execution.setEventSource(null);

      ActivityImpl activity = execution.getActivity();
      ActivityImpl nextScope = findNextScope(activity.getParent(), transition.getDestination());
      execution.setActivity(nextScope);
      execution.performOperation(TRANSITION_CREATE_SCOPE);
    }
  }

  /** finds the next scope to enter.  the most outer scope is found first */
  public static ActivityImpl findNextScope(ScopeImpl outerScopeElement, ActivityImpl destination) {
    ActivityImpl nextScope = destination;
    while( (nextScope.getParent() instanceof ActivityImpl)
           && (nextScope.getParent() != outerScopeElement)
         ) {
      nextScope = (ActivityImpl) nextScope.getParent();
    }
    return nextScope;
  }
}
