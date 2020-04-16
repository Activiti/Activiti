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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for task candidate use case.
 *

 */
public class TaskAssignmentCandidateTest extends PluggableActivitiTestCase {


  @Deployment
  public void testCandidateGroups() {
    runtimeService.startProcessInstanceByKey("taskCandidateExample");
    List<Task> tasks = taskService
      .createTaskQuery()
      .taskCandidateGroup("management")
      .list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("theTask");
    taskService.complete(tasks.get(0).getId());

    tasks = taskService.createTaskQuery().taskCandidateGroup("accounting").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("theOtherTask");
    taskService.complete(tasks.get(0).getId());
  }

}
