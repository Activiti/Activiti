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

import org.activiti.engine.ProcessEngineConfiguration;
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

    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(5, historyService.createHistoricActivityInstanceQuery()
              .activityId("undoBookHotel")
              .count());
    }
    
    runtimeService.signal(processInstance.getId());    
    assertProcessEnded(processInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
      assertEquals(6, historyService.createHistoricProcessInstanceQuery()
              .count());
    }
    
  }
  
  @Deployment
  public void testCompensateMiSubprocessVariableSnapshots() {
    
    // see referenced java delegates in the process definition. 
    
    SetVariablesDelegate.variablesMap.clear();    
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("compensateProcess");
    
    if(!processEngineConfiguration.getHistory().equals(ProcessEngineConfiguration.HISTORY_NONE)) {
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
      if(!e.getMessage().contains("multiple boundary events with compensateEventDefinition not supported on same activity")) {
        fail("different exception expected");
      }
    }    
  }
  
  public void testMultipleCompensationCatchEventsCompensationAttributeMissingFails() {    
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/compensate/CompensateEventTest.testMultipleCompensationCatchEventsCompensationAttributeMissingFails.bpmn20.xml")
        .deploy();
      fail("exception expected");
    } catch (Exception e) {
      if(!e.getMessage().contains("compensation boundary catch must be connected to element with isForCompensation=true")) {
        fail("different exception expected");
      }
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

}
