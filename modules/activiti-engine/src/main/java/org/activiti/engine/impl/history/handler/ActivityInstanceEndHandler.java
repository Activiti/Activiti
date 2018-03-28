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

package org.activiti.engine.impl.history.handler;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


/**
 * @author Tom Baeyens
 */
public class ActivityInstanceEndHandler implements ExecutionListener {
    
    public void notify(DelegateExecution execution) {
        if (!isSourceTransitionNotExecutionActivityAndNonInterrupting((ExecutionEntity) execution)) {
            Context.getCommandContext().getHistoryManager()
                    .recordActivityEnd((ExecutionEntity) execution);
        }
    }
    
    private boolean isSourceTransitionNotExecutionActivityAndNonInterrupting(InterpretableExecution execution) {
        TransitionImpl transition = execution.getTransition();
        if (transition != null) {
            ActivityBehavior activityBehavior = transition.getSource().getActivityBehavior();
        
            return (!(execution.getActivity().getId().equals(execution.getTransition().getSource().getId())) &&
                    activityBehavior instanceof BoundaryEventActivityBehavior &&
                    !(((BoundaryEventActivityBehavior) activityBehavior).isInterrupting()));
        }
        return false;
    }
}

