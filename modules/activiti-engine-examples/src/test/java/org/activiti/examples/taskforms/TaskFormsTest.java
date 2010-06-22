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
package org.activiti.examples.taskforms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.Task;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskFormsTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Before
  public void setUp() throws Exception {
    deployer.getIdentityService().saveUser(deployer.getIdentityService().newUser("fozzie"));
    deployer.getIdentityService().saveGroup(deployer.getIdentityService().newGroup("management"));
    deployer.getIdentityService().createMembership("fozzie", "management");
  }

  @After
  public void tearDown() throws Exception {
    deployer.getIdentityService().deleteGroup("management");
    deployer.getIdentityService().deleteUser("fozzie");
  }

  @Test
  @ProcessDeclared(resources = { "VacationRequest.bpmn20.xml", "approve.form", "request.form", "adjustRequest.form" })
  public void testTaskFormsWithVacationRequestProcess() {

    // Get start form
    Object startForm = deployer.getProcessService().getStartFormByKey("vacationRequest");
    assertNotNull(startForm);

    // Define variables that would be filled in through the form
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("employeeName", "kermit");
    parameters.put("numberOfDays", "4");
    parameters.put("vacationMotivation", "I'm tired");
    deployer.getProcessService().startProcessInstanceByKey("vacationRequest", parameters);

    // Management should now have a task assigned to them
    Task task = deployer.getTaskService().createTaskQuery().candidateGroup("management").singleResult();
    assertEquals("Vacation request by kermit", task.getDescription());
    Object taskForm = deployer.getTaskService().getTaskForm(task.getId());
    assertNotNull(taskForm);

  }

  @Test
  @ProcessDeclared
  public void testTaskFormUnavailable() {
    assertNull(deployer.getProcessService().getStartFormByKey("noStartOrTaskForm"));

    deployer.getProcessService().startProcessInstanceByKey("noStartOrTaskForm");
    Task task = deployer.getTaskService().createTaskQuery().singleResult();
    assertNull(deployer.getTaskService().getTaskForm(task.getId()));
  }

}
