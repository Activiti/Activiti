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

import org.activiti.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

/**
 * @author Nico Rehwaldt
 */
public class TerminateEndEventActivityBehavior extends FlowNodeActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    
    PvmActivity terminateEndEventActivity = execution.getActivity();    
    ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);
    
    // destroy the scope
    scopeExecution.destroyScope("terminate end event fired");
    
    // set the scope execution to the terminate end event and make it end here.
    // (the history should reflect that the execution ended here and we want an 'end time' for the 
    // historic activity instance.)
    ((InterpretableExecution)scopeExecution).setActivity((ActivityImpl) terminateEndEventActivity);
    // end the scope execution
    scopeExecution.end();
  }
  
  
  // If we use this implementation, we run into trouble in the DbSqlSession
  
//  public void execute(ActivityExecution execution) throws Exception {
//    
//    PvmActivity terminateEndEventActivity = execution.getActivity();
//    
//    ActivityExecution scopeExecution = ScopeUtil.findScopeExecution(execution);
//    
//    // first end the current execution normally
//    execution.end();
//    
//    // if this does not end the scope execution, interrupt it and destroy it. 
//    if (!scopeExecution.isEnded()) {
//      // destroy the scope execution (this interrupts all child executions / sub process instances)
//      scopeExecution.destroyScope("terminate end event fired");
//    
//      // set the scope execution to the terminate end event make 
//      // (the history should reflect that the execution ended here).
//      ((InterpretableExecution)scopeExecution).setActivity((ActivityImpl) terminateEndEventActivity);
//      // end the scope execution
//      scopeExecution.end();
//    }
//  }
}
