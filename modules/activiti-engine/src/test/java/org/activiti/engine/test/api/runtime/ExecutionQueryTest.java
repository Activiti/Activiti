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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class ExecutionQueryTest extends PluggableActivitiTestCase {
  
  private static String CONCURRENT_PROCESS_KEY = "concurrent";
  private static String SEQUENTIAL_PROCESS_KEY = "oneTaskProcess";
  private static String CONCURRENT_PROCESS_NAME = "concurrentName";
  private static String SEQUENTIAL_PROCESS_NAME = "oneTaskProcessName";
  private static String CONCURRENT_PROCESS_CATEGORY = "org.activiti.enginge.test.api.runtime.concurrent.Category";
  private static String SEQUENTIAL_PROCESS_CATEGORY = "org.activiti.enginge.test.api.runtime.Category";
  
  private List<String> concurrentProcessInstanceIds;
  private List<String> sequentialProcessInstanceIds;
  
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/concurrentExecution.bpmn20.xml")
      .deploy();

    concurrentProcessInstanceIds = new ArrayList<String>();
    sequentialProcessInstanceIds = new ArrayList<String>();
    
    for (int i = 0; i < 4; i++) {
      concurrentProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(CONCURRENT_PROCESS_KEY, "BUSINESS-KEY-" + i).getId());
    }
    sequentialProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(SEQUENTIAL_PROCESS_KEY).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }
  
  public void testQueryByProcessDefinitionKey() {
    // Concurrent process with 3 executions for each process instance
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(SEQUENTIAL_PROCESS_KEY).list().size());
  }
  
  public void testQueryByProcessDefinitionKeyIn() {
    Set<String> includeIds = new HashSet<String>();
    assertEquals(13, runtimeService.createExecutionQuery().processDefinitionKeys(includeIds).list().size());
    includeIds.add(CONCURRENT_PROCESS_KEY);
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKeys(includeIds).list().size());
    includeIds.add(SEQUENTIAL_PROCESS_KEY);
    assertEquals(13, runtimeService.createExecutionQuery().processDefinitionKeys(includeIds).list().size());
    includeIds.add("invalid");
    assertEquals(13, runtimeService.createExecutionQuery().processDefinitionKeys(includeIds).list().size());
  }
  
  public void testQueryByInvalidProcessDefinitionKey() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processDefinitionKey("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryByProcessDefinitionCategory() {
    // Concurrent process with 3 executions for each process instance
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionCategory(CONCURRENT_PROCESS_CATEGORY).list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionCategory(SEQUENTIAL_PROCESS_CATEGORY).list().size());
  }

  public void testQueryByInvalidProcessDefinitionCategory() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processDefinitionCategory("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryByProcessDefinitionName() {
    // Concurrent process with 3 executions for each process instance
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionName(CONCURRENT_PROCESS_NAME).list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionName(SEQUENTIAL_PROCESS_NAME).list().size());
  }

  public void testQueryByInvalidProcessDefinitionName() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processDefinitionName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryByProcessInstanceId() {
    for (String processInstanceId : concurrentProcessInstanceIds) {
      ExecutionQuery query =  runtimeService.createExecutionQuery().processInstanceId(processInstanceId); 
      assertEquals(3, query.list().size());
      assertEquals(3, query.count());
    }
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(sequentialProcessInstanceIds.get(0)).list().size());
  }
  
  public void testQueryByParentId() {
    // Concurrent processes fork into 2 child-executions. Should be found when parentId is used
    for (String processInstanceId : concurrentProcessInstanceIds) {
      ExecutionQuery query =  runtimeService.createExecutionQuery().parentId(processInstanceId); 
      assertEquals(2, query.list().size());
      assertEquals(2, query.count());
    }
  }
  
  public void testQueryByInvalidProcessInstanceId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processInstanceId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryExecutionId() {
    Execution execution = runtimeService.createExecutionQuery().processDefinitionKey(SEQUENTIAL_PROCESS_KEY).singleResult();
    assertNotNull(runtimeService.createExecutionQuery().executionId(execution.getId()));
  }
  
  public void testQueryByInvalidExecutionId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().executionId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByActivityId() {
    ExecutionQuery query = runtimeService.createExecutionQuery().activityId("receivePayment");
    assertEquals(4, query.list().size());
    assertEquals(4, query.count());
    
    try {
      assertNull(query.singleResult());
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByInvalidActivityId() {
  	ExecutionQuery query = runtimeService.createExecutionQuery().activityId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  /**
   * Validate fix for ACT-1896
   */
  public void testQueryByActivityIdAndBusinessKeyWithChildren() {
    ExecutionQuery query = runtimeService.createExecutionQuery().activityId("receivePayment")
    		.processInstanceBusinessKey("BUSINESS-KEY-1", true);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals("receivePayment", execution.getActivityId());
  }
  
  public void testQueryPaging() {
    assertEquals(13, runtimeService.createExecutionQuery().count());
    assertEquals(4, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(0, 4).size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(2, 1).size());
    assertEquals(10, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(1, 10).size());
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).listPage(0, 20).size());
  }
  
  public void testQuerySorting() {
    
    // 13 executions: 3 for each concurrent, 1 for the sequential
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessDefinitionKey().asc().list().size());
    
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(13, runtimeService.createExecutionQuery().orderByProcessDefinitionKey().desc().list().size());
    
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionId().asc().list().size());
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionId().desc().list().size());

    assertEquals(12,  runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).orderByProcessDefinitionKey().asc().orderByProcessInstanceId().desc().list().size());
  }
  
  public void testQueryInvalidSorting() {
    try {
      runtimeService.createExecutionQuery().orderByProcessDefinitionKey().list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
  
  public void testQueryByBusinessKey() {
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-2").list().size());
    assertEquals(0, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("NON-EXISTING").list().size());
  }  
  
  public void testQueryByBusinessKeyIncludingChildExecutions() {
    assertEquals(3, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-1", true).list().size());
    assertEquals(3, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("BUSINESS-KEY-2", true).list().size());
    assertEquals(0, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).processInstanceBusinessKey("NON-EXISTING", true).list().size());
  }  
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Test EQUAL on single string variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("stringVar", "abcdef");
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());

    // Test EQUAL on two string variables, should result in single match
    query = runtimeService.createExecutionQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Test NOT_EQUAL, should return only 1 execution
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN, should return only matching 'azerty'
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    assertNull(execution);
    
    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());
    
    // Test LESS_THAN, should return 2 results
    executions = runtimeService.createExecutionQuery().variableValueLessThan("stringVar", "abcdeg").list();
    assertEquals(2, executions.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("stringVar", "aa").count());
    
    // Test LIKE
    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLike("stringVar", "%x%").count());

    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals("azerty").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals("abcdef").list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals("notmatchinganyvalues").singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryEqualsIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("lower", "ABCDEFG");
    vars.put("upper", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Execution execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("mixed", "abcdefg").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("lower", "abcdefg").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("upper", "abcdefg").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    // Pass in non-lower-case string
    execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    // Pass in null-value, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
    
    // Test NOT equals 
    execution = runtimeService.createExecutionQuery().variableValueNotEqualsIgnoreCase("upper", "UIOP").singleResult();
    assertNotNull(execution);
    
    // Should return result when using "ABCdefg" case-insensitive while normal not-equals won't
    execution = runtimeService.createExecutionQuery().variableValueNotEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertNull(execution);
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("upper", "ABCdefg").singleResult();
    assertNotNull(execution);
    
    // Pass in null-value, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueNotEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueNotEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLike() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var1", "aaaaa");
    vars.put("var2", "bbbbb");
    vars.put("var3", "ccccc");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Execution execution = runtimeService.createExecutionQuery().variableValueLike("var1", "aa%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLike("var2", "bb%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    // Pass in null-value, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueLike("var1", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("Only string values can be used with 'like' condition", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueLike(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLikeIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("lower", "ABCDEFG");
    vars.put("upper", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Execution execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("mixed", "abcde%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("lower", "abcd%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("upper", "abcd%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    // Pass in non-lower-case string
    execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("upper", "ABCde%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    // Pass in null-value, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      execution = runtimeService.createExecutionQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLongVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    vars.put("longVar2", 67890L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("longVar", 55555L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Query on single long variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("longVar", 12345L);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());
  
    // Query on two long variables, should result in single match
    query = runtimeService.createExecutionQuery().variableValueEquals("longVar", 12345L).variableValueEquals("longVar2", 67890L);
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("longVar", 999L).singleResult();
    assertNull(execution);
    
    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("longVar", 12345L).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("longVar", 44444L).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("longVar", 55555L).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("longVar",1L).count());
    
    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar", 44444L).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar", 55555L).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("longVar",1L).count());
    
    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("longVar", 55555L).list();
    assertEquals(2, executions.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("longVar", 12345L).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("longVar",66666L).count());
 
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("longVar", 55555L).list();
    assertEquals(3, executions.size());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("longVar", 12344L).count());
    
    
    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals(55555L).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals(12345L).list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals(99999L).singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDoubleVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    vars.put("doubleVar2", 9876.54321);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 55555.5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Query on single double variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 12345.6789);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());
  
    // Query on two double variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 12345.6789).variableValueEquals("doubleVar2", 9876.54321);
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("doubleVar", 9999.99).singleResult();
    assertNull(execution);
    
    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("doubleVar", 12345.6789).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar", 44444.4444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar", 55555.5555).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("doubleVar",1.234).count());
    
    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar", 44444.4444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar", 55555.5555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("doubleVar",1.234).count());
    
    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("doubleVar", 55555.5555).list();
    assertEquals(2, executions.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("doubleVar", 12345.6789).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("doubleVar",66666.6666).count());
 
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("doubleVar", 55555.5555).list();
    assertEquals(3, executions.size());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("doubleVar", 12344.6789).count());
    
    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals(55555.5555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals(12345.6789).list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals(9999.9999).singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryIntegerVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    vars.put("integerVar2", 67890);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("integerVar", 55555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Query on single integer variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 12345);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());
  
    // Query on two integer variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 12345).variableValueEquals("integerVar2", 67890);
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Query with unexisting variable value
    execution = runtimeService.createExecutionQuery().variableValueEquals("integerVar", 9999).singleResult();
    assertNull(execution);
    
    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("integerVar", 12345).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar", 44444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar", 55555).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("integerVar",1).count());
    
    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar", 44444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar", 55555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("integerVar",1).count());
    
    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("integerVar", 55555).list();
    assertEquals(2, executions.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("integerVar", 12345).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("integerVar",66666).count());
 
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("integerVar", 55555).list();
    assertEquals(3, executions.size());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("integerVar", 12344).count());
    
    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals(55555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals(12345).list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals(99999).singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryShortVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    short shortVar = 1234;
    vars.put("shortVar", shortVar);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    short shortVar2 = 6789;
    vars = new HashMap<String, Object>();
    vars.put("shortVar", shortVar);
    vars.put("shortVar2", shortVar2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("shortVar", (short)5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Query on single short variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("shortVar", shortVar);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());
  
    // Query on two short variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("shortVar", shortVar).variableValueEquals("shortVar2", shortVar2);
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Query with unexisting variable value
    short unexistingValue = (short)9999;
    execution = runtimeService.createExecutionQuery().variableValueEquals("shortVar", unexistingValue).singleResult();
    assertNull(execution);
    
    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("shortVar", (short)1234).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar", (short)4444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar", (short)5555).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("shortVar",(short)1).count());
    
    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar", (short)4444).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar", (short)5555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("shortVar",(short)1).count());
    
    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("shortVar", (short)5555).list();
    assertEquals(2, executions.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("shortVar", (short)1234).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("shortVar",(short)6666).count());
 
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("shortVar", (short)5555).list();
    assertEquals(3, executions.size());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("shortVar", (short)1233).count());
    
    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals((short)5555).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals((short)1234).list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals((short)999).singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);
    
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Calendar cal2 = Calendar.getInstance();
    cal2.add(Calendar.SECOND, 1);
    
    Date date2 = cal2.getTime();
    vars = new HashMap<String, Object>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<String, Object>();
    vars.put("dateVar",nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);   
    
    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);    
    
    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);    
    
    // Query on single short variable, should result in 2 matches
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("dateVar", date1);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(2, executions.size());
  
    // Query on two short variables, should result in single value
    query = runtimeService.createExecutionQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
    
    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    execution = runtimeService.createExecutionQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    assertNull(execution);
    
    // Test NOT_EQUALS
    execution = runtimeService.createExecutionQuery().variableValueNotEquals("dateVar", date1).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    // Test GREATER_THAN
    execution = runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());
    
    // Test GREATER_THAN_OR_EQUAL
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    assertEquals(3, runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("dateVar",oneYearAgo.getTime()).count());
    
    // Test LESS_THAN
    executions = runtimeService.createExecutionQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    assertEquals(2, executions.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThan("dateVar", date1).count());
    assertEquals(3, runtimeService.createExecutionQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());
 
    // Test LESS_THAN_OR_EQUAL
    executions = runtimeService.createExecutionQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    assertEquals(3, executions.size());
    
    assertEquals(0, runtimeService.createExecutionQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());
    
    // Test value-only matching
    execution = runtimeService.createExecutionQuery().variableValueEquals(nextYear.getTime()).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance3.getId(), execution.getId());
    
    executions = runtimeService.createExecutionQuery().variableValueEquals(date1).list();
    assertEquals(2, executions.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(executions.get(0).getId(), executions.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    execution = runtimeService.createExecutionQuery().variableValueEquals(twoYearsLater.getTime()).singleResult();
    assertNull(execution);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
  "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testBooleanVariable() throws Exception {

  // TEST EQUALS
  HashMap<String, Object> vars = new HashMap<String, Object>();
  vars.put("booleanVar", true);
  ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

  vars = new HashMap<String, Object>();
  vars.put("booleanVar", false);
  ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

  List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", true).list();

  assertNotNull(instances);
  assertEquals(1, instances.size());
  assertEquals(processInstance1.getId(), instances.get(0).getId());

  instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", false).list();

  assertNotNull(instances);
  assertEquals(1, instances.size());
  assertEquals(processInstance2.getId(), instances.get(0).getId());
  
  // TEST NOT_EQUALS
  instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", true).list();

  assertNotNull(instances);
  assertEquals(1, instances.size());
  assertEquals(processInstance2.getId(), instances.get(0).getId());

  instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", false).list();

  assertNotNull(instances);
  assertEquals(1, instances.size());
  assertEquals(processInstance1.getId(), instances.get(0).getId());
  
  // Test value-only matching
  Execution execution = runtimeService.createExecutionQuery().variableValueEquals(true).singleResult();
  assertNotNull(execution);
  assertEquals(processInstance1.getId(), execution.getId());
  
  // Test unsupported operations
  try {
    runtimeService.createProcessInstanceQuery().variableValueGreaterThan("booleanVar", true);
    fail("Excetion expected");
  } catch(ActivitiIllegalArgumentException ae) {
    assertTextPresent("Booleans and null cannot be used in 'greater than' condition", ae.getMessage());
  }
  
  try {
    runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("booleanVar", true);
    fail("Excetion expected");
  } catch(ActivitiIllegalArgumentException ae) {
    assertTextPresent("Booleans and null cannot be used in 'greater than or equal' condition", ae.getMessage());
  }
  
  try {
    runtimeService.createProcessInstanceQuery().variableValueLessThan("booleanVar", true);
    fail("Excetion expected");
  } catch(ActivitiIllegalArgumentException ae) {
    assertTextPresent("Booleans and null cannot be used in 'less than' condition", ae.getMessage());
  }
  
  try {
    runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("booleanVar", true);
    fail("Excetion expected");
  } catch(ActivitiIllegalArgumentException ae) {
    assertTextPresent("Booleans and null cannot be used in 'less than or equal' condition", ae.getMessage());
  }
  
  runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
  runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
}
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryVariablesUpdatedToNullValue() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("booleanVar", true);
    variables.put("dateVar", new Date());
    variables.put("nullVar", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    ExecutionQuery query = runtimeService.createExecutionQuery()
      .variableValueEquals("longVar", null)
      .variableValueEquals("shortVar", null)
      .variableValueEquals("integerVar", null)
      .variableValueEquals("stringVar", null)
      .variableValueEquals("booleanVar", null)
      .variableValueEquals("dateVar", null);
    
    ExecutionQuery notQuery = runtimeService.createExecutionQuery()
    .variableValueNotEquals("longVar", null)
    .variableValueNotEquals("shortVar", null)
    .variableValueNotEquals("integerVar", null)
    .variableValueNotEquals("stringVar", null)
    .variableValueNotEquals("booleanVar", null)
    .variableValueNotEquals("dateVar", null);
    
    assertNull(query.singleResult());
    assertNotNull(notQuery.singleResult());
    
    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "booleanVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    
    Execution queryResult = query.singleResult();
    assertNotNull(queryResult);
    assertEquals(processInstance.getId(), queryResult.getId());
    assertNull(notQuery.singleResult());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryNullVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("nullVar", "notnull");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("nullVarLong", "notnull");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("nullVarDouble", "notnull");
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    vars = new HashMap<String, Object>();
    vars.put("nullVarByte", "testbytes".getBytes());
    ProcessInstance processInstance5 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    // Query on null value, should return one value
    ExecutionQuery query = runtimeService.createExecutionQuery().variableValueEquals("nullVar", null);
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(1, executions.size());
    assertEquals(processInstance1.getId(), executions.get(0).getId());
    
    // Test NOT_EQUALS null
    assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVar", null).count());
    assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarLong", null).count());
    assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarDouble", null).count());
    // When a byte-array refrence is present, the variable is not considered null
    assertEquals(1, runtimeService.createExecutionQuery().variableValueNotEquals("nullVarByte", null).count());

    
    // Test value-only matching
    Execution execution = runtimeService.createExecutionQuery().variableValueEquals(null).singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    
    // All other variable queries with null should throw exception
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThan("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than or equal' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createExecutionQuery().variableValueLessThan("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createExecutionQuery().variableValueLessThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than or equal' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createExecutionQuery().variableValueLike("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Only string values can be used with 'like' condition", ae.getMessage());
    }
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");
    
    // Test value-only matching, non-null processes exist
    execution = runtimeService.createExecutionQuery().variableValueEquals(null).singleResult();
    assertNull(execution);
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryInvalidTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("bytesVar", "test".getBytes());
    vars.put("serializableVar",new DummySerializable());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    try {
      runtimeService.createExecutionQuery()
        .variableValueEquals("bytesVar", "test".getBytes())
        .list();
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Variables of type ByteArray cannot be used to query", ae.getMessage());
    }
    
    try {
      runtimeService.createExecutionQuery()
        .variableValueEquals("serializableVar", new DummySerializable())
        .list();
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Variables of type ByteArray cannot be used to query", ae.getMessage());
    }   
  
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }
  
  public void testQueryVariablesNullNameArgument() {
    try {
      runtimeService.createExecutionQuery().variableValueEquals(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueNotEquals(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThan(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueGreaterThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueLessThan(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueLessThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createExecutionQuery().variableValueLike(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryAllVariableTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    vars.put("stringVar", "string");
    vars.put("longVar", 10L);
    vars.put("doubleVar", 1.2);
    vars.put("integerVar", 1234);
    vars.put("booleanVar", true);
    vars.put("shortVar", (short) 123);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    ExecutionQuery query = runtimeService.createExecutionQuery()
      .variableValueEquals("nullVar", null)
      .variableValueEquals("stringVar", "string")
      .variableValueEquals("longVar", 10L)
      .variableValueEquals("doubleVar", 1.2)
      .variableValueEquals("integerVar", 1234)
      .variableValueEquals("booleanVar", true)
      .variableValueEquals("shortVar", (short) 123);
    
    List<Execution> executions = query.list();
    assertNotNull(executions);
    assertEquals(1, executions.size());
    assertEquals(processInstance.getId(), executions.get(0).getId());
  
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }
  
  @Deployment(resources={
  "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testClashingValues() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 1234L);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("var", 1234);
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars2);
    
    List<Execution> executions = runtimeService.createExecutionQuery()
    .processDefinitionKey("oneTaskProcess")
    .variableValueEquals("var", 1234L)
    .list();
    
    assertEquals(1, executions.size());
    assertEquals(processInstance.getId(), executions.get(0).getProcessInstanceId());
    
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
}
  
  @Deployment
  public void testQueryBySignalSubscriptionName() {
    runtimeService.startProcessInstanceByKey("catchSignal");
    
    // it finds subscribed instances
    Execution execution = runtimeService.createExecutionQuery()
      .signalEventSubscription("alert")
      .singleResult();
    assertNotNull(execution);

    // test query for nonexisting subscription
    execution = runtimeService.createExecutionQuery()
            .signalEventSubscription("nonExisitng")
            .singleResult();
    assertNull(execution);
    
    // it finds more than one
    runtimeService.startProcessInstanceByKey("catchSignal");
    assertEquals(2, runtimeService.createExecutionQuery().signalEventSubscription("alert").count());
  }
  
  @Deployment
  public void testQueryBySignalSubscriptionNameBoundary() {
    runtimeService.startProcessInstanceByKey("signalProces");
    
    // it finds subscribed instances
    Execution execution = runtimeService.createExecutionQuery()
      .signalEventSubscription("Test signal")
      .singleResult();
    assertNotNull(execution);

    // test query for nonexisting subscription
    execution = runtimeService.createExecutionQuery()
            .signalEventSubscription("nonExisitng")
            .singleResult();
    assertNull(execution);
    
    // it finds more than one
    runtimeService.startProcessInstanceByKey("signalProces");
    assertEquals(2, runtimeService.createExecutionQuery().signalEventSubscription("Test signal").count());
  }
    
  public void testNativeQuery() {
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertEquals("ACT_RU_EXECUTION", managementService.getTableName(Execution.class));
    
    long executionCount = runtimeService.createExecutionQuery().count();
    
    assertEquals(executionCount, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).list().size());
    assertEquals(executionCount, runtimeService.createNativeExecutionQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Execution.class)).count());
  }
  
  public void testNativeQueryPaging() {
    assertEquals(5, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).listPage(1, 5).size());
    assertEquals(1, runtimeService.createNativeExecutionQuery().sql("SELECT * FROM " + managementService.getTableName(Execution.class)).listPage(2, 1).size());
  }

  @Deployment(resources={"org/activiti/engine/test/api/runtime/concurrentExecution.bpmn20.xml"})
  public void testExecutionQueryWithProcessVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("x", "parent");
    variables.put("xIgnoreCase", "PaReNt");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrent", variables);
    
    List<Execution> concurrentExecutions = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, concurrentExecutions.size());
    for (Execution execution : concurrentExecutions) {
      if (!((ExecutionEntity)execution).isProcessInstanceType()) {
        // only the concurrent executions, not the root one, would be cooler to query that directly, see https://activiti.atlassian.net/browse/ACT-1373
        runtimeService.setVariableLocal(execution.getId(), "x", "child");
        runtimeService.setVariableLocal(execution.getId(), "xIgnoreCase", "ChILD");
      }      
    }
    
    assertEquals(2, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).variableValueEquals("x", "child").count());
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).variableValueEquals("x", "parent").count());    

    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEquals("x", "parent").count());
    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueNotEquals("x", "xxx").count());
    
    // Test value-only query
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEquals("child").count());
    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEquals("parent").count());
    
    // Test ignore-case queries
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEqualsIgnoreCase("xIgnoreCase", "CHILD").count());
    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueEqualsIgnoreCase("xIgnoreCase", "PARENT").count());   
    
    // Test ignore-case queries
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueNotEqualsIgnoreCase("xIgnoreCase", "paRent").count());
    assertEquals(3, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).processVariableValueNotEqualsIgnoreCase("xIgnoreCase", "chilD").count());  
    
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/executionLocalization.bpmn20.xml"})
  public void testLocalizeExecution() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionLocalization");

    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertNull(execution.getName());
        assertNull(execution.getDescription());
        
      } else if (execution.getParentId().equals(execution.getProcessInstanceId())){
        assertNull(execution.getName());
        assertNull(execution.getDescription());
      }
    }

    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
    dynamicBpmnService.changeLocalizationName("en-GB", "executionLocalization", "Process Name 'en-GB'", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-GB", "executionLocalization", "Process Description 'en-GB'", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    dynamicBpmnService.changeLocalizationName("en", "executionLocalization", "Process Name 'en'", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "executionLocalization", "Process Description 'en'", infoNode);
    
    dynamicBpmnService.changeLocalizationName("en-GB", "subTask", "Sub task Name 'en-GB'", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-GB", "subTask", "Sub task Description 'en-GB'", infoNode);
    
    dynamicBpmnService.changeLocalizationName("en", "subTask", "Sub task Name 'en'", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "subTask", "Sub task Description 'en'", infoNode);
    
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    String subProcessId = null;
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertNull(execution.getName());
        assertNull(execution.getDescription());
        
      } else if (execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertNull(execution.getName());
        assertNull(execution.getDescription());
        subProcessId = execution.getId();
      }
    }

    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).locale("es").list();
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertEquals("Nombre del proceso", execution.getName());
        assertEquals("Descripción del proceso", execution.getDescription());
        
      } else if (execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertEquals("Nombre Subproceso", execution.getName());
        assertEquals("Subproceso Descripción", execution.getDescription());
      }
    }
    
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).locale("en-GB").list();
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertEquals("Process Name 'en-GB'", execution.getName());
        assertEquals("Process Description 'en-GB'", execution.getDescription());
        
      } else if(execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertEquals("Sub task Name 'en-GB'", execution.getName());
        assertEquals("Sub task Description 'en-GB'", execution.getDescription());
      }
    }

    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).listPage(0,10);
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertNull(execution.getName());
        assertNull(execution.getDescription());
        
      } else if (execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertNull(execution.getName());
        assertNull(execution.getDescription());
      }
    }

    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).locale("es").listPage(0,10);
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertEquals("Nombre del proceso", execution.getName());
        assertEquals("Descripción del proceso", execution.getDescription());
        
      } else if(execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertEquals("Nombre Subproceso", execution.getName());
        assertEquals("Subproceso Descripción", execution.getDescription());
      }
    }

    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).locale("en-GB").listPage(0,10);
    assertEquals(2, executions.size());
    for (Execution execution : executions) {
      if (execution.getParentId() == null) {
        assertEquals("Process Name 'en-GB'", execution.getName());
        assertEquals("Process Description 'en-GB'", execution.getDescription());
        
      } else if(execution.getParentId().equals(execution.getProcessInstanceId())) {
        assertEquals("Sub task Name 'en-GB'", execution.getName());
        assertEquals("Sub task Description 'en-GB'", execution.getDescription());
      }
    }

    Execution execution = runtimeService.createExecutionQuery().executionId(processInstance.getId()).singleResult();
    assertNull(execution.getName());
    assertNull(execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(subProcessId).singleResult();
    assertNull(execution.getName());
    assertNull(execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(processInstance.getId()).locale("es").singleResult();
    assertEquals("Nombre del proceso", execution.getName());
    assertEquals("Descripción del proceso", execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(subProcessId).locale("es").singleResult();
    assertEquals("Nombre Subproceso", execution.getName());
    assertEquals("Subproceso Descripción", execution.getDescription());
    
    execution = runtimeService.createExecutionQuery().executionId(processInstance.getId()).locale("en-GB").singleResult();
    assertEquals("Process Name 'en-GB'", execution.getName());
    assertEquals("Process Description 'en-GB'", execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(subProcessId).locale("en-GB").singleResult();
    assertEquals("Sub task Name 'en-GB'", execution.getName());
    assertEquals("Sub task Description 'en-GB'", execution.getDescription());
    
    execution = runtimeService.createExecutionQuery().executionId(processInstance.getId()).locale("en-AU").withLocalizationFallback().singleResult();
    assertEquals("Process Name 'en'", execution.getName());
    assertEquals("Process Description 'en'", execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(subProcessId).locale("en-AU").withLocalizationFallback().singleResult();
    assertEquals("Sub task Name 'en'", execution.getName());
    assertEquals("Sub task Description 'en'", execution.getDescription());
    
    
    infoNode = dynamicBpmnService.changeLocalizationName("en-US", "executionLocalization", "Process Name 'en-US'");
    dynamicBpmnService.changeLocalizationDescription("en-US", "executionLocalization", "Process Description 'en-US'", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);
    
    dynamicBpmnService.changeLocalizationName("en-US", "subTask", "Sub task Name 'en-US'", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-US", "subTask", "Sub task Description 'en-US'", infoNode);
    
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);
    
    execution = runtimeService.createExecutionQuery().executionId(processInstance.getId()).locale("en-US").singleResult();
    assertEquals("Process Name 'en-US'", execution.getName());
    assertEquals("Process Description 'en-US'", execution.getDescription());

    execution = runtimeService.createExecutionQuery().executionId(subProcessId).locale("en-US").singleResult();
    assertEquals("Sub task Name 'en-US'", execution.getName());
    assertEquals("Sub task Description 'en-US'", execution.getDescription());
  }
}
