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

package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


/**
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class CancelEndEventActivityBehavior extends FlowNodeActivityBehavior {
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {
    
    // find cancel boundary event:
    ActivityImpl cancelBoundaryEvent = ScopeUtil
      .findInParentScopesByBehaviorType((ActivityImpl) execution.getActivity(), CancelBoundaryEventActivityBehavior.class);
    
    if(cancelBoundaryEvent == null) {
      throw new ActivitiException("Could not find cancel boundary event for cancel end event "+execution.getActivity());
    }
    
    ActivityExecution scopeExecution = ScopeUtil.findScopeExecutionForScope((ExecutionEntity)execution, cancelBoundaryEvent.getParentActivity());    
    
    // end all executions and process instances in the scope of the transaction
    scopeExecution.destroyScope("cancel end event fired");
    
    // the scope execution executes the boundary event
    InterpretableExecution outgoingExecution = (InterpretableExecution)scopeExecution;
    outgoingExecution.setActivity(cancelBoundaryEvent);
    outgoingExecution.setActive(true);
    
    // execute the boundary
    cancelBoundaryEvent
      .getActivityBehavior()
      .execute(outgoingExecution);    
  }

}
