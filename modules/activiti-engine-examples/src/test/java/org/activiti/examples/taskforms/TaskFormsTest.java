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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskFormsTest extends ProcessEngineTestCase {

  public void setUp() throws Exception {
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveGroup(identityService.newGroup("management"));
    identityService.createMembership("fozzie", "management");
  }

  public void tearDown() throws Exception {
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
  }

  @Deployment(resources = { "VacationRequest.bpmn20.xml", "approve.form", "request.form", "adjustRequest.form" })
  public void testTaskFormsWithVacationRequestProcess() {

    // Get start form
    Object startForm = repositoryService.getStartFormByKey("vacationRequest");
    assertNotNull(startForm);

    // Define variables that would be filled in through the form
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("employeeName", "kermit");
    parameters.put("numberOfDays", "4");
    parameters.put("vacationMotivation", "I'm tired");
    runtimeService.startProcessInstanceByKey("vacationRequest", parameters);

    // Management should now have a task assigned to them
    Task task = taskService.createTaskQuery().candidateGroup("management").singleResult();
    assertEquals("Vacation request by kermit", task.getDescription());
    Object taskForm = taskService.getTaskForm(task.getId());
    assertNotNull(taskForm);

  }

  @Deployment
  public void testTaskFormUnavailable() {
    assertNull(repositoryService.getStartFormByKey("noStartOrTaskForm"));

    runtimeService.startProcessInstanceByKey("noStartOrTaskForm");
    Task task = taskService.createTaskQuery().singleResult();
    assertNull(taskService.getTaskForm(task.getId()));
  }

}
