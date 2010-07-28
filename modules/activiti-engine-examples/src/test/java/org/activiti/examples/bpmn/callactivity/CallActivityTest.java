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

package org.activiti.examples.bpmn.callactivity;

import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joram Barrez
 */
public class CallActivityTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared(resources = {"orderProcess.bpmn20.xml", "checkCreditProcess.bpmn20.xml"})
  public void testOrderProcessWithCallActivity() {
    
    // After the process has started, the 'verify credit history' task should be active
    deployer.getProcessService().startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = deployer.getTaskService().createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("Verify credit history", verifyCreditTask.getName());
    
    // Completing the task with approval, will end the subprocess and continue the original process
    deployer.getTaskService().complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
  }

}
