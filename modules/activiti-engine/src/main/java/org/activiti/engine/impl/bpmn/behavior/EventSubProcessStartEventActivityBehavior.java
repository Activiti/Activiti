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
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;


/**
 * Specialization of the Start Event for Event Sub-Processes.
 * 
 * Assumes that we enter with the "right" execution, 
 * which is the top-most execution for the current scope
 * 
 * @author Daniel Meyer
 * @author Falko Menge
 */
public class EventSubProcessStartEventActivityBehavior extends NoneStartEventActivityBehavior {
  
  // TODO: non-interrupting not yet supported
  protected boolean isInterrupting = true;
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {
    
    ActivityExecution outgoingExecution = execution;
    
    if(isInterrupting) {
      ScopeUtil.destroyScope(execution, "interrupting event subprocess started");
    } else{ 
      outgoingExecution = execution.createExecution();
      outgoingExecution.setActive(true);
      outgoingExecution.setScope(false);
      outgoingExecution.setConcurrent(true);
    }
    
    super.execute(outgoingExecution);
  }
  
}
