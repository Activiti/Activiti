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

package org.activiti.engine.test.api.testing;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;


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
  
  @BeforeClass
  public static void beforeClass() {
    /*
     * If this test is run in the Maven suite, there will be already process
     * engines initialized. Since we use the samen activiti.properties for each
     * test, the processengine that is constructed in the ActivitiRule, will try
     * to do a DB schema create and fail since the schema was already
     * initialized.
     * 
     * By closing the existing process engines, existing db schema is dropped
     * and the cached Process Engine is nullified. The next test that uses the
     * ActivitiInternaleTestCase will then construct a new ProcessEngine and
     * create the schema again.
     */
    ActivitiInternalTestCase.closeProcessEngine();
  }
  
  @AfterClass
  public static void afterClass() {
    /*
     * After the test has run, we need to drop the schema. The ActivitiRule will
     * not do this automatically.
     * 
     * Tests that follow this test will recreate the process engine (since we
     * nullified it in the BeforeClass) and execute a DB schema create, which
     * will fail if the DB schema isn't dropped here.
     */
    TestHelper.closeProcessEngines();
  }
  
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

}
