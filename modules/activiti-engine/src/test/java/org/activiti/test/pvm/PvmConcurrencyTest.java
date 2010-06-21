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
package org.activiti.test.pvm;

import java.util.ArrayList;
import java.util.List;

import org.activiti.pvm.ObjectExecution;
import org.activiti.pvm.ObjectProcessDefinition;
import org.activiti.pvm.ObjectProcessInstance;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.test.LogTestCase;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.Fork;
import org.activiti.test.pvm.activities.Join;
import org.activiti.test.pvm.activities.WaitState;



/**
 * @author Tom Baeyens
 */
public class PvmConcurrencyTest extends LogTestCase {

  public void testSimpleAutmaticConcurrency() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new Fork())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new Join())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  public void testSimpleWaitStateConcurrency() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new Fork())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new Join())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    ObjectExecution executionC1 = processInstance.findExecution("c1");
    assertNotNull(executionC1);
    
    ObjectExecution executionC2 = processInstance.findExecution("c2");
    assertNotNull(executionC2);
    
    executionC1.event(null);
    executionC2.event(null);
    
    List<String> activityNames = processInstance.getActivityNames();
    List<String> expectedActivityNames = new ArrayList<String>();
    expectedActivityNames.add("end");
    
    assertEquals(expectedActivityNames, activityNames);
  }
}
