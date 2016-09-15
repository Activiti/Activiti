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
package org.activiti.standalone.escapeclause;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class HistoricTaskQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;
  
  private Task task1;
  
  private Task task2;
  
  private Task task3;
  
  private Task task4;
  
  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/engine/test/history/oneTaskProcessEscapeClauseTest.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/engine/test/history/oneTaskProcessEscapeClauseTest.bpmn20.xml")
      .deploy()
      .getId();
    
    processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", "One%", "One%");
    runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");
    
    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", "Two_", "Two_");
    runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");
    
    Map<String, Object> vars1 = new HashMap<String, Object>();
    vars1.put("var1", "One%");
    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("var1", "Two_");
    
    task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setAssignee(task1.getId(), "assignee%");
    taskService.setOwner(task1.getId(), "owner%");
    taskService.complete(task1.getId(), vars1, true);
    
    task2 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setAssignee(task2.getId(), "assignee_");
    taskService.setOwner(task2.getId(), "owner_");
    taskService.complete(task2.getId(), vars2, true);
    
    task3 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.setAssignee(task3.getId(), "assignee%");
    taskService.setOwner(task3.getId(), "owner%");
    taskService.complete(task3.getId(), vars1, true);
    
    task4 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.setAssignee(task4.getId(), "assignee_");
    taskService.setOwner(task4.getId(), "owner_");
    taskService.complete(task4.getId(), vars2, true);
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  @Test
  public void testQueryByProcessDefinitionKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLike("%\\%%").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLike("%\\_%").list();
        assertEquals(0, list.size());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().processDefinitionKeyLike("%\\%%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().or().processDefinitionKeyLike("%\\_%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
    }
  }
  
  @Test
  public void testQueryByProcessDefinitionKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("%\\%%").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("%\\_%").list();
        assertEquals(0, list.size());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().processDefinitionKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().or().processDefinitionKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
    }
  }
  
  @Test
  public void testQueryByProcessDefinitionNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionNameLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processDefinitionNameLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(4, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        assertEquals(task2.getId(), list.get(2).getId());
        assertEquals(task4.getId(), list.get(3).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().processDefinitionNameLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(4, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        assertEquals(task2.getId(), list.get(2).getId());
        assertEquals(task4.getId(), list.get(3).getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceBusinessKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKeyLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKeyLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceBusinessKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskDefinitionKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskDefinitionKeyLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskDefinitionKeyLike("%\\%%").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().taskDefinitionKeyLike("%\\_%").list();
        assertEquals(0, list.size());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKeyLike("%\\%%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskDefinitionKeyLike("%\\_%").processDefinitionId("undefined").list();
        assertEquals(0, list.size());
    }
  }
  
  @Test
  public void testQueryByTaskNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskNameLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskNameLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskNameLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskNameLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskNameLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskNameLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskNameLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskDescriptionLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskDescriptionLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskDescriptionLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskDescriptionLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskDescriptionLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLikeIgnoreCase("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskDescriptionLikeIgnoreCase("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskDeleteReasonLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // make test data
        Task task5 = taskService.newTask("task5");
        taskService.saveTask(task5);
        taskService.deleteTask(task5.getId(), "deleteReason%");
        Task task6 = taskService.newTask("task6");
        taskService.saveTask(task6);
        taskService.deleteTask(task6.getId(), "deleteReason_");
        
        // taskDeleteReasonLike
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskDeleteReasonLike("%\\%%").singleResult();
        assertNotNull(historicTask);
        assertEquals(task5.getId(), historicTask.getId());
        
        historicTask = historyService.createHistoricTaskInstanceQuery().taskDeleteReasonLike("%\\_%").singleResult();
        assertNotNull(historicTask);
        assertEquals(task6.getId(), historicTask.getId());
        
        // orQuery
        historicTask = historyService.createHistoricTaskInstanceQuery().or().taskDeleteReasonLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicTask);
        assertEquals(task5.getId(), historicTask.getId());
        
        historicTask = historyService.createHistoricTaskInstanceQuery().or().taskDeleteReasonLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicTask);
        assertEquals(task6.getId(), historicTask.getId());
        
        // clean
        historyService.deleteHistoricTaskInstance(task5.getId());
        historyService.deleteHistoricTaskInstance(task6.getId());
    }
  }
  
  @Test
  public void testQueryByTaskOwnerLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskOwnerLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskOwnerLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskOwnerLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskOwnerLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskOwnerLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskOwnerLikeIgnoreCase("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskOwnerLikeIgnoreCase("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskAssigneeLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskAssigneeLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskAssigneeLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTaskAssigneeLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskAssigneeLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLikeIgnoreCase("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskAssigneeLikeIgnoreCase("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryByTenantIdLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // tenantIdLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskTenantIdLike("%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskTenantIdLike("%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskTenantIdLike("%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskTenantIdLike("%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task3.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // variableValueLike
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskVariableValueLike("var1", "%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskVariableValueLike("var1", "%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskVariableValueLike("var1", "%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskVariableValueLike("var1", "%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
  
  public void testQueryLikeIgnoreCaseByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // variableValueLikeIgnoreCase
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("var1", "%\\%%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().taskVariableValueLikeIgnoreCase("var1", "%\\_%").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
        
        // orQuery
        list = historyService.createHistoricTaskInstanceQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\%%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task3.getId(), list.get(1).getId());
        
        list = historyService.createHistoricTaskInstanceQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\_%").processDefinitionId("undefined").orderByHistoricTaskInstanceStartTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task2.getId(), list.get(0).getId());
        assertEquals(task4.getId(), list.get(1).getId());
    }
  }
}