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

package org.activiti.pvm.test;

import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.End;
import org.activiti.test.pvm.activities.ParallelGateway;


/**
 * @author Tom Baeyens
 */
public class PvmParallelEndTest extends PvmTestCase {

  public void testParallelEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("end1")
        .transition("end2")
      .endActivity()
      .createActivity("end1")
        .behavior(new End())
      .endActivity()
      .createActivity("end2")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }
}
