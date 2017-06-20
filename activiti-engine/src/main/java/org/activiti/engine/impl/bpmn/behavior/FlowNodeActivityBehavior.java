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

import org.activiti.bpmn.model.FlowNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.TriggerableActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * Superclass for all 'connectable' BPMN 2.0 process elements: tasks, gateways and events. This means that any subclass can be the source or target of a sequenceflow.
 * 
 * Corresponds with the notion of the 'flownode' in BPMN 2.0.
 * 

 */
public abstract class FlowNodeActivityBehavior implements TriggerableActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();

  /**
   * Default behaviour: just leave the activity with no extra functionality.
   */
  public void execute(DelegateExecution execution) {
    leave(execution);
  }

  /**
   * Default way of leaving a BPMN 2.0 activity: evaluate the conditions on the outgoing sequence flow and take those that evaluate to true.
   */
  public void leave(DelegateExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior((ExecutionEntity) execution);
  }

  public void leaveIgnoreConditions(DelegateExecution execution) {
    bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior((ExecutionEntity) execution);
  }

  public void trigger(DelegateExecution execution, String signalName, Object signalData) {
    // concrete activity behaviours that do accept signals should override this method;
    throw new ActivitiException("this activity isn't waiting for a trigger");
  }
  
  protected String parseActivityType(FlowNode flowNode) {
    String elementType = flowNode.getClass().getSimpleName();
    elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
    return elementType;
  }

}
