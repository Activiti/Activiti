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
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmProcessInstanceEndTest extends PvmTestCase {

  public void testSimpleProcessInstanceEnd() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("wait")
      .endActivity()
      .createActivity("wait")
        .behavior(new WaitState())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    System.err.println(eventCollector);
    
    processInstance.deleteCascade("test");

    System.err.println();
    System.err.println(eventCollector);
  }
}
