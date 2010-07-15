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

package org.activiti.test.bpmn.subprocess;

import java.util.Date;
import java.util.List;

import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.impl.time.Clock;
import org.activiti.test.JobExecutorPoller;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author Joram Barrez
 */
public class SubProcessTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();
  
  @Test
  @ProcessDeclared
  public void testSimpleSubProcess() {
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = deployer.getTaskService().createTaskQuery()
                                                   .processInstance(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // the subprocess scope is destroyed and the complete process ends
    deployer.getTaskService().complete(subProcessTask.getId());
    assertNull(deployer.getProcessService().findProcessInstanceById(pi.getId()));
  }
  
  /**
   * Same test case as before, but now with all automatic steps
   */
  @Test
  @ProcessDeclared
  public void testSimpleAutomaticSubProcess() {
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("simpleSubProcessAutomatic");
    assertTrue(pi.isEnded());
    deployer.assertProcessEnded(pi.getId());
  }
  
  @Test
  @ProcessDeclared
  public void testSimpleSubProcessWithTimer() {
    
    Clock.setCurrentTime(new Date(0L));
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("simpleSubProcess");
    Task subProcessTask = deployer.getTaskService().createTaskQuery()
                                                   .processInstance(pi.getId())
                                                   .singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // Setting the clock forward 2 hours 1 second (timer fires in 2 hours) and fire up the job executor 
    Clock.setCurrentTime(new Date((2 * 60 * 60 * 1000 ) + 1000));
    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(5000L, 25L);

    // The subprocess should be left, and the escalated task should be active
    Task escalationTask = deployer.getTaskService().createTaskQuery()
                                                   .processInstance(pi.getId())
                                                   .singleResult();
    assertEquals("Fix escalated problem", escalationTask.getName());
  }
  
  /**
   * Test case where the simple sub process of previous test cases
   * is nested within another subprocess.
   */
  @Test
  @ProcessDeclared
  public void testNestedSimpleSubProcess() {
    
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // both subprocesses are destroyed and the task after the subprocess should be active
    deployer.getTaskService().complete(subProcessTask.getId());
    Task taskAfterSubProcesses = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testNestedSimpleSubprocessWithTimerOnInnerSubProcess() {
    Clock.setCurrentTime(new Date(0L));
    
    // After staring the process, the task in the subprocess should be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("nestedSubProcessWithTimer");
    Task subProcessTask = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and fire up the job executor 
    Clock.setCurrentTime(new Date(( 60 * 60 * 1000 ) + 1000));
    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(5000L, 25L);

    // The inner subprocess should be destoyed, and the escalated task should be active
    Task escalationTask = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Escalated task", escalationTask.getName());
    
    // Completing the escalated task, destroys the outer scope and activates the task after the subprocess
    deployer.getTaskService().complete(escalationTask.getId());
    Task taskAfterSubProcess = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcess.getName());
  }
  
  /**
   * Test case where the simple sub process of previous test cases 
   * is nested within two other sub processes
   */
  @Test
  @ProcessDeclared
  public void testDoubleNestedSimpleSubProcess() {
    // After staring the process, the task in the inner subprocess must be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("nestedSimpleSubProcess");
    Task subProcessTask = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task in subprocess", subProcessTask.getName());
    
    // After completing the task in the subprocess, 
    // both subprocesses are destroyed and the task after the subprocess should be active
    deployer.getTaskService().complete(subProcessTask.getId());
    Task taskAfterSubProcesses = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task after subprocesses", taskAfterSubProcesses.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testSimpleParallelSubProcess() {
    
    // After starting the process, the two task in the subprocess should be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("simpleParallelSubProcess");
    List<Task> subProcessTasks = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME).list();
    
    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());
    
    // Completing both tasks, should destroiy the subprocess and activate the task after the subprocess
    deployer.getTaskService().complete(taskA.getId());
    deployer.getTaskService().complete(taskB.getId());
    Task taskAfterSubProcess = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task after sub process", taskAfterSubProcess.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testSimpleParallelSubProcessWithTimer() {
    
    Clock.setCurrentTime(new Date(0L));
    
    // After staring the process, the tasks in the subprocess should be active
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("simpleParallelSubProcessWithTimer");
    List<Task> subProcessTasks = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME).list();
    
    // Tasks are ordered by name (see query)
    Task taskA = subProcessTasks.get(0);
    Task taskB = subProcessTasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());
    
    // Setting the clock forward 1 hour 1 second (timer fires in 1 hour) and fire up the job executor 
    Clock.setCurrentTime(new Date(( 60 * 60 * 1000 ) + 1000));
    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(5000L, 25L);

    // The inner subprocess should be destoyed, and the tsk after the timer should be active
    Task taskAfterTimer = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertEquals("Task after timer", taskAfterTimer.getName());

    // Completing the task after the timer ends the process instance
    deployer.getTaskService().complete(taskAfterTimer.getId());
    deployer.assertProcessEnded(pi.getId());
  }

}
