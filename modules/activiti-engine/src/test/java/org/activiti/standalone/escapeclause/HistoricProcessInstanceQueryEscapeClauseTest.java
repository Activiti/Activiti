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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class HistoricProcessInstanceQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();
    
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var1", "One%");
    processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "One%");
    runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");
    
    vars = new HashMap<String, Object>();
    vars.put("var1", "Two_");
    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "Two_");
    runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    taskService.complete(task.getId());
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  @Test
  public void testQueryByProcessKeyNotIn(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processKeyNotIn
        List<String> processDefinitionKeyNotIn1 = new ArrayList<String>();
        processDefinitionKeyNotIn1.add("%\\%%");
        
        List<String> processDefinitionKeyNotIn2 = new ArrayList<String>();
        processDefinitionKeyNotIn2.add("%\\_%");
        
        List<String> processDefinitionKeyNotIn3 = new ArrayList<String>();
        processDefinitionKeyNotIn3.add("%");
        
        List<String> processDefinitionKeyNotIn4 = new ArrayList<String>();
        processDefinitionKeyNotIn4.add("______________");
        
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(processDefinitionKeyNotIn1);
        assertEquals(2, query.list().size());
        assertEquals(2, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(processDefinitionKeyNotIn2);
        assertEquals(2, query.list().size());
        assertEquals(2, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(processDefinitionKeyNotIn3);
        assertEquals(0, query.list().size());
        assertEquals(0, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(processDefinitionKeyNotIn4);
        assertEquals(0, query.list().size());
        assertEquals(0, query.list().size());
        
        // orQuery
        query = historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(processDefinitionKeyNotIn1).processDefinitionId("undefined");
        assertEquals(2, query.list().size());
        assertEquals(2, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(processDefinitionKeyNotIn2).processDefinitionId("undefined");
        assertEquals(2, query.list().size());
        assertEquals(2, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(processDefinitionKeyNotIn3).processDefinitionId("undefined");
        assertEquals(0, query.list().size());
        assertEquals(0, query.list().size());
        
        query = historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(processDefinitionKeyNotIn4).processDefinitionId("undefined");
        assertEquals(0, query.list().size());
        assertEquals(0, query.list().size());
    }
  }
  
  @Test
  public void testQueryByTenantIdLike() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // tenantIdLike
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceTenantIdLike("%\\%%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceTenantIdLike("%\\_%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
        
        // orQuery
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceTenantIdLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceTenantIdLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceNameLike() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceNameLike
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("%\\%%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("%\\_%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
        
        // orQuery
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
    }
  }
  
  @Test
  public void testQueryByProcessInstanceNameLikeIgnoreCase() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
    	// processInstanceNameLike
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\%%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\_%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
        
        // orQuery
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
    }
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // queryVariableValue
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("var1", "%\\%%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().variableValueLike("var1", "%\\_%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
        
        // orQuery
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().variableValueLike("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().variableValueLike("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
    }
  }
  
  @Test
  public void testQueryLikeIgnoreCaseByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // queryVariableValueIgnoreCase
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
        
        // orQuery
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().variableValueLikeIgnoreCase("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance1.getId(), historicProcessInstance.getId());
        
        historicProcessInstance = historyService.createHistoricProcessInstanceQuery().or().variableValueLikeIgnoreCase("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertNotNull(historicProcessInstance);
        assertEquals(processInstance2.getId(), historicProcessInstance.getId());
    }
  }
}