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

package org.activiti.engine.test.bpmn.callactivity;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Nils Preusker
 * @author Bernd Ruecker
 */
public class CallActivityAdvancedTest extends PluggableActivitiTestCase {

  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcess.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" 
  })
  public void testCallSimpleSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    
    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());
    
    // Completing the task continues the process which leads to calling the subprocess
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    
    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());
    
    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(processInstance.getId());
    
    // Validate subprocess history
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
    	// Subprocess should have initial activity set
    	HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
    			.processInstanceId(taskInSubProcess.getProcessInstanceId())
    			.singleResult();
    	assertNotNull(historicProcess);
    	assertEquals("theStart", historicProcess.getStartActivityId());
    	
    	List<HistoricActivityInstance> historicInstances = historyService.createHistoricActivityInstanceQuery()
    			.processInstanceId(taskInSubProcess.getProcessInstanceId())
    			.list();
    	
    	// Should contain a start-event, the task and an end-event
    	assertEquals(3L, historicInstances.size());
      Set<String> expectedActivities = new HashSet<String>(Arrays.asList(new String[] {
      		"theStart",
      		"task",
      		"theEnd"
      }));
      
      for(HistoricActivityInstance act : historicInstances) {
      	expectedActivities.remove(act.getActivityId());
      }
      assertTrue("Not all expected activities were found in the history", expectedActivities.isEmpty());
    }
  }
  
  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
  "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testCallSimpleSubProcessWithExpressions() {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    // one task in the subprocess should be active after starting the process
    // instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

    // Completing the task continues the process which leads to calling the
    // subprocess. The sub process we want to call is passed in as a variable
    // into this task
    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
    taskService.complete(taskBeforeSubProcess.getId());
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());

    // Completing the task in the subprocess, finishes the subprocess
    taskService.complete(taskInSubProcess.getId());
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

    // Completing this task end the process instance
    taskService.complete(taskAfterSubProcess.getId());
    assertProcessEnded(processInstance.getId());
  }
  
  /**
   * Test case for a possible tricky case: reaching the end event
   * of the subprocess leads to an end event in the super process instance.
   */
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testSubProcessEndsSuperProcess.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testSubProcessEndsSuperProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEndsSuperProcess");
    
    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());
    
    // Completing this task ends the subprocess which leads to the end of the whole process instance
    taskService.complete(taskBeforeSubProcess.getId());
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }
  
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallParallelSubProcess.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/simpleParallelSubProcess.bpmn20.xml"})
  public void testCallParallelSubProcess() {
    runtimeService.startProcessInstanceByKey("callParallelSubProcess");
  
    // The two tasks in the parallel subprocess should be active
    TaskQuery taskQuery = taskService
      .createTaskQuery()
      .orderByTaskName()
      .asc();
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());
    
    Task taskA = tasks.get(0);
    Task taskB = tasks.get(1);
    assertEquals("Task A", taskA.getName());
    assertEquals("Task B", taskB.getName());

    // Completing the first task should not end the subprocess
    taskService.complete(taskA.getId());
    assertEquals(1, taskQuery.list().size());
    
    // Completing the second task should end the subprocess and end the whole process instance
    taskService.complete(taskB.getId());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSequentialSubProcess.bpmn20.xml",
		  "org/activiti/engine/test/bpmn/callactivity/CallActivity.testCallSimpleSubProcessWithExpressions.bpmn20.xml",
		  "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml",
  	      "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess2.bpmn20.xml"})
  public void testCallSequentialSubProcessWithExpressions() {

	    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSequentialSubProcess");

	    // FIRST sub process calls simpleSubProcess
	    
	    // one task in the subprocess should be active after starting the process
	    // instance
	    TaskQuery taskQuery = taskService.createTaskQuery();
	    Task taskBeforeSubProcess = taskQuery.singleResult();
	    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

	    // Completing the task continues the process which leads to calling the
	    // subprocess. The sub process we want to call is passed in as a variable
	    // into this task
	    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess");
	    taskService.complete(taskBeforeSubProcess.getId());
	    Task taskInSubProcess = taskQuery.singleResult();
	    assertEquals("Task in subprocess", taskInSubProcess.getName());

	    // Completing the task in the subprocess, finishes the subprocess
	    taskService.complete(taskInSubProcess.getId());
	    Task taskAfterSubProcess = taskQuery.singleResult();
	    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

	    // Completing this task end the process instance
	    taskService.complete(taskAfterSubProcess.getId());
	    
	    
	    // SECOND sub process calls simpleSubProcess2
	    
	    // one task in the subprocess should be active after starting the process
	    // instance
	    taskQuery = taskService.createTaskQuery();
	    taskBeforeSubProcess = taskQuery.singleResult();
	    assertEquals("Task before subprocess", taskBeforeSubProcess.getName());

	    // Completing the task continues the process which leads to calling the
	    // subprocess. The sub process we want to call is passed in as a variable
	    // into this task
	    taskService.setVariable(taskBeforeSubProcess.getId(), "simpleSubProcessExpression", "simpleSubProcess2");
	    taskService.complete(taskBeforeSubProcess.getId());
	    taskInSubProcess = taskQuery.singleResult();
	    assertEquals("Task in subprocess 2", taskInSubProcess.getName());

	    // Completing the task in the subprocess, finishes the subprocess
	    taskService.complete(taskInSubProcess.getId());
	    taskAfterSubProcess = taskQuery.singleResult();
	    assertEquals("Task after subprocess", taskAfterSubProcess.getName());

	    // Completing this task end the process instance
	    taskService.complete(taskAfterSubProcess.getId());
	    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testTimerOnCallActivity.bpmn20.xml",
    "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"})
  public void testTimerOnCallActivity() {
    Date startTime = processEngineConfiguration.getClock().getCurrentTime();
    
    // After process start, the task in the subprocess should be active
    runtimeService.startProcessInstanceByKey("timerOnCallActivity");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskInSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSubProcess.getName());
    
    // When the timer on the subprocess is fired, the complete subprocess is destroyed
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (6 * 60 * 1000))); // + 6 minutes, timer fires on 5 minutes
    waitForJobExecutorToProcessAllJobs(10000, 5000L);
    
    Task escalatedTask = taskQuery.singleResult();
    assertEquals("Escalated Task", escalatedTask.getName());
    
    // Completing the task ends the complete process
    taskService.complete(escalatedTask.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }
  
  /**
   * Test case for handing over process variables to a sub process
   */
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testSubProcessDataInputOutput.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testSubProcessWithDataInputOutput() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("superVariable", "Hello from the super process.");    
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessDataInputOutput", vars);
    
    // one task in the subprocess should be active after starting the process instance
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task taskBeforeSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskBeforeSubProcess.getName());
    assertEquals("Hello from the super process.", runtimeService.getVariable(taskBeforeSubProcess.getProcessInstanceId(), "subVariable"));
    assertEquals("Hello from the super process.", taskService.getVariable(taskBeforeSubProcess.getId(), "subVariable"));
    
    runtimeService.setVariable(taskBeforeSubProcess.getProcessInstanceId(), "subVariable", "Hello from sub process.");

    // super variable is unchanged
    assertEquals("Hello from the super process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
        
    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskBeforeSubProcess.getId());

    // one task in the subprocess should be active after starting the process instance
    Task taskAfterSubProcess = taskQuery.singleResult();
    assertEquals("Task in super process", taskAfterSubProcess.getName());
    assertEquals("Hello from sub process.", runtimeService.getVariable(processInstance.getId(), "superVariable"));
    assertEquals("Hello from sub process.", taskService.getVariable(taskAfterSubProcess.getId(), "superVariable"));

    vars.clear();
    vars.put("x", 5l);

    // Completing this task ends the super process which leads to a task in the super process
    taskService.complete(taskAfterSubProcess.getId(), vars);
    
    // now we are the second time in the sub process but passed variables via expressions
    Task taskInSecondSubProcess = taskQuery.singleResult();
    assertEquals("Task in subprocess", taskInSecondSubProcess.getName());
    assertEquals(10l, runtimeService.getVariable(taskInSecondSubProcess.getProcessInstanceId(), "y"));
    assertEquals(10l, taskService.getVariable(taskInSecondSubProcess.getId(), "y"));

    // Completing this task ends the subprocess which leads to a task in the super process
    taskService.complete(taskInSecondSubProcess.getId());

    // one task in the subprocess should be active after starting the process instance
    Task taskAfterSecondSubProcess = taskQuery.singleResult();
    assertEquals("Task in super process", taskAfterSecondSubProcess.getName());
    assertEquals(15l, runtimeService.getVariable(taskAfterSecondSubProcess.getProcessInstanceId(), "z"));
    assertEquals(15l, taskService.getVariable(taskAfterSecondSubProcess.getId(), "z"));

    // and end last task in Super process
    taskService.complete(taskAfterSecondSubProcess.getId());

    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createExecutionQuery().list().size());
  }
  
  /**
   * Test case for deleting a sub process
   */
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/CallActivity.testTwoSubProcesses.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml" })
  public void testTwoSubProcesses() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callTwoSubProcesses");
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(3, instanceList.size());
    
    List<Task> taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(2, taskList.size());
    
    runtimeService.deleteProcessInstance(processInstance.getId(), "Test cascading");
    
    instanceList = runtimeService.createProcessInstanceQuery().list();
    assertNotNull(instanceList);
    assertEquals(0, instanceList.size());
    
    taskList = taskService.createTaskQuery().list();
    assertNotNull(taskList);
    assertEquals(0, taskList.size());
  }
  
  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/callactivity/CallActivity.testStartUserIdSetWhenLooping.bpmn20.xml",
      "org/activiti/engine/test/bpmn/callactivity/simpleSubProcess.bpmn20.xml"
  })
  public void testStartUserIdSetWhenLooping() {
    identityService.setAuthenticatedUserId("kermit");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("loopingCallActivity", CollectionUtil.singletonMap("input", 0));
    for (int i=1; i<4; i++) {
      Task task = taskService.createTaskQuery().singleResult();
      assertEquals("Task in subprocess", task.getName());
      identityService.setAuthenticatedUserId("kermit");
      taskService.complete(task.getId(), CollectionUtil.singletonMap("input", i));
    }
    identityService.setAuthenticatedUserId(null);
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Final task", task.getName());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
          .superProcessInstanceId(processInstance.getId()).list();
      assertEquals(3, historicProcessInstances.size());
      for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
        assertNotNull(historicProcessInstance.getStartUserId());
        assertNotNull(historicProcessInstance.getStartTime());
        assertNotNull(historicProcessInstance.getEndTime());
      }
    }
  }
  
  /**
   * Test case for sub process with DataObject
   */
  @Deployment(resources = {
    "org/activiti/engine/test/bpmn/callactivity/DataObject.fatherProcess.bpmn20.xml", 
    "org/activiti/engine/test/bpmn/callactivity/DataObject.subProcess.bpmn20.xml" })
  public void testDataObject() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("DataObject_fatherProcess");
    assertNotNull(processInstance);
  }
    
}
