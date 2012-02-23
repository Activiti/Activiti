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

package org.activiti.engine.test.api.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class RuntimeServiceTest extends PluggableActivitiTestCase {

  public void testStartProcessInstanceByKeyNullKey() {
    try {
      runtimeService.startProcessInstanceByKey(null);
      fail("ActivitiException expected");
    } catch (ActivitiException e) {
      // Expected exception
    }
  }
  
  public void testStartProcessInstanceByKeyUnexistingKey() {
    try {
      runtimeService.startProcessInstanceByKey("unexistingkey");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no processes deployed with key", ae.getMessage());
    }
  }
  
  public void testStartProcessInstanceByIdNullId() {
    try {
      runtimeService.startProcessInstanceById(null);
      fail("ActivitiException expected");
    } catch (ActivitiException e) {
      // Expected exception
    }
  }
  
  public void testStartProcessInstanceByIdUnexistingId() {
    try {
      runtimeService.startProcessInstanceById("unexistingId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no deployed process definition found with id", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceByIdNullVariables() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", (Map<String, Object>) null);
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void startProcessInstanceWithBusinessKey() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    
    // by key
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "123");
    assertNotNull(processInstance);
    assertEquals("123", processInstance.getBusinessKey());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    
    // by key with variables
    processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "456", CollectionUtil.singletonMap("var", "value"));
    assertNotNull(processInstance);
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals("var", runtimeService.getVariable(processInstance.getId(), "var"));
    
    // by id
    processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "789");
    assertNotNull(processInstance);
    assertEquals(3, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    
    // by id with variables
    processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "101123", CollectionUtil.singletonMap("var", "value2"));
    assertNotNull(processInstance);
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals("var", runtimeService.getVariable(processInstance.getId(), "var"));
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testNonUniqueBusinessKey() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "123");
    try {
      runtimeService.startProcessInstanceByKey("oneTaskProcess", "123");
      fail("Non-unique business key used, this should fail");
    } catch(Exception e) {
      
    }
  }
  
  // some databases might react strange on having mutiple times null for the business key
  // when the unique constraint is {processDefinitionId, businessKey}
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testMultipleNullBusinessKeys() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNull(processInstance.getBusinessKey());
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().count());
  }

  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    
    String deleteReason = "testing instance deletion";
    runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());    
    
    // test that the delete reason of the process instance shows up as delete reason of the task in history
    // ACT-848
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(processEngineConfiguration.getHistory())) {
      
      HistoricTaskInstance historicTaskInstance = historyService
              .createHistoricTaskInstanceQuery()
              .processInstanceId(processInstance.getId())
              .singleResult();
      
      assertEquals(deleteReason, historicTaskInstance.getDeleteReason());
    }    
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstanceNullReason() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    
    // Deleting without a reason should be possible
    runtimeService.deleteProcessInstance(processInstance.getId(), null);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
  }
  
  public void testDeleteProcessInstanceUnexistingId() {
    try {
      runtimeService.deleteProcessInstance("enexistingInstanceId", null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("No process instance found for id", ae.getMessage());
    }
  }
  

  public void testDeleteProcessInstanceNullId() {
    try {
      runtimeService.deleteProcessInstance(null, "test null id delete");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("processInstanceId is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testFindActiveActivityIds() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<String> activities = runtimeService.getActiveActivityIds(processInstance.getId());
    assertNotNull(activities);
    assertEquals(1, activities.size());
  }
  
  public void testFindActiveActivityIdsUnexistingExecututionId() {
    try {
      runtimeService.getActiveActivityIds("unexistingExecutionId");      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testFindActiveActivityIdsNullExecututionId() {
    try {
      runtimeService.getActiveActivityIds(null);      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  public void testSignalUnexistingExecututionId() {
    try {
      runtimeService.signal("unexistingExecutionId");      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testSignalNullExecutionId() {
    try {
      runtimeService.signal(null);      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  @Deployment
  public void testSignalWithProcessVariables() {
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignalWithProcessVariables");
    Map<String, Object> processVariables = new HashMap<String, Object>();
    processVariables.put("variable", "value");
    
    // signal the execution while passing in the variables
    runtimeService.signal(processInstance.getId(), processVariables);
    
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(variables, processVariables);
       
  }
  
  public void testGetVariablesUnexistingExecutionId() {
    try {
      runtimeService.getVariables("unexistingExecutionId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testGetVariablesNullExecutionId() {
    try {
      runtimeService.getVariables(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  public void testGetVariableUnexistingExecutionId() {
    try {
      runtimeService.getVariables("unexistingExecutionId");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testGetVariableNullExecutionId() {
    try {
      runtimeService.getVariables(null);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testGetVariableUnexistingVariableName() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Object variableValue = runtimeService.getVariable(processInstance.getId(), "unexistingVariable");
    assertNull(variableValue);
  }
  
  public void testSetVariableUnexistingExecutionId() {
    try {
      runtimeService.setVariable("unexistingExecutionId", "variableName", "value");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testSetVariableNullExecutionId() {
    try {
      runtimeService.setVariable(null, "variableName", "variableValue");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariableNullVariableName() {
    try {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.setVariable(processInstance.getId(), null, "variableValue");
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("variableName is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
  "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSetVariables() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variable1", "value1");
    vars.put("variable2", "value2");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariables(processInstance.getId(), vars);
    
    assertEquals("value1", runtimeService.getVariable(processInstance.getId(), "variable1"));
    assertEquals("value2", runtimeService.getVariable(processInstance.getId(), "variable2"));
  }
  
  @SuppressWarnings("unchecked")
  public void testSetVariablesUnexistingExecutionId() {
    try {
      runtimeService.setVariables("unexistingexecution", Collections.EMPTY_MAP);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingexecution doesn't exist", ae.getMessage());
    }
  }
  
  @SuppressWarnings("unchecked")
  public void testSetVariablesNullExecutionId() {
    try {
      runtimeService.setVariables(null, Collections.EMPTY_MAP);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchPanicSignal.bpmn20.xml"
  })
  public void testSignalEventReceived() {
    
    //////  test  signalEventReceived(String)
    
    startSignalCatchProcesses();    
    // 12, because the signal catch is a scope
    assertEquals(12, runtimeService.createExecutionQuery().count());    
    runtimeService.signalEventReceived("alert");    
    assertEquals(6, runtimeService.createExecutionQuery().count());
    runtimeService.signalEventReceived("panic");
    assertEquals(0, runtimeService.createExecutionQuery().count());
    
    //////  test  signalEventReceived(String, String)    
    startSignalCatchProcesses();    
  
    // signal the executions one at a time:
    for (int executions = 3; executions > 0; executions--) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .signalEventSubscription("alert")
        .listPage(0, 1);
      runtimeService.signalEventReceived("alert", page.get(0).getId());       
      
      assertEquals(executions-1, runtimeService.createExecutionQuery().signalEventSubscription("alert").count());  
    }
    
    for (int executions = 3; executions > 0; executions-- ) {
      List<Execution> page = runtimeService.createExecutionQuery()
        .signalEventSubscription("panic")
        .listPage(0, 1);
      runtimeService.signalEventReceived("panic", page.get(0).getId());       
      
      assertEquals(executions-1, runtimeService.createExecutionQuery().signalEventSubscription("panic").count());  
    }
    
  }
  
 public void testSignalEventReceivedNonExistingExecution() {
   try {
     runtimeService.signalEventReceived("alert", "nonexistingExecution");
     fail("exeception expected");
   }catch (ActivitiException e) {
     // this is good
     assertTrue(e.getMessage().contains("Execution 'nonexistingExecution' has not subscribed to a signal event with name 'alert'"));
   }
  }
 
 @Deployment(resources={
         "org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml"
 })
 public void testExecutionWaitingForDifferentSignal() {
   runtimeService.startProcessInstanceByKey("catchAlertSignal");
   Execution execution = runtimeService.createExecutionQuery()
     .signalEventSubscription("alert")
     .singleResult();
   try {
     runtimeService.signalEventReceived("bogusSignal", execution.getId());
     fail("exeception expected");
   }catch (ActivitiException e) {
     // this is good
     assertTrue(e.getMessage().contains("has not subscribed to a signal event with name 'bogusSignal'"));
   }
  }

  private void startSignalCatchProcesses() {
    for (int i = 0; i < 3; i++) {
      runtimeService.startProcessInstanceByKey("catchAlertSignal");
      runtimeService.startProcessInstanceByKey("catchPanicSignal");      
    }
  }
   
}
