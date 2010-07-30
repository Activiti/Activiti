package org.activiti.pvm.test;
import junit.framework.TestCase;

import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmActivityInstance;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.WaitState;


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

/**
 * @author Tom Baeyens
 */
public class PvmTest extends TestCase {

  public void testBasicLinearActivities() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("one")
      .endActivity()
      .createActivity("one")
        .behavior(new WaitState())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    PvmActivityInstance activityInstance = processInstance.findActivityInstance("one");
    assertNotNull(activityInstance);
    
    activityInstance.signal(null, null);

    activityInstance = processInstance.findActivityInstance("three");
    assertNotNull(activityInstance);
  }
}
