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
package org.activiti.examples.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.VariableScopeImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Tom Baeyens
 */
public class VariablesTest extends PluggableActivitiTestCase {

  @Deployment
  public void testBasicVariableOperations() {
 
    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(928374L, variables.get("longVar"));
    assertEquals((short) 123, variables.get("shortVar"));
    assertEquals(1234, variables.get("integerVar"));
    assertEquals("coca-cola", variables.get("stringVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertEquals(8, variables.size());

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", null);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", null);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(null, variables.get("longVar"));
    assertEquals(null, variables.get("shortVar"));
    assertEquals(null, variables.get("integerVar"));
    assertEquals(null, variables.get("stringVar"));
    assertEquals(null, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(null, variables.get("serializableVar"));
    assertEquals(null, variables.get("bytesVar"));
    assertEquals(8, variables.size());

    // Update existing variable values again, and add a new variable
    runtimeService.setVariable(processInstance.getId(), "new var", "hi");
    runtimeService.setVariable(processInstance.getId(), "longVar", 9987L);
    runtimeService.setVariable(processInstance.getId(), "shortVar", (short) 456);
    runtimeService.setVariable(processInstance.getId(), "integerVar", 4567);
    runtimeService.setVariable(processInstance.getId(), "stringVar", "colgate");
    runtimeService.setVariable(processInstance.getId(), "dateVar", now);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", bytes);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("hi", variables.get("new var"));
    assertEquals(9987L, variables.get("longVar"));
    assertEquals((short)456, variables.get("shortVar"));
    assertEquals(4567, variables.get("integerVar"));
    assertEquals("colgate", variables.get("stringVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertEquals(9, variables.size());
    
    Collection<String> varFilter = new ArrayList<String>(2);
    varFilter.add("stringVar");
    varFilter.add("integerVar");
    
    Map<String, Object> filteredVariables = runtimeService.getVariables(processInstance.getId(), varFilter);
    assertEquals(2, filteredVariables.size());
    assertTrue(filteredVariables.containsKey("stringVar"));
    assertTrue(filteredVariables.containsKey("integerVar"));
    
    // Try setting the value of the variable that was initially created with value 'null'
    runtimeService.setVariable(processInstance.getId(), "nullVar", "a value");
    Object newValue = runtimeService.getVariable(processInstance.getId(), "nullVar");
    assertNotNull(newValue);
    assertEquals("a value", newValue);
  }
  
  
  public String getVariableInstanceId(String executionId, String name) {
	    return historyService.createNativeHistoricVariableInstanceQuery().sql("select id_ from act_ru_variable where EXECUTION_ID_=#{id} and NAME_=#{name}")
    			.parameter("id", executionId).parameter("name", name).singleResult().getId();

	  
  }
  
  // test case for ACT-1082
  @Deployment(resources = 
	     {"org/activiti/examples/variables/VariablesTest.testBasicVariableOperations.bpmn20.xml" })
  public void testChangeVariableType() {
 
    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(928374L, variables.get("longVar"));
    assertEquals((short) 123, variables.get("shortVar"));
    assertEquals(1234, variables.get("integerVar"));
    assertEquals("coca-cola", variables.get("stringVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertEquals(8, variables.size());
    
    
    
    // check if the id of the varible is the same or not
   
    String oldSerializableVarId =   getVariableInstanceId(processInstance.getId(), "serializableVar");
    String oldLongVar =   getVariableInstanceId(processInstance.getId(), "longVar");
    

    // Change type of serializableVar from serializable to Short
    Map<String, Object> newVariables = new HashMap<String, Object>();
    newVariables.put("serializableVar", (short) 222);
    runtimeService.setVariables(processInstance.getId(), newVariables);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals((short) 222, variables.get("serializableVar"));
    
    String newSerializableVarId =   getVariableInstanceId(processInstance.getId(), "serializableVar");
    
    assertEquals(oldSerializableVarId, newSerializableVarId);

    
    // Change type of a  longVar from Long to Short
    newVariables = new HashMap<String, Object>();
    newVariables.put("longVar", (short) 123);
    runtimeService.setVariables(processInstance.getId(), newVariables);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals((short) 123, variables.get("longVar"));
    
    String newLongVar =   getVariableInstanceId(processInstance.getId(), "longVar");
    assertEquals(oldLongVar, newLongVar);

    

  }
  
  
  // test case for ACT-1428
  @Deployment
  public void testNullVariable() {
	    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
	    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

	    Map<String, String> variables = new HashMap<String, String>();
	    variables.put("testProperty", "434");

	    formService.submitTaskFormData(task.getId(), variables);
	    String resultVar = (String) runtimeService.getVariable(processInstance.getId(), "testProperty");
	    
	    assertEquals("434", resultVar);
	    
	    task = taskService.createTaskQuery().executionId(processInstance.getId()).singleResult();
	    taskService.complete(task.getId());

	    // If no variable is given, no variable should be set and script test should throw exception
	    processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
	    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();	    
	    variables = new HashMap<String, String>();
	    try {
	    	formService.submitTaskFormData(task.getId(), variables);
	    	fail("Should throw exception as testProperty is not defined and used in Script task");
	    } catch (Exception e) {
		    runtimeService.deleteProcessInstance(processInstance.getId(), "intentional exception in script task");

	    	assertEquals("class org.activiti.engine.ActivitiException", e.getClass().toString());
	    }
	    
	    	    
	    // No we put null property, This should be put into the variable. We do not expect exceptions
	    processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess");
	    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();	    
	    variables = new HashMap<String, String>();
	    variables.put("testProperty", null);
	    
	    try {
	    	formService.submitTaskFormData(task.getId(), variables);
	    } catch (Exception e) {
	    	fail("Should not throw exception as the testProperty is defined, although null");
	    }
	    resultVar = (String) runtimeService.getVariable(processInstance.getId(), "testProperty");
	    
	    assertNull(resultVar);
	    
	    runtimeService.deleteProcessInstance(processInstance.getId(), "intentional exception in script task");
  }

}
