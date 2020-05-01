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
package org.activiti.examples.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**

 */
public class BoundaryTimerEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testInterruptingTimerDuration() throws Exception{

    // Start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("escalationExample");

    // There should be one task, with a timer : first line support
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("First line support");

      Thread.sleep(2000);

    // Manually execute the job
    Job timer = managementService.createTimerJobQuery().singleResult();
    managementService.moveTimerToExecutableJob(timer.getId());
    managementService.executeJob(timer.getId());

    // The timer has fired, and the second task (second line support) now exists
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Handle escalated issue");
  }

}
