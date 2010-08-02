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

package org.activiti.engine.test.bpmn.compatibility;

import java.util.Arrays;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * Test cases for checking if process models created with the Activiti Modeler,
 * (currently exporting beta1 models instead of 'final' models) can be executed
 * on the Activiti engine.
 * 
 * TODO: remove when Activiti Modeler is upgraded to final XSD.
 * 
 * @author Joram Barrez
 */
public class BpmnBetaCompatibilityTest extends ProcessEngineTestCase {
  
  @Deployment
  public void testStartToEndProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd");
    assertTrue(processInstance.isEnded());
  }
  
  /**
   * One of the changes from beta->final was the scriptLanguage->scriptFormat change
   * This test verifies that the parsing is done correctly.
   */
  @Deployment
  public void testScriptTask() {
    ProcessInstance processInstance = runtimeService
      .startProcessInstanceByKey("scriptTask", CollectionUtil.singletonMap("numbers", Arrays.asList(1,2,3)));

    Task task = taskService.createTaskQuery()
      .processInstance(processInstance.getId())
      .singleResult();
    assertEquals("Human task", task.getName());
    
    Integer sum = (Integer) runtimeService.getVariable(processInstance.getId(), "sum"); 
    assertEquals(6, sum.intValue());
  }

}
