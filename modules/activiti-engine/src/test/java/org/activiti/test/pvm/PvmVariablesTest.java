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

import static org.junit.Assert.assertEquals;

import org.activiti.pvm.ObjectExecution;
import org.activiti.pvm.ObjectProcessDefinition;
import org.activiti.pvm.ObjectProcessInstance;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.test.LogInitializer;
import org.activiti.test.pvm.activities.WaitState;
import org.junit.Test;


/**
 * @author Tom Baeyens
 */
public class PvmVariablesTest extends LogInitializer {

  @Test
  public void testVariables() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
      
    
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("amount", 500L);
    processInstance.start();
    
    ObjectExecution execution = processInstance.findExecution("a");
    assertEquals(500L, execution.getVariable("amount"));
  }
}