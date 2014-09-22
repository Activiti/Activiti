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

package org.activiti.engine.test.history;

import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class HistoricActivityInstanceTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testHistoricActivityInstanceNoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");
    
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("noop")
      .singleResult();
    
    assertEquals("noop", historicActivityInstance.getActivityId());
    assertEquals("serviceTask", historicActivityInstance.getActivityType());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
    assertNotNull(historicActivityInstance.getEndTime());
    assertTrue(historicActivityInstance.getDurationInMillis() >= 0);
  }

  @Deployment
  public void testHistoricActivityInstanceReceive() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveProcess");
    
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("receive")
      .singleResult();
    
    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNull(historicActivityInstance.getEndTime());
    assertNull(historicActivityInstance.getDurationInMillis());
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
    
    runtimeService.signal(processInstance.getId());
    
    historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("receive")
      .singleResult();
    
    assertEquals("receive", historicActivityInstance.getActivityId());
    assertEquals("receiveTask", historicActivityInstance.getActivityType());
    assertNotNull(historicActivityInstance.getEndTime());
    assertTrue(historicActivityInstance.getDurationInMillis() >= 0);
    assertNotNull(historicActivityInstance.getProcessDefinitionId());
    assertEquals(processInstance.getId(), historicActivityInstance.getProcessInstanceId());
    assertEquals(processInstance.getId(), historicActivityInstance.getExecutionId());
    assertNotNull(historicActivityInstance.getStartTime());
  }
  
  @Deployment
  public void testHistoricActivityInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityId("nonExistingActivityId").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("noop").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityType("nonExistingActivityType").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityType("serviceTask").list().size());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().activityName("nonExistingActivityName").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityName("No operation").list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().taskAssignee("nonExistingAssignee").list().size());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().executionId("nonExistingExecutionId").list().size());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId("nonExistingProcessInstanceId").list().size());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    }

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId("nonExistingProcessDefinitionId").list().size());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    }
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().unfinished().list().size());

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(3, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    } else {
      assertEquals(0, historyService.createHistoricActivityInstanceQuery().finished().list().size());
    }

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().list().get(0);
      assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityInstanceId(historicActivityInstance.getId()).list().size());
    }
  }
  
  @Deployment
  public void testHistoricActivityInstanceForEventsQuery() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("eventProcess");
    assertEquals(1, taskService.createTaskQuery().count()); 
    runtimeService.signalEventReceived("signal");
    assertProcessEnded(pi.getId());
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("noop").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("userTask").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("start").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("end").list().size());
    
    // TODO: Discuss if boundary events will occur in the log!
