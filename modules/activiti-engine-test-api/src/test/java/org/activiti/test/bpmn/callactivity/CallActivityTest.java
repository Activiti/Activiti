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

package org.activiti.test.bpmn.callactivity;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ProcessInstance;
import org.activiti.engine.ProcessService;
import org.activiti.engine.Task;
import org.activiti.engine.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.activiti.impl.time.Clock;
import org.activiti.test.JobExecutorPoller;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Joram Barrez
 */
public class CallActivityTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();
  
  @Test
  @Deployment(resources = {"CallActivity.testCallSimpleSubProcess.bpmn20.xml", "simpleSubProcess.bpmn20.xml"})
  public void testCallSimpleSubProcess() {
    ProcessInstance processInstance = deployer.getProcessService().startProcessInstanceByKey("callSimpleSubProcess");
    
    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = deployer.getTaskService().createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    
    // Completing the task continues the process which leads to calling the subprocess
    deployer.getTaskService().complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    
    // Completing the task in the subprocess, finishes the subprocess
    deployer.getTaskService().complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());
    
    // Completing this task end the process instance
    deployer.getTaskService().complete(taskAfterSubProcess.getId());
    deployer.assertProcessEnded(processInstance.getId());
  }
  
  /**
   * Test case for a possible tricky case: reaching the end event
   * of the subprocess leads to an end event in the super process instance.
   */
  @Test
  @Deployment(resources = {"CallActivity.testSubProcessEndsSuperProcess.bpmn20.xml", "simpleSubProcess.bpmn20.xml"})
  public void testSubProcessEndsSuperProcess() {
    ProcessService processService = deployer.getProcessService();
    ProcessInstance processInstance = processService.startProcessInstanceByKey("subProcessEndsSuperProcess");
    
    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = deployer.getTaskService().createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());
    
    // Completing this task ends the subprocess which leads to the end of the whole process instance
    deployer.getTaskService().complete(taskBeforeSubProcess.getId());
    deployer.assertProcessEnded(processInstance.getId());
    assertEquals(0, processService.createProcessInstanceQuery().list().size());
  }
  
  @Test
  @Deployment(resources = {"CallActivity.testCallParallelSubProcess.bpmn20.xml", "simpleParallelSubProcess.bpmn20.xml"})
  public void testCallParallelSubProcess() {
    deployer.getProcessService().startProcessInstanceByKey("callParallelSubProcess");
  
    // The two tasks in the parallel subprocess should be active
    TaskQuery taskQuery = deployer.getTaskService()
      .createTaskQuery()
      .orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());
    
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());
    
    // Completing the first task should not end the subprocess
    deployer.getTaskService().complete(taskA.getId());
    assertEquals(1, taskQuery.list().size());
    assertEquals(2, deployer.getProcessService().createProcessInstanceQuery().list().size());
    
    // Completing the second task should end the subprocess and end the whole process instance
    deployer.getTaskService().complete(taskB.getId());
    assertEquals(0, deployer.getProcessService().createProcessInstanceQuery().list().size());
  }
  
  @Test
  @Deployment(resources = {"CallActivity.testTimerOnCallActivity.bpmn20.xml", "simpleSubProcess.bpmn20.xml"})
  public void testTimerOnCallActivity() {
    Date startTime = Clock.getCurrentTime();
    
    // After process start, the task in the subprocess should be active
    deployer.getProcessService().startProcessInstanceByKey("timerOnCallActivity");
    TaskQuery taskQuery = deployer.getTaskService().createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    
    // When the timer on the subprocess is fired, the complete subprocess is destroyed
    Clock.setCurrentTime(new Date(startTime.getTime() + (6 * 60 * 1000))); // + 6 minutes, timer fires on 5 minutes
    new JobExecutorPoller(deployer.getJobExecutor(), deployer.getCommandExecutor()).waitForJobExecutorToProcessAllJobs(5000L, 25L);
    
    Task escalatedTask = taskQuery.singleResult();
    assertEquals("Escalated Task", escalatedTask.getName());
    
    // Completing the task ends the complete process
    deployer.getTaskService().complete(escalatedTask.getId());
    assertEquals(0, deployer.getProcessService().createProcessInstanceQuery().list().size());
  }
  
}
