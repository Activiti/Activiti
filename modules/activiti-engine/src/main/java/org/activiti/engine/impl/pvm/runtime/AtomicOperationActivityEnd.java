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

import org.activiti.engine.delegate.ActivityBehavior;
import org.activiti.engine.delegate.CompositeActivityBehavior;
import org.activiti.engine.delegate.EventListener;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationActivityEnd extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(ExecutionImpl execution) {
    return execution.getActivity();
  }

  @Override
  protected String getEventName() {
    return EventListener.EVENTNAME_END;
  }

  @Override
  protected void eventNotificationsCompleted(ExecutionImpl execution) {
    ActivityImpl activity = execution.getActivity();
    ActivityImpl parentActivity = activity.getParentActivity();

    // if the execution is a single path of execution inside the process definition scope
    if ( (parentActivity!=null)
         &&(!parentActivity.isScope())
          ) {
      execution.setActivity(parentActivity);
      execution.performOperation(ACTIVITY_END);
      
    } else if (execution.isProcessInstance()) {
      execution.performOperation(PROCESS_END);
    
    } else if (execution.isScope()) {

      ActivityBehavior parentActivityBehavior = (parentActivity!=null ? parentActivity.getActivityBehavior() : null);
      if (parentActivityBehavior instanceof CompositeActivityBehavior) {
        CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) parentActivity.getActivityBehavior();
        execution.setActivity(parentActivity);
        compositeActivityBehavior.lastExecutionEnded(execution);
      } else {
        // default destroy scope behavior
        ExecutionImpl parentScopeExecution = execution.getParent();
        execution.destroy();
        execution.remove();
        parentScopeExecution.setActivity(parentActivity);
        parentScopeExecution.performOperation(ACTIVITY_END);
        // TODO prune if necessary
      }

    } else { // execution.isConcurrent() && !execution.isScope()
      
      execution.remove();

      // prune if necessary
      ExecutionImpl concurrentRoot = execution.getParent();
      if (concurrentRoot.getExecutions().size()==1) {
        ExecutionImpl lastConcurrent = concurrentRoot.getExecutions().get(0);
        if (!lastConcurrent.isScope()) {
          concurrentRoot.setActivity(lastConcurrent.getActivity());
          lastConcurrent.setReplacedBy(concurrentRoot);
          lastConcurrent.remove();
        } else {
          lastConcurrent.setConcurrent(false);
        }
      }
    }
  }

  protected boolean isExecutionAloneInParent(ExecutionImpl execution) {
    ScopeImpl parentScope = execution.getActivity().getParent();
    for (ExecutionImpl other: execution.getParent().getExecutions()) {
      if (other!=execution && parentScope.contains(other.getActivity())) {
        return false;
      }
    }
    return true;
  }
}
