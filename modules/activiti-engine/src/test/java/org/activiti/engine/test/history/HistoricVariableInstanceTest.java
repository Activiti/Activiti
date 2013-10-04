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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceTest extends AbstractActivitiTestCase {

  @Override
  protected void initializeProcessEngine() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
    .setJdbcDriver("org.h2.Driver")
    .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
    .setJdbcUsername("sa")
    .setJdbcPassword("")
    .setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_TRUE)
    .setJobExecutorActivate(false)
    .setHistory(HistoryLevel.FULL.getKey());
    
    processEngine = processEngineConfiguration.buildProcessEngine();
  }
  
  @Deployment(resources={
    "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml",
    "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml"       
  })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertEquals("Verify credit history", verifyCreditTask.getName());
    
    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(pi.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());
    
    // Completing the task with approval, will end the subprocess and continue the original process
    taskService.complete(verifyCreditTask.getId(), CollectionUtil.singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertEquals("Prepare and Ship", prepareAndShipTask.getName());
  }
  
  @Deployment
  public void testSimple() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task userTask = taskQuery.singleResult();
    assertEquals("userTask1", userTask.getName());
    
    taskService.complete(userTask.getId(), CollectionUtil.singletonMap("myVar", "test789"));
    
    assertProcessEnded(processInstance.getId());
    
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    
    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(3, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testSimpleNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    
    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());
    
    assertEquals(4, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testParallel() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task userTask = taskQuery.singleResult();
    assertEquals("userTask1", userTask.getName());
    
    taskService.complete(userTask.getId(), CollectionUtil.singletonMap("myVar", "test789"));
    
    assertProcessEnded(processInstance.getId());
    
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());
    
    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test789", historicVariable.getTextValue());
    
    HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test456", historicVariable1.getTextValue());
    
    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(5, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testParallelNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(1, variables.size());
    
    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());
    
    assertEquals(7, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());
    
    HistoricVariableInstanceEntity historicVariable = (HistoricVariableInstanceEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test101112", historicVariable.getTextValue());
    
    HistoricVariableInstanceEntity historicVariable1 = (HistoricVariableInstanceEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test789", historicVariable1.getTextValue());
    
    assertEquals(15, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(7, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/history/HistoricVariableInstanceTest.testCallSimpleSubProcess.bpmn20.xml",
          "org/activiti/engine/test/history/simpleSubProcess.bpmn20.xml"
  })
  public void testHistoricVariableInstanceQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    assertProcessEnded(processInstance.getId());
    
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().list().size());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc().list().size());
    
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("myVar").list().size());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableNameLike("myVar1").list().size());

    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().list();
    assertEquals(4, variables.size());

    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test123").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test456").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar", "test666").list().size());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("myVar1", "test666").list().size());
    
    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(5, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"
  })
  public void testHistoricProcessVariableOnDeletion() {
    HashMap<String, Object> variables = new HashMap<String,  Object>();
    variables.put("testVar", "Hallo Christian");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    runtimeService.deleteProcessInstance(processInstance.getId(), "deleted");
    assertProcessEnded(processInstance.getId());
    
    // check that process variable is set even if the process is canceled and not ended normally
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableValueEquals("testVar", "Hallo Christian").count());
  }

  
  @Deployment(resources={"org/activiti/standalone/history/FullHistoryTest.testVariableUpdatesAreLinkedToActivity.bpmn20.xml"})
  public void testVariableUpdatesLinkedToActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ProcessWithSubProcess");
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("test", "1");    
    taskService.complete(task.getId(), variables);
    
    // now we are in the subprocess
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    variables.clear();
    variables.put("test", "2");    
    taskService.complete(task.getId(), variables);
    
    // now we are ended
    assertProcessEnded(pi.getId());
    
    // check history
    List<HistoricDetail> updates = historyService.createHistoricDetailQuery().variableUpdates().list();
    assertEquals(2, updates.size());

    Map<String, HistoricVariableUpdate> updatesMap = new HashMap<String, HistoricVariableUpdate>();
    HistoricVariableUpdate update = (HistoricVariableUpdate) updates.get(0);
    updatesMap.put((String)update.getValue(), update);
    update = (HistoricVariableUpdate) updates.get(1);
    updatesMap.put((String)update.getValue(), update);
    
    HistoricVariableUpdate update1 = updatesMap.get("1");
    HistoricVariableUpdate update2 = updatesMap.get("2");

    assertNotNull(update1.getActivityInstanceId());
    assertNotNull(update1.getExecutionId());
    HistoricActivityInstance historicActivityInstance1 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update1.getActivityInstanceId()).singleResult();
    assertEquals(historicActivityInstance1.getExecutionId(), update1.getExecutionId());
    assertEquals("usertask1", historicActivityInstance1.getActivityId());
    
    // TODO http://jira.codehaus.org/browse/ACT-1083
    assertNotNull(update2.getActivityInstanceId());
    HistoricActivityInstance historicActivityInstance2 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update2.getActivityInstanceId()).singleResult();
    assertEquals("usertask2", historicActivityInstance2.getActivityId());

    /*
     * This is OK! The variable is set on the root execution, on a execution never run through the activity, where the process instances
     * stands when calling the set Variable. But the ActivityId of this flow node is used. So the execution id's doesn't have to be equal.
     * 
     * execution id: On which execution it was set
     * activity id: in which activity was the process instance when setting the variable
     */
    assertFalse(historicActivityInstance2.getExecutionId().equals(update2.getExecutionId()));
  }  
  
  // Test for ACT-1528, which (correctly) reported that deleting any
  // historic process instance would remove ALL historic variables.
  // Yes. Real serious bug. 
  @Deployment
  public void testHistoricProcessInstanceDeleteCascadesCorrectly() {
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "value1");
    variables.put("var2", "value2");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProcess", variables);
    assertNotNull(processInstance);

    variables = new HashMap<String, Object>();
    variables.put("var3", "value3");
    variables.put("var4", "value4");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("myProcess", variables);
    assertNotNull(processInstance2);
    
    // check variables
    long count = historyService.createHistoricVariableInstanceQuery().count();
    assertEquals(4, count);

    // delete runtime execution of ONE process instance
    runtimeService.deleteProcessInstance(processInstance.getId(), "reason 1");
    historyService.deleteHistoricProcessInstance(processInstance.getId());
    
    // recheck variables
    // this is a bug: all variables was deleted after delete a history processinstance
    count = historyService.createHistoricVariableInstanceQuery().count();
    assertEquals(2, count);
    
  }

  @Deployment(resources = "org/activiti/engine/test/history/HistoricVariableInstanceTest.testSimple.bpmn20.xml")
  public void testNativeHistoricVariableInstanceQuery() {
    assertEquals("ACT_HI_VARINST", managementService.getTableName(HistoricVariableInstance.class));
    assertEquals("ACT_HI_VARINST", managementService.getTableName(HistoricVariableInstanceEntity.class));

    String tableName = managementService.getTableName(HistoricVariableInstance.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "value1");
    variables.put("var2", "value2");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc", variables);
    assertNotNull(processInstance);

    assertEquals(3, historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).list().size());

    String sqlWithConditions = baseQuerySql + " where NAME_ = #{name}";
    assertEquals("test123", historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions)
        .parameter("name", "myVar").singleResult().getValue());

    sqlWithConditions = baseQuerySql + " where NAME_ like #{name}";
    assertEquals(2, historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions)
        .parameter("name", "var%").list().size());

    // paging
    assertEquals(3, historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).listPage(0, 3).size());
    assertEquals(2, historyService.createNativeHistoricVariableInstanceQuery().sql(baseQuerySql).listPage(1, 3).size());
    assertEquals(2, historyService.createNativeHistoricVariableInstanceQuery().sql(sqlWithConditions)
        .parameter("name", "var%").listPage(0, 2).size());

  }

  @Deployment(resources = "org/activiti/engine/test/history/HistoricVariableInstanceTest.testSimple.bpmn20.xml")
  public void testNativeHistoricDetailQuery() {
    assertEquals("ACT_HI_DETAIL", managementService.getTableName(HistoricDetail.class));
    assertEquals("ACT_HI_DETAIL", managementService.getTableName(HistoricVariableUpdate.class));

    String tableName = managementService.getTableName(HistoricDetail.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("var1", "value1");
    variables.put("var2", "value2");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc", variables);
    assertNotNull(processInstance);

    assertEquals(3, historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).list().size());

    String sqlWithConditions = baseQuerySql + " where NAME_ = #{name} and TYPE_ = #{type}";
    assertNotNull(historyService.createNativeHistoricDetailQuery().sql(sqlWithConditions)
        .parameter("name", "myVar").parameter("type", "VariableUpdate").singleResult());

    sqlWithConditions = baseQuerySql + " where NAME_ like #{name}";
    assertEquals(2, historyService.createNativeHistoricDetailQuery().sql(sqlWithConditions)
        .parameter("name", "var%").list().size());

    Task task = taskService.createTaskQuery().singleResult();
    Map<String, String> formDatas = new HashMap<String, String>();
    formDatas.put("field1", "field value 1");
    formDatas.put("field2", "field value 2");
    formService.submitTaskFormData(task.getId(), formDatas);

    String countSql = "select count(*) from " + tableName + " where TYPE_ = #{type} and PROC_INST_ID_ = #{pid}";
    assertEquals(2, historyService.createNativeHistoricDetailQuery().sql(countSql)
        .parameter("type", "FormProperty").parameter("pid", processInstance.getId()).count());


    // paging
    assertEquals(3, historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).listPage(0, 3).size());
    assertEquals(3, historyService.createNativeHistoricDetailQuery().sql(baseQuerySql).listPage(1, 3).size());
    sqlWithConditions = baseQuerySql + " where TYPE_ = #{type} and PROC_INST_ID_ = #{pid}";
    assertEquals(2, historyService.createNativeHistoricDetailQuery().sql(sqlWithConditions)
        .parameter("type", "FormProperty").parameter("pid", processInstance.getId()).listPage(0, 2).size());
  }
  
  @Deployment(resources={
     "org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"
   })
   public void testChangeType() {
     ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
     TaskQuery taskQuery = taskService.createTaskQuery();
     Task task = taskQuery.singleResult();
     assertEquals("my task", task.getName());
     
     // no type change
     runtimeService.setVariable(processInstance.getId(), "firstVar", "123");
     assertEquals("123", getHistoricVariable("firstVar").getValue());
     runtimeService.setVariable(processInstance.getId(), "firstVar", "456");
     assertEquals("456", getHistoricVariable("firstVar").getValue());
     runtimeService.setVariable(processInstance.getId(), "firstVar", "789");
     assertEquals("789", getHistoricVariable("firstVar").getValue());
 
     // type is changed from text to integer and back again. same result expected(?)
     runtimeService.setVariable(processInstance.getId(), "secondVar", "123");
     assertEquals("123", getHistoricVariable("secondVar").getValue());
     runtimeService.setVariable(processInstance.getId(), "secondVar", 456);
     // there are now 2 historic variables, so the following does not work
     assertEquals(456, getHistoricVariable("secondVar").getValue()); 
     runtimeService.setVariable(processInstance.getId(), "secondVar", "789");
     // there are now 3 historic variables, so the following does not work
     assertEquals("789", getHistoricVariable("secondVar").getValue());
     
     taskService.complete(task.getId());
     
     assertProcessEnded(processInstance.getId());
   }
 
   private HistoricVariableInstance getHistoricVariable(String variableName) {
     return historyService.createHistoricVariableInstanceQuery().variableName(variableName).singleResult();
   }
   
  
}
