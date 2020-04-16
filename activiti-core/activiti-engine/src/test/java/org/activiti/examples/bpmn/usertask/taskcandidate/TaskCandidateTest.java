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
package org.activiti.examples.bpmn.usertask.taskcandidate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class TaskCandidateTest extends PluggableActivitiTestCase {

  private static final String KERMIT = "kermit";
  private static final List<String> KERMITSGROUPS = asList("accountancy");

  private static final String GONZO = "gonzo";
  private static final List<String> GONZOSGROUPS = asList("management","accountancy","sales");



  @Deployment
  public void testSingleCandidateGroup() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("singleCandidateGroup");

    // Task should not yet be assigned to kermit
    List<Task> tasks = taskService.createTaskQuery().taskAssignee(KERMIT).list();
    assertThat(tasks.isEmpty()).isTrue();

    // The task should be visible in the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks).hasSize(1);
    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Pay out expenses");

    // Claim the task
    taskService.claim(task.getId(), KERMIT);

    // The task must now be gone from the candidate task list
    tasks = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list();
    assertThat(tasks.isEmpty()).isTrue();

    // The task will be visible on the personal task list
    tasks = taskService.createTaskQuery().taskAssignee(KERMIT).list();
    assertThat(tasks).hasSize(1);
    task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Pay out expenses");

    // Completing the task ends the process
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testMultipleCandidateGroups() {

    // Deploy and start process
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("multipleCandidatesGroup");

    // Task should not yet be assigned to anyone
    List<Task> tasks = taskService.createTaskQuery().taskAssignee(KERMIT).list();

    assertThat(tasks.isEmpty()).isTrue();
    tasks = taskService.createTaskQuery().taskAssignee(GONZO).list();

    assertThat(tasks.isEmpty()).isTrue();

    // The task should be visible in the candidate task list of Gonzo and
    // Kermit
    // and anyone in the mgmt/accountancy group
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("management").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("accountancy").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("sales").count()).isEqualTo(0);

    // Gonzo claims the task
    tasks = taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list();
    Task task = tasks.get(0);
    assertThat(task.getName()).isEqualTo("Approve expenses");
    taskService.claim(task.getId(), GONZO);

    // The task must now be gone from the candidate task lists
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list().isEmpty()).isTrue();
    assertThat(taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list().isEmpty()).isTrue();
    assertThat(taskService.createTaskQuery().taskCandidateGroup("management").count()).isEqualTo(0);

    // The task will be visible on the personal task list of Gonzo
    assertThat(taskService.createTaskQuery().taskAssignee(GONZO).count()).isEqualTo(1);

    // But not on the personal task list of (for example) Kermit
    assertThat(taskService.createTaskQuery().taskAssignee(KERMIT).count()).isEqualTo(0);

    // Completing the task ends the process
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testMultipleCandidateUsers() {
    runtimeService.startProcessInstanceByKey("multipleCandidateUsersExample", singletonMap("Variable", (Object) "var"));

    assertThat(taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list()).hasSize(1);

    List<Task> tasks = taskService.createTaskQuery().taskInvolvedUser(KERMIT).list();
    assertThat(tasks).hasSize(1);

    Task task = tasks.get(0);
    taskService.setVariableLocal(task.getId(), "taskVar", 123);
    tasks = taskService.createTaskQuery().taskInvolvedUser(KERMIT).includeProcessVariables().includeTaskLocalVariables().list();
    task = tasks.get(0);

    assertThat(task.getProcessVariables()).hasSize(1);
    assertThat(task.getTaskLocalVariables()).hasSize(1);
    taskService.addUserIdentityLink(task.getId(), GONZO, "test");

    tasks = taskService.createTaskQuery().taskInvolvedUser(GONZO).includeProcessVariables().includeTaskLocalVariables().list();
    assertThat(tasks).hasSize(1);
    assertThat(task.getProcessVariables()).hasSize(1);
    assertThat(task.getTaskLocalVariables()).hasSize(1);
  }

  @Deployment
  public void testMixedCandidateUserAndGroup() {
    runtimeService.startProcessInstanceByKey("mixedCandidateUserAndGroupExample");

    assertThat(taskService.createTaskQuery().taskCandidateUser(GONZO,GONZOSGROUPS).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list()).hasSize(1);
  }

  // test if candidate group works with expression, when there is a function
  // with one parameter
  @Deployment
  public void testCandidateExpressionOneParam() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("testBean", new TestBean());

    runtimeService.startProcessInstanceByKey("candidateWithExpression", params);
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).list()).hasSize(1);

  }

  // test if candidate group works with expression, when there is a function
  // with two parameters
  @Deployment
  public void testCandidateExpressionTwoParams() {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("testBean", new TestBean());

    runtimeService.startProcessInstanceByKey("candidateWithExpression", params);
    assertThat(taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskCandidateGroup("sales").count()).isEqualTo(1);
  }

}
