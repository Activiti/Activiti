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
package org.activiti.examples.bpmn.scripttask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.activiti.ProcessInstance;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.activiti.util.CollectionUtil;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class ScriptTaskTest extends ActivitiTestCase {

  @Test
  @ProcessDeclared
  public void testScriptExecution() {
    int[] inputArray = new int[] { 1, 2, 3, 4, 5 };
    ProcessInstance pi = processService.startProcessInstanceByKey("scriptExecution", CollectionUtil.singletonMap("inputArray", inputArray));

    Integer result = (Integer) processService.getVariable(pi.getId(), "sum");
    assertEquals(15, result.intValue());
  }

  @Test
  @ProcessDeclared
  public void testSetVariableThroughExecutionInScript() {
    ProcessInstance pi = processService.startProcessInstanceByKey("setScriptVariableThroughExecution");

    // Since 'def' is used, the 'scriptVar' will be script local
    // and not automatically stored as a process variable.
    assertNull(processService.getVariable(pi.getId(), "scriptVar"));
    assertEquals("test123", processService.getVariable(pi.getId(), "myVar"));
  }

}
