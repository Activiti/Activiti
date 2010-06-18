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

import org.activiti.impl.definition.ActivityImpl;
import org.activiti.impl.definition.ScopeElementImpl;
import org.activiti.impl.definition.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class ExeOpTransitionDestroyScope implements ExeOp {

  public void execute(ExecutionImpl execution) {
    ExecutionImpl propagatingExecution = null;

    ActivityImpl activity = execution.getActivity();
    if (activity.isScope()) {
      ExecutionImpl parentScopeInstance = null;
      if (execution.isConcurrencyScope()) {
        parentScopeInstance = execution.getParent();
      } else {
        parentScopeInstance = execution.getParent().getParent();
      }
      
      if (parentScopeInstance.isActive()) {
        throw new UnsupportedOperationException("not implemented yet");
        
      } else {
        propagatingExecution = parentScopeInstance;
      }

      execution.destroyScope();
      
    } else {
      propagatingExecution = execution;
    }
    
    // if there is another scope element that is ended
    ScopeElementImpl nextOuterScopeElement = activity.getParent();
    TransitionImpl transition = execution.getTransition();
    ActivityImpl destination = transition.getDestination();
    if (transitionLeavesNextOuterScope(nextOuterScopeElement, destination)) {
      propagatingExecution.setActivity((ActivityImpl) nextOuterScopeElement);
      propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_END);
    } else {
      propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_TAKE);
    }
  }

  protected boolean transitionLeavesNextOuterScope(ScopeElementImpl nextScopeElement, ActivityImpl destination) {
    return !nextScopeElement.contains(destination);
  }

  public boolean isAsync() {
    return false;
  }
  
  public String toString() {
    return "TransitionDestroyScope";
  }
}
