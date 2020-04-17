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

package org.activiti.examples.bpmn.subprocess;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**

 */
public class SubProcessTest extends PluggableActivitiTestCase {

  @Test
  public void testSimpleSubProcess() {

    Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/examples/bpmn/subprocess/SubProcessTest.fixSystemFailureProcess.bpmn20.xml").deploy();

    // After staring the process, both tasks in the subprocess should be
    // active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("fixSystemFailure");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderByTaskName().asc().list();

    // Tasks are ordered by name (see query)
    assertThat(tasks).hasSize(2);
    Task investigateHardwareTask = tasks.get(0);
    Task investigateSoftwareTask = tasks.get(1);
    assertThat(investigateHardwareTask.getName()).isEqualTo("Investigate hardware");
    assertThat(investigateSoftwareTask.getName()).isEqualTo("Investigate software");

    // Completing both the tasks finishes the subprocess and enables the
    // task after the subprocess
    taskService.complete(investigateHardwareTask.getId());
    taskService.complete(investigateSoftwareTask.getId());

    Task writeReportTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(writeReportTask.getName()).isEqualTo("Write report");

    // Clean up
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}
