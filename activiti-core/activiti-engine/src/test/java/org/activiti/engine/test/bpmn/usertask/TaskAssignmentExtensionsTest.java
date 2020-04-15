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
package org.activiti.engine.test.bpmn.usertask;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * Testcase for the non-spec extensions to the task candidate use case.
 *

 */
public class TaskAssignmentExtensionsTest extends PluggableActivitiTestCase {

  private static final String KERMIT = "kermit";
  private static final List<String> KERMITSGROUPS = asList("management","accountancy");

  private static final String GONZO = "gonzo";
  private static final List<String> GONZOSGROUPS = asList();

  private static final String FOZZIE = "fozzie";
  private static final List<String> FOZZIESGROUPS = asList("management");

  @Deployment
  public void testAssigneeExtension() {
    runtimeService.startProcessInstanceByKey("assigneeExtension");
    List<Task> tasks = taskService.createTaskQuery().taskAssignee(KERMIT).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
  }

  public void testDuplicateAssigneeDeclaration() {
    String resource = TestHelper.getBpmnProcessDefinitionResource(getClass(), "testDuplicateAssigneeDeclaration");
    assertThatExceptionOfType(XMLException.class)
      .as("Invalid BPMN 2.0 process should not parse, but it gets parsed successfully")
      .isThrownBy(() -> repositoryService.createDeployment().addClasspathResource(resource).deploy());
  }

  @Deployment
  public void testOwnerExtension() {
    runtimeService.startProcessInstanceByKey("ownerExtension");
    List<Task> tasks = taskService.createTaskQuery().taskOwner(GONZO).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
  }

  @Deployment
  public void testCandidateUsersExtension() {
    runtimeService.startProcessInstanceByKey("candidateUsersExtension");
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks).hasSize(1);
    tasks = taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list();
    assertThat(tasks).hasSize(1);
  }

  @Deployment
  public void testCandidateGroupsExtension() {
    runtimeService.startProcessInstanceByKey("candidateGroupsExtension");

    // Bugfix check: potentially the query could return 2 tasks since
    // kermit is a member of the two candidate groups
    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("make profit");

    tasks = taskService.createTaskQuery().taskCandidateUser(FOZZIE,FOZZIESGROUPS).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("make profit");

    // Test the task query find-by-candidate-group operation
    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.taskCandidateGroup("management").count()).isEqualTo(1);
    assertThat(query.taskCandidateGroup("accountancy").count()).isEqualTo(1);
  }

  // Test where the candidate user extension is used together
  // with the spec way of defining candidate users
  @Deployment
  public void testMixedCandidateUserDefinition() {
    runtimeService.startProcessInstanceByKey("mixedCandidateUser");

    List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks).hasSize(1);

    tasks = taskService.createTaskQuery().taskCandidateUser(FOZZIE,FOZZIESGROUPS).list();
    assertThat(tasks).hasSize(1);

    tasks = taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list();
    assertThat(tasks).hasSize(1);

    tasks = taskService.createTaskQuery().taskCandidateUser("mispiggy",null).list();
    assertThat(tasks).hasSize(0);
  }

}
