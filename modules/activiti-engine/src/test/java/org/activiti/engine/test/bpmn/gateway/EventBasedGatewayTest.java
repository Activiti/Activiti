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

package org.activiti.engine.test.bpmn.gateway;

import java.util.Date;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class EventBasedGatewayTest extends PluggableActivitiTestCase {
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml",
          "org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.throwAlertSignal.bpmn20.xml"})
  public void testCatchSignalCancelsTimer() {
    
    runtimeService.startProcessInstanceByKey("catchSignal");
        
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, managementService.createJobQuery().count());
    
    runtimeService.startProcessInstanceByKey("throwSignal");
    
    assertEquals(0, runtimeService.createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());    
    assertEquals(0, managementService.createJobQuery().count());
    
    Task task = taskService.createTaskQuery()
      .taskName("afterSignal")
      .singleResult();
    
    assertNotNull(task);
    
    taskService.complete(task.getId());
       
  }
    
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testCatchAlertAndTimer.bpmn20.xml"
          })
  public void testCatchTimerCancelsSignal() {
    
    runtimeService.startProcessInstanceByKey("catchSignal");
        
    assertEquals(1, runtimeService.createEventSubscriptionQuery().count());    
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, managementService.createJobQuery().count());
    
    ClockUtil.setCurrentTime(new Date(ClockUtil.getCurrentTime().getTime() +10000));
    try {
      // wait for timer to fire
      waitForJobExecutorToProcessAllJobs(10000, 100);
      
      assertEquals(0, runtimeService.createEventSubscriptionQuery().count());    
      assertEquals(1, runtimeService.createProcessInstanceQuery().count());    
      assertEquals(0, managementService.createJobQuery().count());
      
      Task task = taskService.createTaskQuery()
        .taskName("afterTimer")
        .singleResult();
      
      assertNotNull(task);
      
      taskService.complete(task.getId());
    }finally{
      ClockUtil.setCurrentTime(new Date());
    }
  }

  public void testConnectedToActitiy() {
    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testConnectedToActivity.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Event based gateway can only be connected to elements of type intermediateCatchEvent")) {
        fail("different exception expected");
      }
    }
    
  }
  
  public void testInvalidSequenceFlow() {
    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/gateway/EventBasedGatewayTest.testEventInvalidSequenceFlow.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Invalid incoming sequenceflow for intermediateCatchEvent")) {
        fail("different exception expected");
      }
    }
    
  }

}
