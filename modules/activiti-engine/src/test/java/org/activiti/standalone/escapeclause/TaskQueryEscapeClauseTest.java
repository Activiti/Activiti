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

import java.util.List;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class TaskQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;
  
  private Task task1;
  
  private Task task2;
  
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
    
    task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setAssignee(task1.getId(), "assignee%");
    taskService.setOwner(task1.getId(), "owner%");
    taskService.setVariableLocal(task1.getId(), "var1", "One%");
    
    task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    task2.setName("my task_");
    task2.setDescription("documentation_");
    taskService.saveTask(task2);
    taskService.setAssignee(task2.getId(), "assignee_");
    taskService.setOwner(task2.getId(), "owner_");
    taskService.setVariableLocal(task2.getId(), "var1", "Two_");
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  @Test
  public void testQueryByNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // nameLike
        Task task = taskService.createTaskQuery().taskNameLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskNameLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskNameLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskNameLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByNameLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // nameLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskNameLikeIgnoreCase("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskNameLikeIgnoreCase("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByDescriptionLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // descriptionLike
        Task task = taskService.createTaskQuery().taskDescriptionLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskDescriptionLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskDescriptionLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskDescriptionLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByDescriptionLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // descriptionLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskDescriptionLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskDescriptionLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByAssigneeLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // assigneeLike
        Task task = taskService.createTaskQuery().taskAssigneeLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskAssigneeLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        /*
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        */
    }
  }
  
  @Test
  public void testQueryByAssigneeLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // assigneeLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskAssigneeLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskAssigneeLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        /*
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        */
    }
  }
  
  @Test
  public void testQueryByOwnerLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLike
        Task task = taskService.createTaskQuery().taskOwnerLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskOwnerLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskOwnerLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskOwnerLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByOwnerLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskOwnerLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskOwnerLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceBusinessKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLike
        Task task = taskService.createTaskQuery().processInstanceBusinessKeyLike("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().processInstanceBusinessKeyLike("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceBusinessKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLike
        Task task = taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        /*
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        */
    }
  }
  
  @Test
  public void testQueryByKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskDefinitionKeyLike
        Task task = taskService.createTaskQuery().taskDefinitionKeyLike("%\\%%").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().taskDefinitionKeyLike("%\\_%").singleResult();
        assertNull(task);
        
        // orQuery
        task = taskService.createTaskQuery().or().taskDefinitionKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().or().taskDefinitionKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNull(task);
    }
  }
  
  @Test
  public void testQueryByProcessDefinitionKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLike
        Task task = taskService.createTaskQuery().processDefinitionKeyLike("%\\%%").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().processDefinitionKeyLike("%\\_%").singleResult();
        assertNull(task);
        
        // orQuery
        task = taskService.createTaskQuery().or().processDefinitionKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().or().processDefinitionKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNull(task);
    }
  }
  
  @Test
  public void testQueryByProcessDefinitionKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLikeIgnoreCase
        Task task = taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%\\%%").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%\\_%").singleResult();
        assertNull(task);
        
        // orQuery
        task = taskService.createTaskQuery().or().processDefinitionKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNull(task);
        
        task = taskService.createTaskQuery().or().processDefinitionKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNull(task);
    }
  }
  
  @Test
  public void testQueryByProcessDefinitionNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionNameLike
        List<Task> list = taskService.createTaskQuery().processDefinitionNameLike("%\\%%").orderByTaskCreateTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
        
        // orQuery
        list = taskService.createTaskQuery().or().processDefinitionNameLike("%\\%%").processDefinitionId("undefined").orderByTaskCreateTime().asc().list();
        assertEquals(2, list.size());
        assertEquals(task1.getId(), list.get(0).getId());
        assertEquals(task2.getId(), list.get(1).getId());
    }
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskVariableValueLike
        Task task = taskService.createTaskQuery().taskVariableValueLike("var1", "%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskVariableValueLike("var1", "%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskVariableValueLike("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskVariableValueLike("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
  
  public void testQueryLikeIgnoreCaseByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskVariableValueLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskVariableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().taskVariableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
        
        // orQuery
        task = taskService.createTaskQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task1.getId(), task.getId());
        
        task = taskService.createTaskQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(task);
        assertEquals(task2.getId(), task.getId());
    }
  }
}