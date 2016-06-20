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
package org.activiti5.engine.test.bpmn.event.timer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

public class IntermediateTimerEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testCatchingTimerEvent() throws Exception {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample");
    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertEquals(1, jobQuery.count());

    // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(5000L, 200L);

    assertEquals(0, jobQuery.count());
    assertProcessEnded(pi.getProcessInstanceId());
  }

  @Deployment
  public void testTimerEventWithStartAndDuration() throws Exception {

    Date testStartTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(testStartTime);

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("timerEventWithStartAndDuration");
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("Task A", task.getName());

    TimerJobQuery jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertEquals(0, jobQuery.count());

    Date startDate = new Date();
    runtimeService.setVariable(pi.getId(), "StartDate", startDate);
    taskService.complete(task.getId());

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertEquals(1, jobQuery.count());

    processEngineConfiguration.getClock().setCurrentTime(new Date(startDate.getTime() + 7000L));

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertEquals(1, jobQuery.count());

    processEngineConfiguration.getClock().setCurrentTime(new Date(startDate.getTime() + 11000L));
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(15000L, 200L);

    jobQuery = managementService.createTimerJobQuery().processInstanceId(pi.getId());
    assertEquals(0, jobQuery.count());

    tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    assertEquals("Task B", task.getName());
    taskService.complete(task.getId());

    assertProcessEnded(pi.getProcessInstanceId());
  }

  @Deployment
  public void testExpression() {
    // Set the clock fixed
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("dueDate", new Date());

    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("dueDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));

    // After process start, there should be timer created
    ProcessInstance pi1 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables1);
    ProcessInstance pi2 = runtimeService.startProcessInstanceByKey("intermediateTimerEventExample", variables2);

    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(pi1.getId()).count());
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(pi2.getId()).count());

    // After setting the clock to one second in the future the timers should fire
    List<Job> jobs = managementService.createTimerJobQuery().list();
    assertEquals(2, jobs.size());
    for (Job job : jobs) {
      managementService.moveTimerToExecutableJob(job.getId());
      managementService.executeJob(job.getId());
    }

    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(pi1.getId()).count());
    assertEquals(0, managementService.createTimerJobQuery().processInstanceId(pi2.getId()).count());

    assertProcessEnded(pi1.getProcessInstanceId());
    assertProcessEnded(pi2.getProcessInstanceId());
  }

  @Deployment
  public void testLoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLoop");

    // After looping 3 times, the process should end
    for (int i = 0; i < 3; i++) {
      Job timer = managementService.createTimerJobQuery().singleResult();
      managementService.moveTimerToExecutableJob(timer.getId());
      managementService.executeJob(timer.getId());
    }

    assertProcessEnded(processInstance.getId());
  }

  @Deployment
  public void testLoopWithCycle() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testLoop");

    // After looping 3 times, the process should end. Cycle should NOT repeat itself
    for (int i = 0; i < 3; i++) {
      Job timer = managementService.createTimerJobQuery().singleResult();
      managementService.moveTimerToExecutableJob(timer.getId());
      managementService.executeJob(timer.getId());
    }

    assertProcessEnded(processInstance.getId());
  }

}