//    assertEquals(1, historyService.createHistoricActivityInstanceQuery().activityId("boundaryEvent").list().size());

    HistoricActivityInstance intermediateEvent = historyService.createHistoricActivityInstanceQuery().activityId("intermediate-event").singleResult();
    assertNotNull(intermediateEvent.getStartTime());
    assertNotNull(intermediateEvent.getEndTime());
    
    HistoricActivityInstance startEvent = historyService.createHistoricActivityInstanceQuery().activityId("start").singleResult();
    assertNotNull(startEvent.getStartTime());
    assertNotNull(startEvent.getEndTime());
    
    HistoricActivityInstance endEvent = historyService.createHistoricActivityInstanceQuery().activityId("end").singleResult();
    assertNotNull(endEvent.getStartTime());
    assertNotNull(endEvent.getEndTime());
  }
  
  @Deployment
  public void testHistoricActivityInstanceProperties() {    
    // Start process instance
    runtimeService.startProcessInstanceByKey("taskAssigneeProcess");

    // Get task list
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("theTask")
      .singleResult();
    
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals(task.getId(), historicActivityInstance.getTaskId());
    assertEquals("kermit", historicActivityInstance.getAssignee());
  }
  
  @Deployment(resources = {
          "org/activiti/engine/test/history/calledProcess.bpmn20.xml",
          "org/activiti/engine/test/history/HistoricActivityInstanceTest.testCallSimpleSubProcess.bpmn20.xml"
        })
  public void testHistoricActivityInstanceCalledProcessId() {    
    runtimeService.startProcessInstanceByKey("callSimpleSubProcess");

    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("callSubProcess")
      .singleResult();
    
    HistoricProcessInstance oldInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("calledProcess").singleResult();
    
    assertEquals(oldInstance.getId(), historicActivityInstance.getCalledProcessInstanceId());
  }
  
  @Deployment
  public void testSorting() {
    runtimeService.startProcessInstanceByKey("process");

    int expectedActivityInstances;
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      expectedActivityInstances = 2;
    } else {
      expectedActivityInstances = 0;
    }

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().list().size());

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().list().size());

    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().count());
  
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(expectedActivityInstances, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().count());
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricActivityInstanceQuery().asc().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricActivityInstanceQuery().desc().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }
  
  /**
   * Test to validate fix for ACT-1399: Boundary-event and event-based auditing
   */
  @Deployment
  public void testBoundaryEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("boundaryEventProcess");
    // Complete the task with the boundary-event on it
    Task task = taskService.createTaskQuery()
    		.processInstanceId(processInstance.getId())
    		.singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    assertEquals(0L, runtimeService.createProcessInstanceQuery()
    		.processInstanceId(processInstance.getId()).count());
    
    // Check if there is NO historic activity instance for a boundary-event that has not triggered
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .activityId("boundary")
      .processInstanceId(processInstance.getId())
      .singleResult();
    
    assertNull(historicActivityInstance);
    
    // Now check the history when the boundary-event is fired
    processInstance = runtimeService.startProcessInstanceByKey("boundaryEventProcess");
    
    task = taskService.createTaskQuery()
    		.processInstanceId(processInstance.getId())
    		.singleResult();
    
    runtimeService.signalEventReceived("alert", task.getExecutionId());
    assertEquals(0L, runtimeService.createProcessInstanceQuery()
    		.processInstanceId(processInstance.getId()).count());
    
    historicActivityInstance = historyService
        .createHistoricActivityInstanceQuery()
        .activityId("boundary")
        .processInstanceId(processInstance.getId())
        .singleResult();
      
      assertNotNull(historicActivityInstance);
  }
  
  /**
   * Test to validate fix for ACT-1399: Boundary-event and event-based auditing
   */
  @Deployment
  public void testEventBasedGateway() {
  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("catchSignal");
  	Execution waitingExecution = runtimeService.createExecutionQuery()
  			.signalEventSubscriptionName("alert")
  			.singleResult();
  	assertNotNull(waitingExecution);
  	runtimeService.signalEventReceived("alert", waitingExecution.getId());
  	
  	assertEquals(0L, runtimeService.createProcessInstanceQuery()
  			.processInstanceId(processInstance.getId()).count());
  	
  	HistoricActivityInstance historicActivityInstance = historyService
  			.createHistoricActivityInstanceQuery()
  			.activityId("eventBasedgateway")
  			.processInstanceId(processInstance.getId())
  			.singleResult();
  	
  	assertNotNull(historicActivityInstance);
  }
  
  /**
   * Test to validate fix for ACT-1549: endTime of joining parallel gateway is not set
   */
  @Deployment
  public void testParallelJoinEndTime() {
ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("forkJoin");
  	
  	List<Task> tasksToComplete = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
  	assertEquals(2, tasksToComplete.size());
  	
  	// Complete both tasks, second task-complete should end the fork-gateway and set time
  	taskService.complete(tasksToComplete.get(0).getId());
  	taskService.complete(tasksToComplete.get(1).getId());
  	
  	List<HistoricActivityInstance> historicActivityInstance = historyService
  			.createHistoricActivityInstanceQuery()
  			.activityId("join")
  			.processInstanceId(processInstance.getId())
  			.list();
  	
  	assertNotNull(historicActivityInstance);
  	
  	// History contains 2 entries for parallel join (one for each path arriving in the join), should contain end-time
  	assertEquals(2, historicActivityInstance.size());
  	assertNotNull(historicActivityInstance.get(0).getEndTime());
  	assertNotNull(historicActivityInstance.get(1).getEndTime());
  }
  
  
}
