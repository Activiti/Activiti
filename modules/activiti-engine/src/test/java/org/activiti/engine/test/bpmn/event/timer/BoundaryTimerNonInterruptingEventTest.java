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

package org.activiti.engine.test.bpmn.event.timer;

import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class BoundaryTimerNonInterruptingEventTest extends PluggableActivitiTestCase {
    
  @Deployment
  public void testMultipleTimersOnUserTask() {
    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingTimersOnUserTask");
    Task task1 = taskService.createTaskQuery().singleResult();
    assertEquals("First Task", task1.getName());
    
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(2, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the first timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    
    // we still have one timer more to fire
    assertEquals(1L, jobQuery.count());

    // and we are still in the first state, but in the second state as well!    
    assertEquals(2L, taskService.createTaskQuery().count());
    List<Task> taskList = taskService.createTaskQuery().orderByTaskName().desc().list();
    assertEquals("First Task", taskList.get(0).getName());
    assertEquals("Escalation Task 1", taskList.get(1).getName());

    // complete the task and end the forked execution
    taskService.complete(taskList.get(1).getId());

    // but we still have the original executions    
    assertEquals(1L, taskService.createTaskQuery().count());
    assertEquals("First Task", taskService.createTaskQuery().singleResult().getName());

    // After setting the clock to time '2 hour and 5 seconds', the second timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    
    // no more timers to fire
    assertEquals(0L, jobQuery.count());

    // and we are still in the first state, but in the next escalation state as well    
    assertEquals(2L, taskService.createTaskQuery().count());
    taskList = taskService.createTaskQuery().orderByTaskName().desc().list();
    assertEquals("First Task", taskList.get(0).getName());
    assertEquals("Escalation Task 2", taskList.get(1).getName());

    // This time we end the main task
    taskService.complete(taskList.get(0).getId());

    // but we still have the escalation task    
    assertEquals(1L, taskService.createTaskQuery().count());
    Task escalationTask = taskService.createTaskQuery().singleResult();
    assertEquals("Escalation Task 2", escalationTask.getName());
    
    taskService.complete(escalationTask.getId());

    // now we are really done :-)
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testJoin() {
    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testJoin");
    Task task1 = taskService.createTaskQuery().singleResult();
    assertEquals("Main Task", task1.getName());
    
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the first timer should fire
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    
    // timer has fired
    assertEquals(0L, jobQuery.count());

    // we now have both tasks
    assertEquals(2L, taskService.createTaskQuery().count());
    
    // end the first
    taskService.complete(task1.getId());
    
    // we now have one task left
    assertEquals(1L, taskService.createTaskQuery().count());    
    Task task2 = taskService.createTaskQuery().singleResult();
    assertEquals("Escalation Task", task2.getName());

    // complete the task, the parallel gateway should fire
    taskService.complete(task2.getId());

    // and the process has ended
    assertProcessEnded(pi.getId());
  }
  

}
