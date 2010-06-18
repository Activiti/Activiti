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
package org.activiti.examples.pojo;

import junit.framework.TestCase;

import org.activiti.client.ClientProcessDefinition;
import org.activiti.client.ClientProcessInstance;
import org.activiti.client.ProcessDefinitionBuilder;


/**
 * @author Tom Baeyens
 */
public class PojoTest extends TestCase {

  public void testPojoWaitState() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
        .transition("b")
      .endActivity()
      .createActivity("b")
        .behavior(new WaitState())
        .transition("c")
      .endActivity()
      .createActivity("c")
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
      
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isActive("a"));

    processInstance.event(null);

    assertTrue(processInstance.isActive("b"));

    processInstance.event(null);
    
    assertTrue(processInstance.isActive("c"));
  }
  
  public void testPojoAutomatic() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("a")
        .initial()
        .behavior(new Automatic())
        .transition("b")
      .endActivity()
      .createActivity("b")
        .behavior(new Automatic())
        .transition("c")
      .endActivity()
      .createActivity("c")
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
      
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isActive("c"));
  }
}
