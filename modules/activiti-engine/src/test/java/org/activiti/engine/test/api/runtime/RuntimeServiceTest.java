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
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Frederik Heremans
 */
public class RuntimeServiceTest extends ActivitiInternalTestCase {

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
    runtimeService.startProcessInstanceByKey("oneTaskProcess", null);
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    
    runtimeService.deleteProcessInstance(processInstance.getId(), "testing instance deletion");
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
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
  public void testFindExecutionById() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Execution execution = runtimeService.findExecutionById(processInstance.getId());
    assertNotNull(execution);
  }
  
  public void testFindExecutionByIdUnexistingId() {
    Execution execution = runtimeService.findExecutionById("unexisting");
    assertNull(execution);
  }
  
  public void testFindExecutionByIdNullId() {
    try {
      runtimeService.findExecutionById(null);      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("executionId is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testFindActiveActivityIds() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<String> activities = runtimeService.findActiveActivityIds(processInstance.getId());
    assertNotNull(activities);
    assertEquals(1, activities.size());
  }
  
  public void testFindActiveActivityIdsUnexistingExecututionId() {
    try {
      runtimeService.findActiveActivityIds("unexistingExecutionId");      
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("execution unexistingExecutionId doesn't exist", ae.getMessage());
    }
  }
  
  public void testFindActiveActivityIdsNullExecututionId() {
    try {
      runtimeService.findActiveActivityIds(null);      
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
}
