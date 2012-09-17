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

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricProcessVariable;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.HistoricProcessVariableEntity;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricProcessVariableTest extends AbstractActivitiTestCase {

  @Override
  protected void initializeProcessEngine() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration()
    .setJdbcDriver("org.h2.Driver")
    .setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
    .setJdbcUsername("sa")
    .setJdbcPassword("")
    .setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_TRUE)
    .setJobExecutorActivate(false)
    .setHistory(ProcessEngineConfiguration.HISTORY_FULL);
    
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
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().list();
    assertEquals(1, variables.size());
    
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());
    
    assertEquals(5, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(3, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testSimpleNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().list();
    assertEquals(1, variables.size());
    
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
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
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());
    
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test789", historicVariable.getTextValue());
    
    HistoricProcessVariableEntity historicVariable1 = (HistoricProcessVariableEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test456", historicVariable1.getTextValue());
    
    assertEquals(8, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(5, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testParallelNoWaitState() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myProc");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().list();
    assertEquals(1, variables.size());
    
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("test456", historicVariable.getTextValue());
    
    assertEquals(7, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(2, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment
  public void testTwoSubProcessInParallelWithinSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoSubProcessInParallelWithinSubProcess");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().orderByVariableName().asc().list();
    assertEquals(2, variables.size());
    
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test101112", historicVariable.getTextValue());
    
    HistoricProcessVariableEntity historicVariable1 = (HistoricProcessVariableEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test789", historicVariable1.getTextValue());
    
    assertEquals(15, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(7, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/history/HistoricProcessVariableTest.testCallSimpleSubProcess.bpmn20.xml",
          "org/activiti/engine/test/history/simpleSubProcess.bpmn20.xml"
  })
  public void testCallSimpleSubProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().list();
    assertEquals(4, variables.size());
    
    // call activity
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test666", historicVariable.getTextValue());
    
    HistoricProcessVariableEntity historicVariable1 = (HistoricProcessVariableEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test666", historicVariable1.getTextValue());
    
    // process instance
    HistoricProcessVariableEntity historicVariable2 = (HistoricProcessVariableEntity) variables.get(2);
    assertEquals(processInstance.getId(), historicVariable2.getProcessInstanceId());
    assertEquals("myVar", historicVariable2.getName());
    assertEquals("test123", historicVariable2.getTextValue());
    
    HistoricProcessVariableEntity historicVariable3 = (HistoricProcessVariableEntity) variables.get(3);
    assertEquals(processInstance.getProcessInstanceId(), historicVariable3.getProcessInstanceId());
    assertEquals("myVar1", historicVariable3.getName());
    assertEquals("test456", historicVariable3.getTextValue());
    
    assertEquals(7, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(5, historyService.createHistoricDetailQuery().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/history/HistoricProcessVariableTest.testCallSimpleSubProcess.bpmn20.xml",
          "org/activiti/engine/test/history/simpleSubProcess.bpmn20.xml"
  })
  public void testHistoricProcessVariableQuery() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    assertProcessEnded(processInstance.getId());
    
    List<HistoricProcessVariable> variables = historyService.createHistoricProcessVariableQuery().list();
    assertEquals(4, variables.size());
    
    assertEquals(4, historyService.createHistoricProcessVariableQuery().orderByProcessInstanceId().asc().count());
    assertEquals(4, historyService.createHistoricProcessVariableQuery().orderByTime().asc().count());
    assertEquals(4, historyService.createHistoricProcessVariableQuery().orderByVariableName().asc().count());
    
    assertEquals(2, historyService.createHistoricProcessVariableQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(2, historyService.createHistoricProcessVariableQuery().variableName("myVar").count());
    assertEquals(2, historyService.createHistoricProcessVariableQuery().variableNameLike("myVar1").count());
    
    // call activity
    HistoricProcessVariableEntity historicVariable = (HistoricProcessVariableEntity) variables.get(0);
    assertEquals("myVar", historicVariable.getName());
    assertEquals("test666", historicVariable.getTextValue());
    
    HistoricProcessVariableEntity historicVariable1 = (HistoricProcessVariableEntity) variables.get(1);
    assertEquals("myVar1", historicVariable1.getName());
    assertEquals("test666", historicVariable1.getTextValue());
    
    // process instance
    HistoricProcessVariableEntity historicVariable2 = (HistoricProcessVariableEntity) variables.get(2);
    assertEquals(processInstance.getId(), historicVariable2.getProcessInstanceId());
    assertEquals("myVar", historicVariable2.getName());
    assertEquals("test123", historicVariable2.getTextValue());
    
    HistoricProcessVariableEntity historicVariable3 = (HistoricProcessVariableEntity) variables.get(3);
    assertEquals(processInstance.getProcessInstanceId(), historicVariable3.getProcessInstanceId());
    assertEquals("myVar1", historicVariable3.getName());
    assertEquals("test456", historicVariable3.getTextValue());
    
    assertEquals(7, historyService.createHistoricActivityInstanceQuery().count());
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
    assertEquals(1, historyService.createHistoricProcessVariableQuery().processInstanceId(processInstance.getId()).variableValueEquals("testVar", "Hallo Christian").count());
  }
}
