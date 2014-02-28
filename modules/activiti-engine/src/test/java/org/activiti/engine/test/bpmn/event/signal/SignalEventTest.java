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

package org.activiti.engine.test.bpmn.event.signal;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class SignalEventTest extends PluggableActivitiTestCase {
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchIntermediate() {
    
    runtimeService.startProcessInstanceByKey("catchSignal");
        
    assertEquals(1, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    runtimeService.startProcessInstanceByKey("throwSignal");
    
    assertEquals(0, createEventSubscriptionQuery().count());    
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
   
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundary.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchBoundary() {
    runtimeService.startProcessInstanceByKey("catchSignal");
        
    assertEquals(1, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    runtimeService.startProcessInstanceByKey("throwSignal");
    
    assertEquals(0, createEventSubscriptionQuery().count());    
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());   
  }

  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignalBoundaryWithReceiveTask.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml"})
  public void testSignalCatchBoundaryWithVariables() {
    HashMap<String, Object> variables1 = new HashMap<String, Object>();
    variables1.put("processName", "catchSignal");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchSignal", variables1);
        
    HashMap<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("processName", "throwSignal");
    runtimeService.startProcessInstanceByKey("throwSignal", variables2);
    
    assertEquals("catchSignal", runtimeService.getVariable(pi.getId(), "processName"));
  }

  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignalAsynch.bpmn20.xml"})
  public void testSignalCatchIntermediateAsynch() {
    
    runtimeService.startProcessInstanceByKey("catchSignal");
        
    assertEquals(1, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    runtimeService.startProcessInstanceByKey("throwSignal");
    
    assertEquals(1, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    // there is a job:
    assertEquals(1, managementService.createJobQuery().count()); 
    
    try {
      ClockUtil.setCurrentTime( new Date(System.currentTimeMillis() + 1000));
      waitForJobExecutorToProcessAllJobs(10000, 100l);
      
      assertEquals(0, createEventSubscriptionQuery().count());    
      assertEquals(0, runtimeService.createProcessInstanceQuery().count());
      assertEquals(0, managementService.createJobQuery().count());   
    }finally {
     ClockUtil.setCurrentTime(new Date()); 
    }
   
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.catchMultipleSignals.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/signal/SignalEventTests.throwAbortSignal.bpmn20.xml"})
  public void testSignalCatchDifferentSignals() {
    
    runtimeService.startProcessInstanceByKey("catchSignal");
    
    assertEquals(2, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    runtimeService.startProcessInstanceByKey("throwAbort");
    
    assertEquals(1, createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());    

    Task taskAfterAbort = taskService.createTaskQuery().taskAssignee("gonzo").singleResult();
    assertNotNull(taskAfterAbort);
    taskService.complete(taskAfterAbort.getId());
    
    runtimeService.startProcessInstanceByKey("throwSignal");
    
    assertEquals(0, createEventSubscriptionQuery().count());    
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  /**
   * Verifies the solution of https://jira.codehaus.org/browse/ACT-1309
   */
  @Deployment
  public void testSignalBoundaryOnSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventOnSubprocess");
    runtimeService.signalEventReceived("stopSignal");    
    assertProcessEnded(pi.getProcessInstanceId());
  }

  public void testDuplicateSignalNames() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.duplicateSignalNames.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("duplicate signal name")) {
        fail("different exception expected");
      }
    }    
  }
  
  public void testNoSignalName() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.noSignalName.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("has no name")) {
        fail("different exception expected, was " + e.getMessage());
      }
    }    
  }
  
  public void testSignalNoId() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.signalNoId.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("signal must have an id")) {
        fail("different exception expected");
      }
    }    
  }
  
  public void testSignalNoRef() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/signal/SignalEventTests.signalNoRef.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("signalEventDefinition does not have required property 'signalRef'")) {
        fail("different exception expected");
      }
    }    
  }
  
  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
  }
  
  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignal() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalEvent");
    
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());
    Task currentTask = tasks.get(0);
    assertEquals("My User Task", currentTask.getName());
    
    runtimeService.signalEventReceived("alert");
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2,  tasks.size());
    
    for (Task task : tasks) {
      if (!task.getName().equals("My User Task") && !task.getName().equals("My Second User Task")) {
        fail("Expected: <My User Task> or <My Second User Task> but was <" + task.getName() + ">.");
      }
    }
    
    taskService.complete(taskService.createTaskQuery().taskName("My User Task").singleResult().getId());
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());
    currentTask = tasks.get(0);
    assertEquals("My Second User Task", currentTask.getName());
  }
  
  
  /**
   * TestCase to reproduce Issue ACT-1344
   */
  @Deployment
  public void testNonInterruptingSignalWithSubProcess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("nonInterruptingSignalWithSubProcess");
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1,  tasks.size());
    
    Task currentTask = tasks.get(0);
    assertEquals("Approve", currentTask.getName());
    
    runtimeService.signalEventReceived("alert");
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(2, tasks.size());
    
    for (Task task : tasks) {
      if (!task.getName().equals("Approve") && !task.getName().equals("Review")) {
        fail("Expected: <Approve> or <Review> but was <" + task.getName() + ">.");
      }
    }
    
    taskService.complete(taskService.createTaskQuery().taskName("Approve").singleResult().getId());
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());

    currentTask = tasks.get(0);
    assertEquals("Review", currentTask.getName());
    
    taskService.complete(taskService.createTaskQuery().taskName("Review").singleResult().getId());
    
    tasks = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
    assertEquals(1, tasks.size());
  }
  
  @Deployment
  public void testUseSignalForExceptionsBetweenParallelPaths() {
    runtimeService.startProcessInstanceByKey("processWithSignal");
    
    // First task should be to select the developers
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Enter developers", task.getName());
    taskService.complete(task.getId(), CollectionUtil.singletonMap("developers", Arrays.asList("developerOne", "developerTwo", "developerThree")));
    
    // Should be three distinct tasks for each developer
    assertEquals("Develop specifications", taskService.createTaskQuery().taskAssignee("developerOne").singleResult().getName());
    assertEquals("Develop specifications", taskService.createTaskQuery().taskAssignee("developerTwo").singleResult().getName());
    assertEquals("Develop specifications", taskService.createTaskQuery().taskAssignee("developerThree").singleResult().getName());
    
    // Negotiate with client is a task for kermit
    task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
    assertEquals("Negotiate with client", task.getName());
    
    // When the kermit task is completed, it throws a signal which should cancel the multi instance
    taskService.complete(task.getId(), CollectionUtil.singletonMap("negotationFailed", true));
    
    // No tasks should be open then and process should have ended
    assertEquals(0, taskService.createTaskQuery().count());
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment
  public void testSignalWithProcessInstanceScope() {
    // Start the process that catches the signal
    ProcessInstance processInstanceCatch = runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertEquals("userTaskWithSignalCatch", taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName());
    
    // Then start the process that will throw the signal
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");
    
    // Since the signal is process instance scoped, the second process shouldn't have proceeded in any way
    assertEquals("userTaskWithSignalCatch", taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName());
    
    // Let's try to trigger the catch using the API, that should also fail
    runtimeService.signalEventReceived("The Signal");
    assertEquals("userTaskWithSignalCatch", taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName());
  }
  
  @Deployment
  public void testSignalWithGlobalScope() {
    // Start the process that catches the signal
    ProcessInstance processInstanceCatch = runtimeService.startProcessInstanceByKey("processWithSignalCatch");
    assertEquals("userTaskWithSignalCatch", taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName());
    
    // Then start the process that will throw thee signal
    runtimeService.startProcessInstanceByKey("processWithSignalThrow");
    
    // Since the signal is process instance scoped, the second process shouldn't have proceeded in any way
    assertEquals("userTaskAfterSignalCatch", taskService.createTaskQuery().processInstanceId(processInstanceCatch.getId()).singleResult().getName());
  }
  
  @Deployment
  public void testAsyncTriggeredSignalEvent() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("processWithSignalCatch");
		
		assertNotNull(processInstance);
		Execution execution = runtimeService.createExecutionQuery()
			      .processInstanceId(processInstance.getId())
			      .signalEventSubscriptionName("The Signal")
			      .singleResult();
		assertNotNull(execution);
		assertEquals(1, createEventSubscriptionQuery().count());
		assertEquals(2, runtimeService.createExecutionQuery().count());
		
		runtimeService.signalEventReceivedAsync("The Signal", execution.getId());
		
		assertEquals(1, managementService
			      .createJobQuery().messages().count());
		
		waitForJobExecutorToProcessAllJobs(8000L, 200L);
		assertEquals(0, createEventSubscriptionQuery().count());    
	    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
	    assertEquals(0, managementService.createJobQuery().count()); 
  }
  
  @Deployment
  public void testSignalUserTask() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("catchSignal");
    
    Execution execution = runtimeService.createExecutionQuery()
            .processInstanceId(pi.getId())
            .activityId("waitState")
            .singleResult();
    
    assertNotNull(execution);
    
    try {
      runtimeService.signal(execution.getId());
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      // Exception expected
    }
    
  }
  
  @Deployment 
  public void testEarlyFinishedProcess() {	 	
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callerProcess");
		assertNotNull(processInstance.getId());
  }

}
