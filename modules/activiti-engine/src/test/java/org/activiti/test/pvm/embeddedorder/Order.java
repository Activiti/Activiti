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
package org.activiti.test.pvm.embeddedorder;

import org.activiti.pvm.ObjectProcessDefinition;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class Order {
  
  static ObjectProcessDefinition orderProcess = ProcessDefinitionBuilder.createProcessDefinition()
    .createActivity("start")
      .initial()
      .behavior(new Automatic())
      .transition("verification")
    .endActivity()
    .createActivity("verification")
      .behavior(new WaitState())
      .transition("inProcess", "verificationComplete")
    .endActivity()
    .createActivity("inProcess")
      .behavior(new WaitState())
      .transition("archived", "deliveryAcknowledged")
    .endActivity()
    .createActivity("archived")
      .behavior(new WaitState())
    .endActivity()
  .endProcessDefinition();

  String state = null;
  StateManager stateManager = null;
  
  // constructor for persistence
  protected Order() {
    stateManager = new StateManager(this, "state", orderProcess);
  }

  // real constructor
  public Order(String someValueToConstructAnOrder) {
    stateManager = new StateManager(this, "state", orderProcess, true);
  }

  public void verificationComplete() {
    stateManager.event("verificationComplete");
  }

  public void deliveryAcknowledged() {
    stateManager.event("deliveryAcknowledged");
  }
  
  public String getState() {
    return state;
  }
}
