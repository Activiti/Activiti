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

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;

/**
 * A receive task is a wait state that waits for the receival of some message.
 * 
 * Currently, the only message that is supported is the external trigger, given by calling the {@link RuntimeService#signal(String)} operation.
 * 

 */
public class ReceiveTaskActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  public void execute(DelegateExecution execution) {
    // Do nothing: waitstate behavior
  }

  public void trigger(DelegateExecution execution, String signalName, Object data) {
    leave(execution);
  }

}
