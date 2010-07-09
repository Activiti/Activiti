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
package org.activiti.impl.bpmn;

import java.util.logging.Logger;

import org.activiti.BpmnActivityBehavior;
import org.activiti.pvm.ActivityExecution;
import org.activiti.pvm.EventActivityBehavior;


/**
 * superclass for all 'connectable' BPMN 2.0 process elements.
 * This means that any subclass can be the source or target of a sequenceflow.
 * 
 * Corresponds with the notion of the 'flownode' in BPMN 2.0.
 * 
 * @author Joram Barrez
 */
public abstract class BpmnActivity implements EventActivityBehavior {
  
  private static final Logger LOG = Logger.getLogger(BpmnActivity.class.getName());
  
  protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();
  
  /**
   * Default behaviour: just leave the activity with no extra functionality.
   */
  public void execute(ActivityExecution execution) throws Exception {
    leave(execution);
  }
  
  /**
   * Default way of leaving a BPMN 2.0 activity: evaluate the conditions on the
   * outgoing sequence flow and take those that evaluate to true.
   */
  protected void leave(ActivityExecution execution) {
    bpmnActivityBehavior.performDefaultOutgoingBehavior(execution);
  }
  
  protected void leaveIgnoreConditions(ActivityExecution execution) {
    bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior(execution);
  }

  public void event(ActivityExecution execution, Object event) throws Exception {
  }

}
