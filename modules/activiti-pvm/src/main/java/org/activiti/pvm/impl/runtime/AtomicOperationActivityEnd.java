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

import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.CompositeActivityBehavior;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.impl.process.ActivityImpl;
import org.activiti.pvm.impl.process.ScopeImpl;


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
    if (execution.isProcessInstance()) {
      if (parentActivity!=null) {
        if (parentActivity.isScope()) {
          ActivityBehavior parentActivityBehavior = parentActivity.getActivityBehavior();
          if (parentActivityBehavior instanceof CompositeActivityBehavior) {
            CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) parentActivityBehavior;
            compositeActivityBehavior.lastExecutionEnded(execution);
          }

        } else {
          execution.setActivity(parentActivity);
          execution.performOperation(ACTIVITY_END);
        }
        
      } else {
        execution.performOperation(PROCESS_END);
      }
      
    } else if (!execution.isConcurrent()) {

      if (parentActivity.isScope()) {
        ActivityBehavior parentActivityBehavior = parentActivity.getActivityBehavior();
        if (parentActivityBehavior instanceof CompositeActivityBehavior) {
          execution.setActivity(parentActivity);
          CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) parentActivityBehavior;
          compositeActivityBehavior.lastExecutionEnded(execution);

        } else {
          // default destroy scope behavior
          ExecutionImpl parentScopeExecution = execution.getParent();
          execution.destroy();
          execution.remove();
          parentScopeExecution.setActivity(parentActivity);
          parentScopeExecution.performOperation(ACTIVITY_END);
        }
        
      } else {
        execution.setActivity(parentActivity);
        execution.performOperation(ACTIVITY_END);
        
      }
            
    } else if (execution.isScope() && !activity.isScope()) {
      execution.setActivity(parentActivity);
      execution.performOperation(ACTIVITY_END);
      
    } else {
      if (execution.isScope()) {
        execution.destroy();
      }
      
      
      if ( parentActivity!=null 
           && !parentActivity.isScope()
           && (isExecutionAloneInParent(execution))
         ) {
        execution.setActivity(parentActivity);
        execution.performOperation(ACTIVITY_END);
        
      } else {
        execution.remove();

        ExecutionImpl concurrentRoot = execution.getParent();
        // if there is now only 1 concurrent execution left
        if (concurrentRoot.getExecutions().size()==1) {
          ExecutionImpl lastConcurrent = concurrentRoot.getExecutions().get(0);
          concurrentRoot.setActivity(lastConcurrent.getActivity());
          lastConcurrent.setReplacedBy(concurrentRoot);
          lastConcurrent.remove();
        
        } else if (concurrentRoot.getExecutions().isEmpty()) {
          ActivityBehavior parentActivityBehavior = parentActivity.getActivityBehavior();
          if (parentActivityBehavior instanceof CompositeActivityBehavior) {
            CompositeActivityBehavior compositeActivityBehavior = (CompositeActivityBehavior) parentActivityBehavior;
            compositeActivityBehavior.lastExecutionEnded(execution);
          }
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
