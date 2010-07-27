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

package org.activiti.test.bpmn.compatibility;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test cases for checking if process models created with the Activiti Modeler,
 * (currently exporting beta1 models instead of 'final' models) can be executed
 * on the Activiti engine.
 * 
 * TODO: remove when Activiti Modeler is upgraded to final XSD.
 * 
 * @author Joram Barrez
 */
public class BpmnBetaCompatibilityTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();
  
  @Test
  @ProcessDeclared
  public void testStartToEndProcess() {
    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("startToEnd");
    assertTrue(processInstance.isEnded());
  }
  
  /**
   * One of the changes from beta->final was the scriptLanguage->scriptFormat change
   * This test verifies that the parsing is done correctly.
   */
  @Test
  @ProcessDeclared
  public void testScriptTask() {
    ProcessInstance processInstance = deployer.getProcessService()
      .startProcessInstanceByKey("scriptTask", CollectionUtil.singletonMap("numbers", Arrays.asList(1,2,3)));

    Task task = deployer.getTaskService().createTaskQuery()
      .processInstance(processInstance.getId())
      .singleResult();
    assertEquals("Human task", task.getName());
    
    Integer sum = (Integer) deployer.getProcessService().getVariable(processInstance.getId(), "sum"); 
    assertEquals(6, sum.intValue());
  }

}
