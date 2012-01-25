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

import java.util.List;

import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.CompositeActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationActivityEnd extends AbstractEventAtomicOperation {

  @Override
  protected ScopeImpl getScope(InterpretableExecution execution) {
    return (ScopeImpl) execution.getActivity();
  }

  @Override
  protected String getEventName() {
    return org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void eventNotificationsCompleted(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
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
        
        if(activity.isScope() && activity.getOutgoingTransitions().isEmpty()) { 
          // there is no transition destroying the scope
          InterpretableExecution parentScopeExecution = (InterpretableExecution) execution.getParent();
          execution.destroy();
          execution.remove();
          parentScopeExecution.setActivity(parentActivity);
          compositeActivityBehavior.lastExecutionEnded(parentScopeExecution);          
        } else {        
          execution.setActivity(parentActivity);
          compositeActivityBehavior.lastExecutionEnded(execution);
        }
      } else {
        // default destroy scope behavior
        InterpretableExecution parentScopeExecution = (InterpretableExecution) execution.getParent();
        execution.destroy();
        execution.remove();
        parentScopeExecution.setActivity(parentActivity);
        parentScopeExecution.performOperation(ACTIVITY_END);
      }

    } else { // execution.isConcurrent() && !execution.isScope()
      
      execution.remove();

      // prune if necessary
      InterpretableExecution concurrentRoot = (InterpretableExecution) execution.getParent();
      if (concurrentRoot.getExecutions().size()==1) {
        InterpretableExecution lastConcurrent = (InterpretableExecution) concurrentRoot.getExecutions().get(0);
        if (!lastConcurrent.isScope()) {
          concurrentRoot.setActivity((ActivityImpl) lastConcurrent.getActivity());
          lastConcurrent.setReplacedBy(concurrentRoot);
          
          // Move children of lastConcurrent one level up
          if (lastConcurrent.getExecutions().size() > 0) {
            concurrentRoot.getExecutions().clear();
            for (ActivityExecution childExecution : lastConcurrent.getExecutions()) {
              InterpretableExecution childInterpretableExecution = (InterpretableExecution) childExecution;
              ((List)concurrentRoot.getExecutions()).add(childExecution); // casting ... damn generics
              childInterpretableExecution.setParent(concurrentRoot);
            }
            lastConcurrent.getExecutions().clear();
          }
          
          // Copy execution-local variables of lastConcurrent
          concurrentRoot.setVariablesLocal(lastConcurrent.getVariablesLocal());
          
          lastConcurrent.remove();
        } else {
          lastConcurrent.setConcurrent(false);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean isExecutionAloneInParent(InterpretableExecution execution) {
    ScopeImpl parentScope = (ScopeImpl) execution.getActivity().getParent();
    for (InterpretableExecution other: (List<InterpretableExecution>) execution.getParent().getExecutions()) {
      if (other!=execution && parentScope.contains((ActivityImpl) other.getActivity())) {
        return false;
      }
    }
    return true;
  }
}
