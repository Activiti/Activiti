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

package org.activiti.examples.bpmn.servicetask;

import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityExecution;

/**
 * @author Joram Barrez
 */
public class ThrowsExceptionBehavior implements ActivityBehavior {

  public void execute(ActivityExecution execution) {
    String var = (String) execution.getVariable("var");

    
    SequenceFlow sequenceFlow = null;
    
    try {
      executeLogic(var);
      sequenceFlow = findSequenceFlow(execution, "no-exception");
    } catch (Exception e) {
      sequenceFlow = findSequenceFlow(execution, "exception");
    }
    
    execution.setCurrentFlowElement(sequenceFlow);
    Context.getCommandContext().getAgenda().planContinueProcessOperation(execution);
  }

  protected void executeLogic(String value) {
    if (value.equals("throw-exception")) {
      throw new RuntimeException();
    }
  }
  
  protected SequenceFlow findSequenceFlow(ActivityExecution execution, String sequenceFlowId) {
    FlowNode currentFlowNode = (FlowNode) execution.getCurrentFlowElement();
    for (SequenceFlow sequenceFlow : currentFlowNode.getOutgoingFlows()) {
      if (sequenceFlow.getId() != null && sequenceFlow.getId().equals(sequenceFlowId)) {
        return sequenceFlow;
      }
    }
    return null;
  }

}
