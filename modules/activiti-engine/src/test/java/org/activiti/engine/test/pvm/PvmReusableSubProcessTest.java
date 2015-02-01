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

package org.activiti.engine.test.pvm;

import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.test.pvm.activities.Automatic;
import org.activiti.engine.test.pvm.activities.End;
import org.activiti.engine.test.pvm.activities.ReusableSubProcess;


/**
 * @author Tom Baeyens
 */
public class PvmReusableSubProcessTest extends PvmTestCase {

  public void testReusableSubProcess() {
    PvmProcessDefinition subProcessDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("subEnd")
      .endActivity()
      .createActivity("subEnd")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
  
    PvmProcessDefinition superProcessDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("subprocess")
      .endActivity()
      .createActivity("subprocess")
        .behavior(new ReusableSubProcess(subProcessDefinition))
        .transition("superEnd")
      .endActivity()
      .createActivity("superEnd")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
  
    PvmProcessInstance processInstance = superProcessDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }
}
