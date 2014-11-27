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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmVariablesTest extends PvmTestCase {

  public void testVariables() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
      
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("amount", 500L);
    processInstance.setVariable("msg", "hello world");
    processInstance.start();

    assertEquals(500L, processInstance.getVariable("amount"));
    assertEquals("hello world", processInstance.getVariable("msg"));

    PvmExecution activityInstance = processInstance.findExecution("a");
    assertEquals(500L, activityInstance.getVariable("amount"));
    assertEquals("hello world", activityInstance.getVariable("msg"));
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("amount", 500L);
    expectedVariables.put("msg", "hello world");

    assertEquals(expectedVariables, activityInstance.getVariables());
    assertEquals(expectedVariables, processInstance.getVariables());
  }
}