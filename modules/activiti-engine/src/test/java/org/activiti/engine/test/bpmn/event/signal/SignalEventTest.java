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

import java.util.Date;
import java.util.HashMap;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
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
        fail("different exception expected");
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
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }
  
}
