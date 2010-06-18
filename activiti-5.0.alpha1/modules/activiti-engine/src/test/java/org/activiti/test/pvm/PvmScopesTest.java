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

import junit.framework.TestCase;

import org.activiti.client.ClientExecution;
import org.activiti.client.ClientProcessDefinition;
import org.activiti.client.ClientProcessInstance;
import org.activiti.client.ProcessDefinitionBuilder;
import org.activiti.impl.util.LogUtil;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmScopesTest extends TestCase {

  static {LogUtil.readJavaUtilLoggingConfigFromClasspath();}

  public void testSimpleNestedScope() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinition()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("unscopedA")
      .endActivity()
      .createActivity("unscopedA")
        .behavior(new WaitState())
        .transition("scopedB")
      .endActivity()
      .createActivity("scopedB")
        .scope()
        .behavior(new WaitState())
        .transition("unscopedC")
      .endActivity()
      .createActivity("unscopedC")
        .behavior(new WaitState())
      .endActivity()
    .endProcessDefinition();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    ClientExecution executionInUnscopedA = processInstance.findExecution("unscopedA");
    assertNotNull(executionInUnscopedA);
    assertSame(processInstance, executionInUnscopedA);
    
    executionInUnscopedA.event(null);

    ClientExecution executionInScopedB = processInstance.findExecution("scopedB");
    assertNotNull(executionInScopedB);
    assertNotSame(processInstance, executionInScopedB);
    
    executionInScopedB.event(null);

    ClientExecution executionInUnscopedC = processInstance.findExecution("unscopedC");
    assertNotNull(executionInUnscopedC);
    assertSame(processInstance, executionInUnscopedC);
  }

}
