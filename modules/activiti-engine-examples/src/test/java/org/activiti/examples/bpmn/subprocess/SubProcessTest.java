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

package org.activiti.examples.bpmn.subprocess;

import java.util.List;

import org.activiti.engine.Deployment;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.engine.test.ProcessEngineTestCase;


/**
 * @author Joram Barrez
 */
public class SubProcessTest extends ProcessEngineTestCase {
  
  public void testSimpleSubProcess() {
    
    Deployment deployment = 
      repositoryService.createDeployment()
                  .addClasspathResource("org/activiti/examples/bpmn/subprocess/SubProcessTest.fixSystemFailureProcess.bpmn20.xml")
                  .deploy();
    
    // After staring the process, both tasks in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fixSystemFailure");
    List<Task> tasks = taskService.createTaskQuery()
                                  .processInstance(pi.getId())
                                  .orderAsc(TaskQuery.PROPERTY_NAME)
                                  .list();

    // Tasks are ordered by name (see query)
    Task investigateHardwareTask = tasks.get(0);
    Task investigateSoftwareTask = tasks.get(1);
    assertEquals("Investigate hardware", investigateHardwareTask.getName());
    assertEquals("Investigate software", investigateSoftwareTask.getName());
    
    // Completing boith the tasks finishes the subprocess and enables the task after the subprocess
    taskService.complete(investigateHardwareTask.getId());
    taskService.complete(investigateSoftwareTask.getId());
    Task writeReportTask = taskService.createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Write report", writeReportTask.getName());
    
    // Clean up
    repositoryService.deleteDeployment(deployment.getId());
  }

}
