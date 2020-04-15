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
package org.activiti.engine.test.api.task;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class DelegateTaskTest extends PluggableActivitiTestCase {

  /**
   * @see <a href="https://activiti.atlassian.net/browse/ACT-380">https://activiti.atlassian.net/browse/ACT-380</a>
   */
  @Deployment
  public void testGetCandidates() {
    runtimeService.startProcessInstanceByKey("DelegateTaskTest.testGetCandidates");

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    @SuppressWarnings("unchecked")
    Set<String> candidateUsers = (Set<String>) taskService.getVariable(task.getId(), DelegateTaskTestTaskListener.VARNAME_CANDIDATE_USERS);
    assertThat(candidateUsers).hasSize(2);
    assertThat(candidateUsers.contains("kermit")).isTrue();
    assertThat(candidateUsers.contains("gonzo")).isTrue();

    @SuppressWarnings("unchecked")
    Set<String> candidateGroups = (Set<String>) taskService.getVariable(task.getId(), DelegateTaskTestTaskListener.VARNAME_CANDIDATE_GROUPS);
    assertThat(candidateGroups).hasSize(2);
    assertThat(candidateGroups.contains("management")).isTrue();
    assertThat(candidateGroups.contains("accountancy")).isTrue();
  }

  @Deployment
  public void testChangeCategoryInDelegateTask() {

    // Start process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("approvers", singletonList("kermit")); // , "gonzo", "mispiggy"));
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("delegateTaskTest", variables);

    // Assert there are three tasks with the default category
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertThat(task.getCategory()).isEqualTo("approval");
      Map<String, Object> taskVariables = new HashMap<String, Object>();
      taskVariables.put("outcome", "approve");
      taskService.complete(task.getId(), taskVariables, true);
    }

    // After completion, the task category should be changed in the script
    // listener working on the delegate task
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
    for (HistoricTaskInstance historicTaskInstance : historyService.createHistoricTaskInstanceQuery().list()) {
      assertThat(historicTaskInstance.getCategory()).isEqualTo("approved");
    }
  }

}
