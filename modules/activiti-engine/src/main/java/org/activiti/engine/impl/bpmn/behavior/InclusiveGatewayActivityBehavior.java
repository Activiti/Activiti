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

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of the Inclusive Gateway/OR gateway/inclusive data-based
 * gateway as defined in the BPMN specification.
 * 
 * Note: The default implementation of the "leave" method inside
 * GatewayActivitiBehavior is to check all conditions and take all outgoing
 * transitions wherein the condition evaluates to <code>true</code> (or
 * condition checking is disabled).
 * 
 * This suits the purpose of the Inclusive Gateway nicely ... therefore
 * implementation of this class simply extends from the parent class for now.
 * 
 * @author Tom Van Buskirk
 */
public class InclusiveGatewayActivityBehavior extends GatewayActivityBehavior {

  /**
   * Leave the BPMN 2.0 activity, ensuring that conditions are validated and
   * that if no path is found an ActivitiException will be thrown.
   */
  @Override
  protected void leave(ActivityExecution execution) {
    bpmnActivityBehavior.performOutgoingBehavior(execution, true, true);
  }
}
