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

import java.util.HashMap;
import java.util.Map;

import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmExecution;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.End;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmScopesAndVariablesTest extends PvmTestCase {

  /**
   * +--------------------------------------------------------------------------------+
   * | mostOuterNestedActivity                                                        |                  
   * | +----------------------------------------------------------------------------+ |
   * | | outerScope (scope)                                                         | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | | | firstInnerScope (scope)          | | secondInnerScope (scope)          | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | | | firstMostInnerNestedActivity | | | | secondMostInnerNestedActivity | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | | | start |-->| waitInFirst |--------->| waitInSecond |--> | end |   | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | +----------------------------------------------------------------------------+ |
   * +--------------------------------------------------------------------------------+
   * 
   */
  public void testStartEndWithScopesAndNestedActivities() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and events")
      .variable("processVar1", "one")
      .variable("processVar2", "two")
      .createActivity("mostOuterNestedActivity")
        .createActivity("outerScope")
          .variable("outerVar", "outerValue")
          .createActivity("firstInnerScope")
            .variable("firstInnerVar", "firstInnerValue")
            .createActivity("firstMostInnerNestedActivity")
              .createActivity("start")
                .initial()
                .behavior(new Automatic())
                .transition("waitInFirst")
              .endActivity()
              .createActivity("waitInFirst")
                .behavior(new WaitState())
                .transition("waitInSecond")
              .endActivity()
            .endActivity()
          .endActivity()
          .createActivity("secondInnerScope")
            .variable("secondInnerVar", "secondInnerValue")
            .createActivity("secondMostInnerNestedActivity")
              .createActivity("waitInSecond")
                .behavior(new WaitState())
                .transition("end")
              .endActivity()
              .createActivity("end")
                .behavior(new End())
              .endActivity()
            .endActivity()
          .endActivity()
        .endActivity()
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    PvmExecution execution = processInstance.findExecution("waitInFirst");
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("processVar1", "one");
    expectedVariables.put("processVar2", "two");
    expectedVariables.put("outerVar", "outerValue");
    expectedVariables.put("firstInnerVar", "firstInnerValue");
    assertEquals(expectedVariables, execution.getVariables());
    
    execution.signal(null, null);
    
    execution = processInstance.findExecution("waitInSecond");
    
    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("processVar1", "one");
    expectedVariables.put("processVar2", "two");
    expectedVariables.put("outerVar", "outerValue");
    expectedVariables.put("secondInnerVar", "secondInnerValue");
  }
}
