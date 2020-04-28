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
package org.activiti.examples.bpmn.tasklistener;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**



 */
public class CustomTaskAssignmentTest extends PluggableActivitiTestCase {


  @Deployment
  public void testCandidateGroupAssignment() {
    runtimeService.startProcessInstanceByKey("customTaskAssignment");
    assertThat(taskService.createTaskQuery().taskCandidateGroup("management").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser("kermit", asList("management")).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser("fozzie",null).count()).isEqualTo(0);
  }

  @Deployment
  public void testCandidateUserAssignment() {
    runtimeService.startProcessInstanceByKey("customTaskAssignment");
    assertThat(taskService.createTaskQuery().taskCandidateUser("kermit",null).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser("fozzie",null).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser("gonzo",null).count()).isEqualTo(0);
  }

  @Deployment
  public void testAssigneeAssignment() {
    runtimeService.startProcessInstanceByKey("setAssigneeInListener");
    assertThat(taskService.createTaskQuery().taskAssignee("kermit").singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().taskAssignee("fozzie").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskAssignee("gonzo").count()).isEqualTo(0);
  }

  @Deployment
  public void testOverwriteExistingAssignments() {
    runtimeService.startProcessInstanceByKey("overrideAssigneeInListener");
    assertThat(taskService.createTaskQuery().taskAssignee("kermit").singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().taskAssignee("fozzie").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskAssignee("gonzo").count()).isEqualTo(0);
  }

  @Deployment
  public void testOverwriteExistingAssignmentsFromVariable() {
    // prepare variables
    Map<String, String> assigneeMappingTable = new HashMap<String, String>();
    assigneeMappingTable.put("fozzie", "gonzo");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("assigneeMappingTable", assigneeMappingTable);

    // start process instance
    runtimeService.startProcessInstanceByKey("customTaskAssignment", variables);

    // check task lists
    assertThat(taskService.createTaskQuery().taskAssignee("gonzo").singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().taskAssignee("fozzie").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskAssignee("kermit").count()).isEqualTo(0);
  }

  @Deployment
  public void testReleaseTask() throws Exception {
    runtimeService.startProcessInstanceByKey("releaseTaskProcess");

    Task task = taskService.createTaskQuery().taskAssignee("fozzie").singleResult();
    assertThat(task).isNotNull();
    String taskId = task.getId();

    // Set assignee to null
    taskService.setAssignee(taskId, null);

    task = taskService.createTaskQuery().taskAssignee("fozzie").singleResult();
    assertThat(task).isNull();

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getAssignee()).isNull();
  }

}
