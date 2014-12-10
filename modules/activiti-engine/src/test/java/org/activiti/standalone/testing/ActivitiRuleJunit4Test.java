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

package org.activiti.standalone.testing;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;


/**
 * Test runners follow the this rule:
 *   - if the class extends Testcase, run as Junit 3
 *   - otherwise use Junit 4
 *   
 * So this test can be included in the regular test suite without problems.
 * 
 * @author Joram Barrez
 */
public class ActivitiRuleJunit4Test {
  
  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();
  
  @Test
  @Deployment
  public void ruleUsageExample() {
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    runtimeService.startProcessInstanceByKey("ruleUsage");
    
    TaskService taskService = activitiRule.getTaskService();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  //this is to show how JobTestHelper could be used to wait for jobs to be all processed
  @Test
  @Deployment(resources={"org/activiti/engine/test/bpmn/async/AsyncTaskTest.testAsyncTask.bpmn20.xml"})
  public void testWaitForJobs() {
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    ManagementService managementService = activitiRule.getManagementService();
   
    // start process 
    runtimeService.startProcessInstanceByKey("asyncTask");
   
    // now there should be one job in the database:
    assertEquals(1, managementService.createJobQuery().count());
      
    JobTestHelper.waitForJobExecutorToProcessAllJobs(activitiRule, 5000L, 500L);
   
    // the job is done
    assertEquals(0, managementService.createJobQuery().count()); 
  }
}
