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

package org.activiti.engine.test.bpmn.event.compensate;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.bpmn.event.compensate.helper.SetVariablesDelegate;


/**
 * @author Daniel Meyer
 */
public class CompensateEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testCompensateSubprocess() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    
  }
  
  @Deployment
  public void testCompensateMiSubprocess() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    
  }
  
  @Deployment
  public void testCompensateScope() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
    assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
    
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCallActivityCompensationHandler.bpmn20.xml",
          "org/activiti/engine/test/bpmn/event/compensate/CompensationHandler.bpmn20.xml"          
  })
  public void testCallActivityCompensationHandler() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");

    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
    }
    
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(6, historyService.createHistoricProcessInstanceQuery()
              .count());
    }
    
  }
  
  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {
    
    // see referenced java delegates in the process definition. 
    
    SetVariablesDelegate.variablesMap.clear();    
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery().activityId("undoBookHotel").count());
    }
    
    assertProcessEnded(processInstance.getId());
    
  }
  
  public void testMultipleCompensationCatchEventsFails() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
    }    
  }
  
  public void testInvalidActivityRefFails() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testInvalidActivityRefFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("Invalid attribute value for 'activityRef':")) {
        fail("different exception expected");
      }
    }    
  }

  @Deployment(resources = {"org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testCompensationStepEndRecorded.bpmn20.xml"})
  public void testCompensationStepEndTimeRecorded() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensationStepEndRecordedProcess");
    assertProcessEnded(processInstance.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    if (!processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      return;
    }
    final HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().activityId("compensationScriptTask");
    assertEquals(1, query.count());
    final HistoricActivityInstance compensationScriptTask = query.singleResult();
    assertNotNull(compensationScriptTask);
    assertNotNull(compensationScriptTask.getEndTime());
    assertNotNull(compensationScriptTask.getDurationInMillis());
  }
}
