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

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiTestCase;
import org.activiti.engine.test.Deployment;


/**
 * @author Joram Barrez
 */
public class ActivitiTestCaseTest extends ActivitiTestCase {
  
  @Override
  protected void setUp() throws Exception {
    ActivitiInternalTestCase.closeProcessEngine(); 
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    /*
     * After the test has run, we need to drop the schema.
     * 
     * Tests that follow this test will recreate the process engine (since we
     * nullified it in the setup) and execute a DB schema create, which
     * will fail if the DB schema isn't dropped here.
     */
    TestHelper.closeProcessEngines();
    ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfiguration();
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    try {
      processEngineConfiguration.dbSchemaDrop();
    } catch (Exception e) {}
  }
  
  @Deployment
  public void testSimpleProcess() {
    runtimeService.startProcessInstanceByKey("simpleProcess");
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("My Task", task.getName());
    
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  

}
