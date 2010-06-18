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

import org.activiti.client.ClientExecution;
import org.activiti.client.ClientProcessDefinition;
import org.activiti.client.ClientProcessInstance;
import org.activiti.client.ProcessDefinitionBuilder;
import org.activiti.impl.util.LogUtil;
import org.activiti.test.pvm.activities.WaitState;

import junit.framework.TestCase;


/**
 * @author Tom Baeyens
 */
public class PvmVariablesTest extends TestCase {

  static {LogUtil.readJavaUtilLoggingConfigFromClasspath();}

  public void testVariables() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
      
    
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("amount", 500L);
    processInstance.start();
    
    ClientExecution execution = processInstance.findExecution("a");
    assertEquals(500L, execution.getVariable("amount"));
  }
}