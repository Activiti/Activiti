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

import java.util.Date;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Joram Barrez
 */
public class BoundaryTimerEventTest extends ProcessEngineTestCase {

  @Deployment
  public void testInterruptingTimerDuration() {
    
    Date startTime = new Date();

    // Start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("interruptingBoundaryTimer");

    // There should be one task, with a timer : first line support
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("First line support", task.getName());

    // Set clock to the future such that the timer can fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (5 * 60 * 60 * 1000)));
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    // The timer has fired, and the second task (secondlinesupport) now exists
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Second line support", task.getName());
  }

}
