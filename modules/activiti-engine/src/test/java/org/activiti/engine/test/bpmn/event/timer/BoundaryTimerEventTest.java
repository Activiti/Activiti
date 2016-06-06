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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class BoundaryTimerEventTest extends PluggableActivitiTestCase {
  
  private static boolean listenerExecutedStartEvent = false;
  private static boolean listenerExecutedEndEvent = false;
  
  public static class MyExecutionListener implements ExecutionListener {
    private static final long serialVersionUID = 1L;

    public void notify(DelegateExecution execution) throws Exception {
      if ("end".equals(execution.getEventName())) {
        listenerExecutedEndEvent = true;
      } else if ("start".equals(execution.getEventName())) {
        listenerExecutedStartEvent = true;
      }
    }    
  }
  
  /*
   * Test for when multiple boundary timer events are defined on the same user
   * task
   * 
   * Configuration: - timer 1 -> 2 hours -> secondTask - timer 2 -> 1 hour ->
   * thirdTask - timer 3 -> 3 hours -> fourthTask
   * 
   * See process image next to the process xml resource
   */
  @Deployment
  public void testMultipleTimersOnUserTask() {

    // Set the clock fixed
    Date startTime = new Date();

    // After process start, there should be 3 timers created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("multipleTimersOnUserTask");
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(3, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    assertEquals(0L, jobQuery.count());

    // which means that the third task is reached
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Third Task", task.getName());
  }
  
  @Deployment
  public void testTimerOnNestingOfSubprocesses() {
    
    Date testStartTime = processEngineConfiguration.getClock().getCurrentTime();
    
    runtimeService.startProcessInstanceByKey("timerOnNestedSubprocesses");
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("Inner subprocess task 1", tasks.get(0).getName());
    assertEquals("Inner subprocess task 2", tasks.get(1).getName());
    
    // Timer will fire in 2 hours
    processEngineConfiguration.getClock().setCurrentTime(new Date(testStartTime.getTime() + ((2 * 60 * 60 * 1000) + 5000)));
    Job timer = managementService.createJobQuery().timers().singleResult();
    managementService.executeJob(timer.getId());
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("task outside subprocess", task.getName());
  }
  
  @Deployment
  public void testExpressionOnTimer(){
    // Set the clock fixed
    Date startTime = new Date();
    
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duration", "PT1H");
    
    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testExpressionOnTimer", variables);

    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(1, jobs.size());

    // After setting the clock to time '1 hour and 5 seconds', the second timer should fire
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L, 25L);
    assertEquals(0L, jobQuery.count());
    
    // start execution listener is not executed
    assertFalse(listenerExecutedStartEvent);
    assertTrue(listenerExecutedEndEvent);

    // which means the process has ended
    assertProcessEnded(pi.getId());
  }
  

  @Deployment
  public void testNullExpressionOnTimer(){
	  
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("duration", null);
    
    // After process start, there should be a timer created
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testNullExpressionOnTimer", variables);

    //NO job scheduled as null expression set
    JobQuery jobQuery = managementService.createJobQuery().processInstanceId(pi.getId());
    List<Job> jobs = jobQuery.list();
    assertEquals(0, jobs.size());

    // which means the process is still running waiting for human task input.
    ProcessInstance processInstance = processEngine
    	      .getRuntimeService()
    	      .createProcessInstanceQuery()
    	      .processInstanceId(pi.getId())
    	      .singleResult();
    assertNotNull(processInstance);
  }
  
  
  @Deployment
  public void testTimerInSingleTransactionProcess() {
    // make sure that if a PI completes in single transaction, JobEntities associated with the execution are deleted.
    // broken before 5.10, see ACT-1133
    runtimeService.startProcessInstanceByKey("timerOnSubprocesses"); 
    assertEquals(0, managementService.createJobQuery().count());
  }
  
  @Deployment
  public void testRepeatingTimerWithCancelActivity() {
    runtimeService.startProcessInstanceByKey("repeatingTimerAndCallActivity");
    assertEquals(1, managementService.createJobQuery().count());
    assertEquals(1, taskService.createTaskQuery().count());
    
    // Firing job should cancel the user task, destroy the scope,
    // re-enter the task and recreate the task. A new timer should also be created.
    // This didn't happen before 5.11 (new jobs kept being created). See ACT-1427
    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());
    assertEquals(1, managementService.createJobQuery().count());
    assertEquals(1, taskService.createTaskQuery().count());
  }
  
  @Deployment
	public void testInfiniteRepeatingTimer() throws Exception {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
		Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
		processEngineConfiguration.getClock().setCurrentTime(currentTime);
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("timerString", "R/2015-10-01T11:00:00/PT24H");
		runtimeService.startProcessInstanceByKey("testTimerErrors", vars);
		
		long twentyFourHours = 24L * 60L * 60L * 1000L; 

		Date previousDueDate = null;
		
		// Move clock, job should fire
		for (int i=0; i<30; i++) {
			Job job = managementService.createJobQuery().singleResult();
			
			// Verify due date
			if (previousDueDate != null) {
				assertTrue(job.getDuedate().getTime() - previousDueDate.getTime() >= twentyFourHours);
			}
			previousDueDate = job.getDuedate();
			
			currentTime = new Date(currentTime.getTime() + twentyFourHours + (60 * 1000));
			processEngineConfiguration.getClock().setCurrentTime(currentTime);
			managementService.executeJob(managementService.createJobQuery().executable().singleResult().getId());
		}
		
	}

  @Deployment
  public void testRepeatTimerDuration() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    runtimeService.startProcessInstanceByKey("repeattimertest");

    long twentyFourHours = 24L * 60L * 60L * 1000L; 

    Date previousDueDate = null;

    // Move clock, job should fire
    for (int i = 0; i < 3; i++) {
      Job job = managementService.createJobQuery().singleResult();

      // Verify due date
      if (previousDueDate != null) {
        assertTrue(job.getDuedate().getTime() - previousDueDate.getTime() >= twentyFourHours);
      }
      previousDueDate = job.getDuedate();

      currentTime = new Date(currentTime.getTime() + twentyFourHours + (60 * 1000));
      processEngineConfiguration.getClock().setCurrentTime(currentTime);
      managementService.executeJob(job.getId());
    }

  }
  
  @Deployment
  public void testBoundaryTimerEvent() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("patient","kermit");
    runtimeService.startProcessInstanceByKey("process1", vars);

    // just wait for 2 seconds to run any job if it's the case
    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    // there should be a userTask waiting for user input
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1,tasks.size());
    assertEquals("First Task",tasks.get(0).getName());
    List<Job> jobList = managementService.createJobQuery().list();
    assertEquals(1,jobList.size());


    // let's see what's happening after 2 minutes
    // nothing should change since the timer have to executed after 10 minutes
    long twoMinutes = 2L * 60L * 1000L;

    currentTime = new Date(currentTime.getTime() + twoMinutes +  1000L);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    tasks = taskService.createTaskQuery().list();
    assertEquals(1,tasks.size());
    assertEquals("First Task",tasks.get(0).getName());
    jobList = managementService.createJobQuery().list();
    assertEquals(1,jobList.size());


    // after another 8 minutes (the timer will have to execute because it wasa set to be executed @ 10 minutes after process start)
    long tenMinutes = 8L * 60L * 1000L;
    currentTime = new Date(currentTime.getTime() + tenMinutes);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      ex.getCause();
      //expected exception because a new job is prepared
    }

    // there should be only one userTask and it should be the one triggered by the boundary timer event.
    // after the boundary event is triggered there should be no active job.
    tasks = taskService.createTaskQuery().list();
    assertEquals(1,tasks.size());
    assertEquals("Second Task",tasks.get(0).getName());
    jobList = managementService.createJobQuery().list();
    assertEquals(0,jobList.size());
  }


  @Deployment
  public void testBoundaryTimerEvent2() throws Exception {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyy.MM.dd hh:mm");
    Date currentTime = simpleDateFormat.parse("2015.10.01 11:01");
    processEngineConfiguration.getClock().setCurrentTime(currentTime);


    runtimeService.startProcessInstanceByKey("timerprocess");

    // just wait for 2 seconds to run any job if it's the case
    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      //expected exception because the boundary timer event created a timer job to be executed after 10 minutes
    }

    // there should be a userTask waiting for user input
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1,tasks.size());
    assertEquals("Start",tasks.get(0).getName());
    List<Job> jobList = managementService.createJobQuery().list();
    assertEquals(1,jobList.size());


    // after another 2 minutes
    long tenMinutes = 2L * 60L * 1000L;
    currentTime = new Date(currentTime.getTime() + tenMinutes);
    processEngineConfiguration.getClock().setCurrentTime(currentTime);

    try {
      waitForJobExecutorToProcessAllJobs(2000, 200);
    } catch (Exception ex) {
      ex.getCause();
      //expected exception because a new job is prepared
    }

    // there should be no userTask
    tasks = taskService.createTaskQuery().list();
    assertEquals(0,tasks.size());
    jobList = managementService.createJobQuery().list();
    assertEquals(0,jobList.size());
  }

}
