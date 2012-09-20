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

package org.activiti.engine.test.api.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.runtime.ProcessInstanceQueryTest;

/**
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class HistoryServiceTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQuery() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);

    // Complete the task and check if the size is count 1
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryOrderBy() {
    // With a clean ProcessEngine, no instances should be available
    assertTrue(historyService.createHistoricProcessInstanceQuery().count() == 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());

    historyService.createHistoricTaskInstanceQuery().orderByDeleteReason().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByExecutionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceDuration().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceEndTime().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessDefinitionId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByProcessInstanceId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskAssignee().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDefinitionKey().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskDescription().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskId().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskName().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskOwner().asc().list();
    historyService.createHistoricTaskInstanceQuery().orderByTaskPriority().asc().list();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceUserIdAndActivityId() {
    identityService.setAuthenticatedUserId("johndoe");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("johndoe", historicProcessInstance.getStartUserId());
    assertEquals("theStart", historicProcessInstance.getStartActivityId());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    assertEquals("theEnd", historicProcessInstance.getEndActivityId());
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
      "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be
    // active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();

    // Completing the task with approval, will end the subprocess and continue
    // the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());

    // verify
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().contains("checkCreditProcess"));
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
      "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessDefinitionKey() {

    String processDefinitionKey = "oneTaskProcess";
    runtimeService.startProcessInstanceByKey(processDefinitionKey);
    runtimeService.startProcessInstanceByKey("orderProcess");
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey)
            .singleResult();
    assertNotNull(historicProcessInstance);
    assertTrue(historicProcessInstance.getProcessDefinitionId().startsWith(processDefinitionKey));
    assertEquals("theStart", historicProcessInstance.getStartActivityId());

    // now complete the task to end the process instance
    Task task = taskService.createTaskQuery().processDefinitionKey("checkCreditProcess").singleResult();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("creditApproved", true);
    taskService.complete(task.getId(), map);

    // and make sure the super process instance is set correctly on the
    // HistoricProcessInstance
    HistoricProcessInstance historicProcessInstanceSub = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("checkCreditProcess")
            .singleResult();
    HistoricProcessInstance historicProcessInstanceSuper = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("orderProcess")
            .singleResult();
    assertEquals(historicProcessInstanceSuper.getId(), ((HistoricProcessInstanceEntity) historicProcessInstanceSub).getSuperProcessInstanceId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml" })
  public void testHistoricProcessInstanceQueryByProcessInstanceIds() {
    HashSet<String> processInstanceIds = new HashSet<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess", i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey("oneTaskProcess2", "1").getId());

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("oneTaskProcess2", "2");

    HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertEquals(5, processInstanceQuery.count());

    List<HistoricProcessInstance> processInstances = processInstanceQuery.list();
    assertNotNull(processInstances);
    assertEquals(5, processInstances.size());

    for (HistoricProcessInstance historicProcessInstance : processInstances) {
      assertTrue(processInstanceIds.contains(historicProcessInstance.getId()));
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsEmpty() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(new HashSet<String>());
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("Set of process instance ids is empty", re.getMessage());
    }
  }

  public void testHistoricProcessInstanceQueryByProcessInstanceIdsNull() {
    try {
      historyService.createHistoricProcessInstanceQuery().processInstanceIds(null);
      fail("ActivitiException expected");
    } catch (ActivitiException re) {
      assertTextPresent("Set of process instance ids is null", re.getMessage());
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/concurrentExecution.bpmn20.xml" })
  public void testHistoricVariableInstancesOnParallelExecution() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("rootValue", "test");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("concurrent", vars);
    
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    for (Task task : tasks) {      
      Map<String, Object> variables = new HashMap<String, Object>();
      // set token local variable
      log.fine("setting variables on task "+task.getId()+", execution "+task.getExecutionId());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue1", task.getName());
      runtimeService.setVariableLocal(task.getExecutionId(), "parallelValue2", "test");
      taskService.complete(task.getId(), variables);      
    }
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("rootValue", "test").count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Receive Payment").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue1", "Ship Order").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("parallelValue2", "test").count());
  }
  
  /**
   * basically copied from {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());    

    // Test EQUAL on single string variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<HistoricProcessInstance> processInstances = query.list();
    Assert.assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Test EQUAL on two string variables, should result in single match
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    HistoricProcessInstance resultInstance = query.singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    Assert.assertNull(resultInstance);

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN, should return 2 results
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    Assert.assertEquals(2, processInstances.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    Assert.assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count());

    // Test LIKE
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());

    assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLike("stringVar", "%x%").count());

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }

  /**
   * Only do one second type, as the logic is same as in {@link ProcessInstanceQueryTest} and I do not want to duplicate
   * all test case logic here. 
   * Basically copied from {@link ProcessInstanceQueryTest}
   */
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult().getId());    

    Date date2 = Calendar.getInstance().getTime();
    vars = new HashMap<String, Object>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult().getId());    

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<String, Object>();
    vars.put("dateVar", nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance3.getId()).singleResult().getId());    

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<HistoricProcessInstance> processInstances = query.list();
    Assert.assertNotNull(processInstances);
    Assert.assertEquals(2, processInstances.size());

    // Query on two short variables, should result in single value
    query = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    HistoricProcessInstance resultInstance = query.singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance2.getId(), resultInstance.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    Assert.assertNull(resultInstance);

    // Test NOT_EQUALS
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    // Test GREATER_THAN
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    resultInstance = historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    Assert.assertNotNull(resultInstance);
    Assert.assertEquals(processInstance3.getId(), resultInstance.getId());

    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    // Test LESS_THAN
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(2, processInstances.size());

    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", date1).count());
    Assert.assertEquals(3, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());

    // Test LESS_THAN_OR_EQUAL
    processInstances = historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    Assert.assertEquals(3, processInstances.size());

    Assert.assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());

    historyService.deleteHistoricProcessInstance(processInstance1.getId());
    historyService.deleteHistoricProcessInstance(processInstance2.getId());
    historyService.deleteHistoricProcessInstance(processInstance3.getId());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricProcessInstanceTest() {    
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    runtimeService.startProcessInstanceByKey("oneTaskProcess");    
    assertEquals(1, historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricTaskInstanceTest() {    
    runtimeService.startProcessInstanceByKey("oneTaskProcess");    
    assertEquals(1, historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricTaskInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testNativeHistoricActivityInstanceTest() {    
    runtimeService.startProcessInstanceByKey("oneTaskProcess");    
    assertEquals(1, historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(HistoricProcessInstance.class)).count());
    assertEquals(1, historyService.createNativeHistoricActivityInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(HistoricProcessInstance.class)).list().size());
  }
  
}
