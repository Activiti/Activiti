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
package org.activiti.examples.bpmn.usertask.taskassignee;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;


/**
 * Simple process test to validate the current implementation protoype.
 * 
 * @author Joram Barrez 
 */
public class TaskAssigneeTest extends ActivitiTestCase {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared
  public void testTaskAssignee() {    
    
    // Start process instance
    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("taskAssigneeProcess");

    // assert if the process instance completed
    deployer.expectProcessEnds(processInstance.getId());

    // Get task list
    List<Task> tasks = deployer.getTaskService().findAssignedTasks("kermit");
    assertEquals(1, tasks.size());
    Task myTask = tasks.get(0);
    assertEquals("Schedule meeting", myTask.getName());
    assertEquals("Schedule an engineering meeting for next week with the new hire.", myTask.getDescription());

    // Complete task. Process is now finished
    deployer.getTaskService().complete(myTask.getId());
  }

}
