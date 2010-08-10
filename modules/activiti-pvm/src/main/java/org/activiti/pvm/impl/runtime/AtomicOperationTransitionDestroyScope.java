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

import java.util.logging.Logger;

import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ScopeImpl;
import org.activiti.pvm.impl.process.TransitionImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationTransitionDestroyScope implements AtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationTransitionDestroyScope.class.getName());

  public void execute(ExecutionImpl execution) {
    ExecutionImpl propagatingExecution = null;

    ActivityImpl activity = execution.getActivity();
    // if this transition is crossing a scope boundary
    if (activity.isScope()) {
      
      ExecutionImpl parentScopeInstance = null;
      // if this is a concurrent execution crossing a scope boundary
      if (execution.isConcurrent() && !execution.isScope()) {
        // first remove the execution from the current root
        ExecutionImpl concurrentRoot = execution.getParent();
        parentScopeInstance = execution.getParent().getParent();

        log.fine("moving concurrent "+execution+" one scope up under "+parentScopeInstance);
        concurrentRoot.getExecutions().remove(execution);
        parentScopeInstance.getExecutions().add(execution);
        execution.setParent(parentScopeInstance);
        execution.setActivity(activity);
        propagatingExecution = execution;
        
        // if there is only a single concurrent execution left
        // in the concurrent root, auto-prune it.  meaning, the 
        // last concurrent child execution data should be cloned into
        // the concurrent root.   
        if (concurrentRoot.getExecutions().size()==1) {
          ExecutionImpl lastConcurrent = concurrentRoot.getExecutions().get(0);
          log.fine("replacing concurrent root "+concurrentRoot+" with last concurrent "+lastConcurrent);
          
          // We can't just merge the data of the lastConcurrent into the concurrentRoot.
          // This is because the concurrent root might be in a takeAll-loop.  So the 
          // concurrent execution is the one that will be receiveing the take
          parentScopeInstance.getExecutions().remove(concurrentRoot);
          parentScopeInstance.getExecutions().add(lastConcurrent);
          lastConcurrent.setParent(parentScopeInstance);
          lastConcurrent.setActive(true);
          lastConcurrent.setScope(true);
          
          // TODO extract common, overridable destroy method
        }

      } else if (execution.isConcurrent() && execution.isScope()) {
        log.fine("scoped concurrent "+execution+" becomes concurrent and remains under "+execution.getParent());
        execution.setScope(false);
        propagatingExecution = execution;
        
      } else {
        propagatingExecution = execution.destroyScope();
      }
      
    } else {
      propagatingExecution = execution;
    }
    
    // if there is another scope element that is ended
    ScopeImpl nextOuterScopeElement = activity.getParent();
    TransitionImpl transition = execution.getTransition();
    ActivityImpl destination = transition.getDestination();
    if (transitionLeavesNextOuterScope(nextOuterScopeElement, destination)) {
      propagatingExecution.setActivity((ActivityImpl) nextOuterScopeElement);
      propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_END);
    } else {
      propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_TAKE);
    }
  }

  protected boolean transitionLeavesNextOuterScope(ScopeImpl nextScopeElement, ActivityImpl destination) {
    return !nextScopeElement.contains(destination);
  }

  public boolean isAsync() {
    return false;
  }
  
  public String toString() {
    return "TransitionDestroyScope";
  }
}
