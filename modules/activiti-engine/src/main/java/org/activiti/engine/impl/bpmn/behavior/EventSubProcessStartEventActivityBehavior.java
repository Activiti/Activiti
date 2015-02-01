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

import java.util.Collections;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;


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
  
  // default = true
  protected boolean isInterrupting = true;
  protected String activityId;
  
  public EventSubProcessStartEventActivityBehavior(String activityId) {
    this.activityId = activityId;
  }
  
  @Override
  public void execute(ActivityExecution execution) throws Exception {

    InterpretableExecution interpretableExecution = (InterpretableExecution) execution;
    ActivityImpl activity = interpretableExecution.getProcessDefinition().findActivity(activityId);
    
    ActivityExecution outgoingExecution = execution;
    
    if(isInterrupting) {
      execution.destroyScope("Event subprocess triggered using activity "+ activityId);
    } else{ 
      outgoingExecution = execution.createExecution();
      outgoingExecution.setActive(true);
      outgoingExecution.setScope(false);
      outgoingExecution.setConcurrent(true);
    }
    
    // set the outgoing execution to this activity
    ((InterpretableExecution)outgoingExecution).setActivity(activity);
    
    // continue execution
    outgoingExecution.takeAll(activity.getOutgoingTransitions(), Collections.EMPTY_LIST);
  }

  public void setInterrupting(boolean b) {
    isInterrupting = b;
  }
  
  public boolean isInterrupting() {
    return isInterrupting;
  }
  
}
