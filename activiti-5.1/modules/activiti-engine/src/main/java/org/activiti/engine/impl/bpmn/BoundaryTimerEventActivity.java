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
package org.activiti.engine.impl.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * implementation of the boundary timer event logic.
 * 
 * @author Joram Barrez
 */
public class BoundaryTimerEventActivity extends AbstractBpmnActivity {
  
  protected boolean interrupting;
    
  @SuppressWarnings("unchecked")
  public void execute(ActivityExecution execution) throws Exception {
    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    List<ExecutionEntity> interruptedExecutions = null;
    
    if (interrupting) {
      ExecutionEntity executionImpl = (ExecutionEntity) execution;
      if (executionImpl.getSubProcessInstance()!=null) {
        executionImpl.getSubProcessInstance().deleteCascade(executionImpl.getDeleteReason());
      }
      
      interruptedExecutions = new ArrayList<ExecutionEntity>(executionImpl.getExecutions());
      for (ExecutionEntity interruptedExecution: interruptedExecutions) {
        interruptedExecution.deleteCascade("interrupting timer event '"+execution.getActivity().getId()+"' fired");
      }
    }

    execution.takeAll(outgoingTransitions, (List) interruptedExecutions);
  }

  public boolean isInterrupting() {
    return interrupting;
  }

  public void setInterrupting(boolean interrupting) {
    this.interrupting = interrupting;
  }
  
}
