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

package org.activiti.engine.test.bpmn.subprocess;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.ProcessEngineTestCase;


/**
 * @author Joram Barrez
 */
public class SubProcessTest extends ProcessEngineTestCase {
  
  @Deployment
  public void testSimpleSubProcess() {
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // the subprocess scope is destroyed and the complete process ends
    taskService.complete(subProcessTask.getId());
    assertNull(runtimeService.findExecutionById(pi.getId()));
  }
  
  /**
   * Same test case as before, but now with all automatic steps
   */
  @Deployment
  public void testSimpleAutomaticSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessAutomatic");
    assertTrue(pi.isEnded());
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testSimpleSubProcessWithTimer() {
    
    Date startTime = new Date();
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // Setting the clock forward 2 hours 1 second (timer fires in 2 hours) and fire up the job executor 
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);

    // The subprocess should be left, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery()
                                                   .processInstanceId(pi.getId())
                                                   .singleResult();
    assertEquals("Fix escalated problem", escalationTask.getName());
  }
  
  /**
   * A test case that has a timer attached to the subprocess,
   * where 2 concurrent paths are defined when the timer fires.
   */
  @Deployment
  public void IGNORED_testSimpleSubProcessWithConcurrentTimer() {
    
    Date startTime = new Date();
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleSubProcessWithConcurrentTimer");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderAsc(TaskQuery.PROPERTY_NAME);
    
    Task subProcessTask = taskQuery.singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // When the timer is fired (after 2 hours), two concurrent paths should be created
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    List<Task> tasksAfterTimer = taskQuery.list();
    assertEquals(2, tasksAfterTimer.size());
    Task taskAfterTimer1 = tasksAfterTimer.get(0);
    Task taskAfterTimer2 = tasksAfterTimer.get(1);
    assertEquals("Task after timer 1", taskAfterTimer1.getName());
    assertEquals("Task after timer 2", taskAfterTimer2.getName());
    
    // Completing the two tasks should end the process instance
    taskService.complete(taskAfterTimer1.getId());
    taskService.complete(taskAfterTimer2.getId());
    assertProcessEnded(pi.getId());
  }
  
  /**
   * Test case where the simple sub process of previous test cases
   * is nested within another subprocess.
   */
  @Deployment
  public void testNestedSimpleSubProcess() {
    
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // both subprocesses are destroyed and the task after the subprocess should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
  }
  
  @Deployment
  public void testNestedSimpleSubprocessWithTimerOnInnerSubProcess() {
    Date startTime = new Date();
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSubProcessWithTimer");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and fire up the job executor 
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ( 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);

    // The inner subprocess should be destoyed, and the escalated task should be active
    Task escalationTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Escalated task", escalationTask.getName());
    
    // Completing the escalated task, destroys the outer scope and activates the task after the subprocess
    taskService.complete(escalationTask.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcess.getName());
  }
  
  /**
   * Test case where the simple sub process of previous test cases 
   * is nested within two other sub processes
   */
  @Deployment
  public void testDoubleNestedSimpleSubProcess() {
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // both subprocesses are destroyed and the task after the subprocess should be active
    taskService.complete(subProcessTask.getId());
    Task taskAfterSubProcesses = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
  }
  
  @Deployment
  public void testSimpleParallelSubProcess() {
    
    // After starting the process, the two task in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleParallelSubProcess");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME).list();
    
    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());
    
    // Completing both tasks, should destroiy the subprocess and activate the task after the subprocess
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());
    Task taskAfterSubProcess = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after sub process", taskAfterSubProcess.getName());
  }
  
  @Deployment
  public void testSimpleParallelSubProcessWithTimer() {
    
    Date startTime = new Date();
    
    // After staring the process, the tasks in the subprocess should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleParallelSubProcessWithTimer");
    List<Task> subProcessTasks = taskService.createTaskQuery().processInstanceId(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME).list();
    
    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());
    
    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and fire up the job executor 
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + ( 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 5L);

    // The inner subprocess should be destoyed, and the tsk after the timer should be active
    Task taskAfterTimer = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertEquals("Task after timer", taskAfterTimer.getName());

    // Completing the task after the timer ends the process instance
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testTwoSubProcessInParallel() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallel");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = taskQuery.list();
    
    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task in subprocess A", taskA.getName());
    assertEquals("Task in subprocess B", taskB.getName());
    
    // Completing both tasks should active the tasks outside the subprocesses
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());
    tasks = taskQuery.list();
    
    taskA = tasks.get(0);
    taskB = tasks.get(1);
    assertEquals("Task after subprocess A", taskA.getName());
    assertEquals("Task after subprocess B", taskB.getName());
    
    // Completing these tasks should end the process
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = taskQuery.list();
    
    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task in subprocess A", taskA.getName());
    assertEquals("Task in subprocess B", taskB.getName());
    
    // Completing both tasks should active the tasks outside the subprocesses
    taskService.complete(taskA.getId());
    taskService.complete(taskB.getId());
    
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());
    
    // Completing this task should end the process
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testTwoNestedSubProcessesInParallelWithTimer() {
    
    Date startTime = new Date();
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nestedParallelSubProcessesWithTimer");
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .processInstanceId(pi.getId())
      .orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = taskQuery.list();
    
    // After process start, both tasks in the subprocesses should be active
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task in subprocess A", taskA.getName());
    assertEquals("Task in subprocess B", taskB.getName());
    
    // Firing the timer should destroy all three subprocesses and activate the task after the timer
    ClockUtil.setCurrentTime(new Date(startTime.getTime() + (2 * 60 * 60 * 1000 ) + 1000));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    Task taskAfterTimer = taskQuery.singleResult();
    assertEquals("Task after timer", taskAfterTimer.getName());
    
    // Completing the task should end the process instance
    taskService.complete(taskAfterTimer.getId());
    assertProcessEnded(pi.getId());
  }

}
