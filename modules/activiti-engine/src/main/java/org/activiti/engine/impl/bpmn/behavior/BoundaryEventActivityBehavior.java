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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.process.ActivityImpl;


/**
 * @author Joram Barrez
 */
public class BoundaryEventActivityBehavior extends FlowNodeActivityBehavior {
  
  private static Logger log = Logger.getLogger(BoundaryEventActivityBehavior.class.getName());
  
  protected boolean interrupting;
  protected String activityId;
  
  public BoundaryEventActivityBehavior() {
    
  }
  
  public BoundaryEventActivityBehavior(boolean interrupting, String activityId) {
    this.interrupting = interrupting;
    this.activityId = activityId;
  }
  
  @SuppressWarnings("unchecked")
  public void execute(ActivityExecution execution) throws Exception {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    ActivityImpl boundaryActivity = executionEntity.getProcessDefinition().findActivity(activityId);
    ActivityImpl interruptedActivity = executionEntity.getActivity();
    
    executionEntity.setActivity(boundaryActivity);
    
    List<PvmTransition> outgoingTransitions = boundaryActivity.getOutgoingTransitions();
    List<ExecutionEntity> interruptedExecutions = null;
    
    if (interrupting) {
      if (executionEntity.getSubProcessInstance()!=null) {
        executionEntity.getSubProcessInstance().deleteCascade(executionEntity.getDeleteReason());
      }
      
      interruptedExecutions = new ArrayList<ExecutionEntity>(executionEntity.getExecutions());
      for (ExecutionEntity interruptedExecution: interruptedExecutions) {
        interruptedExecution.deleteCascade("interrupting boundary event '"+execution.getActivity().getId()+"' fired");
      }
      
      execution.takeAll(outgoingTransitions, (List) interruptedExecutions);
    }
    else {
      // non interrupting event, introduced with BPMN 2.0, we need to create a new execution in this case
      
      // create a new execution and move it out from the timer activity
      ExecutionEntity concurrentRoot = executionEntity.getParent().isConcurrent() ? executionEntity.getParent() : executionEntity;
      ExecutionEntity outgoingExecution = concurrentRoot.createExecution();
    
      outgoingExecution.setActive(true);
      outgoingExecution.setScope(false);
      outgoingExecution.setConcurrent(true);
      
      outgoingExecution.takeAll(outgoingTransitions, Collections.EMPTY_LIST);
      outgoingExecution.remove();
      // now we have to move the execution back to the real activity
      // since the execution stays there (non interrupting) and it was
      // set to the boundary event before
      executionEntity.setActivity(interruptedActivity);      
    }
  }

  public boolean isInterrupting() {
    return interrupting;
  }

  public void setInterrupting(boolean interrupting) {
    this.interrupting = interrupting;
  }

}
