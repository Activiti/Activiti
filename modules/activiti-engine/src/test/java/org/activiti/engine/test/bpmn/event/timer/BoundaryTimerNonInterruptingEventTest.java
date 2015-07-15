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
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
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
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    Job job = managementService.createJobQuery().executable().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());
    
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
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
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
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
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
  
  @Deployment
  public void testTimerOnConcurrentTasks() {
    String procId = runtimeService.startProcessInstanceByKey("nonInterruptingOnConcurrentTasks").getId();
    assertEquals(2, taskService.createTaskQuery().count());
    
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(3, taskService.createTaskQuery().count());
    
    // Complete task that was reached by non interrupting timer 
    Task task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());
    assertEquals(2, taskService.createTaskQuery().count());
    
    // Complete other tasks
    for (Task t : taskService.createTaskQuery().list()) {
      taskService.complete(t.getId());
    }
    assertProcessEnded(procId);
  }
  
  // Difference with previous test: now the join will be reached first
  @Deployment(resources = {"org/activiti/engine/test/bpmn/event/timer/BoundaryTimerNonInterruptingEventTest.testTimerOnConcurrentTasks.bpmn20.xml"})
  public void testTimerOnConcurrentTasks2() {
    String procId = runtimeService.startProcessInstanceByKey("nonInterruptingOnConcurrentTasks").getId();
    assertEquals(2, taskService.createTaskQuery().count());
    
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(3, taskService.createTaskQuery().count());
    
    // Complete 2 tasks that will trigger the join
    Task task = taskService.createTaskQuery().taskDefinitionKey("firstTask").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("secondTask").singleResult();
    taskService.complete(task.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    
    // Finally, complete the task that was created due to the timer 
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(procId);
  }

  @Deployment
  public void testTimerWithCycle() throws Exception {
    runtimeService.startProcessInstanceByKey("nonInterruptingCycle").getId();

    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
    //boundary events
    try {
      waitForJobExecutorToProcessAllJobs(2000, 100);
      fail("a new job must be prepared because there are undefinite number of repeats 1 hour interval");
    }catch (Exception ex){
      //expected exception because a new job is prepared
    }

   moveByMinutes(60);
    try {
      waitForJobExecutorToProcessAllJobs(2000, 100);
      fail("a new job must be prepared because there are undefinite number of repeats 1 hour interval");
    }catch (Exception ex){
      //expected exception because a new job is prepared
    }


    Task task = taskService.createTaskQuery().taskDefinitionKey("task").singleResult();
    taskService.complete(task.getId());

    moveByMinutes(60);
    try {
      waitForJobExecutorToProcessAllJobs(2000, 100);
     }catch (Exception ex){
      fail("No more jobs since the user completed the task");

    }
  }
  
  @Deployment
  /**
   * see https://activiti.atlassian.net/browse/ACT-1173
   */
  public void testTimerOnEmbeddedSubprocess() {
    String id = runtimeService.startProcessInstanceByKey("nonInterruptingTimerOnEmbeddedSubprocess").getId();
    
    TaskQuery tq = taskService.createTaskQuery().taskAssignee("kermit");
    
    assertEquals(1, tq.count());
    
    // Simulate timer
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    
    tq = taskService.createTaskQuery().taskAssignee("kermit");
    
    assertEquals(2, tq.count());
    
    List<Task> tasks = tq.list(); 
    
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());
    
    assertProcessEnded(id);
  }
  
  @Deployment
  /**
   * see https://activiti.atlassian.net/browse/ACT-1106
   */
  public void testReceiveTaskWithBoundaryTimer(){
    // Set the clock fixed
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("timeCycle", "R/PT1H");
    
    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingCycle",variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());
    
    // The Execution Query should work normally and find executions in state "task"
    List<Execution> executions = runtimeService.createExecutionQuery()
      .activityId("task")
      .list();
    assertEquals(1, executions.size());
    List<String> activeActivityIds = runtimeService.getActiveActivityIds(executions.get(0).getId());
    assertEquals(1, activeActivityIds.size());
    assertEquals("task", activeActivityIds.get(0));
    
    runtimeService.signal(executions.get(0).getId());

//    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
//    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
//    waitForJobExecutorToProcessAllJobs(5000L, 25L);
//    assertEquals(0L, jobQuery.count());

    // which means the process has ended
    assertProcessEnded(pi.getId());
  }  
  
  @Deployment
  public void testTimerOnConcurrentSubprocess() {
    String procId = runtimeService.startProcessInstanceByKey("testTimerOnConcurrentSubprocess").getId();
    assertEquals(4, taskService.createTaskQuery().count());
    
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(5, taskService.createTaskQuery().count());
    
    // Complete 4 tasks that will trigger the join
    Task task = taskService.createTaskQuery().taskDefinitionKey("sub1task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub1task2").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task2").singleResult();
    taskService.complete(task.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    
    // Finally, complete the task that was created due to the timer 
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(procId);
  }
  
  @Deployment(resources="org/activiti/engine/test/bpmn/event/timer/BoundaryTimerNonInterruptingEventTest.testTimerOnConcurrentSubprocess.bpmn20.xml")
  public void testTimerOnConcurrentSubprocess2() {
    String procId = runtimeService.startProcessInstanceByKey("testTimerOnConcurrentSubprocess").getId();
    assertEquals(4, taskService.createTaskQuery().count());
    
    Job timer = managementService.createJobQuery().singleResult();
    managementService.executeJob(timer.getId());
    assertEquals(5, taskService.createTaskQuery().count());
    
    Task task = taskService.createTaskQuery().taskDefinitionKey("sub1task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub1task2").singleResult();
    taskService.complete(task.getId());
    
    // complete the task that was created due to the timer 
    task = taskService.createTaskQuery().taskDefinitionKey("timerFiredTask").singleResult();
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task1").singleResult();
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().taskDefinitionKey("sub2task2").singleResult();
    taskService.complete(task.getId());
    assertEquals(0, taskService.createTaskQuery().count());
    
    assertProcessEnded(procId);
  }

  private void moveByMinutes(int minutes) throws Exception {
    processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + ((minutes * 60 * 1000))));
  }



}
