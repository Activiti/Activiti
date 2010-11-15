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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class HistoricActivityInstanceTest extends ActivitiInternalTestCase {
  
  @Deployment
  public void testHistoricActivityInstanceNoop() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("noopProcess");
    
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
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
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().executionId(processInstance.getId()).list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId("nonExistingProcessInstanceId").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).list().size());

    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processDefinitionId("nonExistingProcessDefinitionId").list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list().size());
    
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().unfinished().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().finished().list().size());
  }
  
  @Deployment
  public void testHistoricActivityInstanceAssignee() {    
    // Start process instance
    runtimeService.startProcessInstanceByKey("taskAssigneeProcess");

    // Get task list
    HistoricActivityInstance historicActivityInstance = historyService
      .createHistoricActivityInstanceQuery()
      .singleResult();
    
    assertEquals("kermit", historicActivityInstance.getAssignee());
  }
  
  @Deployment
  public void testSorting() {
    runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().list().size());

    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().list().size());

    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().asc().count());
  
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByExecutionId().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(1, historyService.createHistoricActivityInstanceQuery().orderByProcessInstanceId().desc().count());
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricActivityInstanceQuery().asc().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricActivityInstanceQuery().desc().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceDuration().list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
}
