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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceAndVariablesQueryTest extends PluggableActivitiTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "oneTaskProcess2";
  private static String PROCESS_DEFINITION_NAME_2 = "oneTaskProcess2Name";
  private static String PROCESS_DEFINITION_CATEGORY_2 = "org.activiti.enginge.test.api.runtime.2Category";
  private static String PROCESS_DEFINITION_KEY_3 = "oneTaskProcess3";
  
  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneTaskProcess 
   * and 1 instance of oneTaskProcess2
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess3.bpmn20.xml")
      .deploy();
    
    Map<String, Object> startMap = new HashMap<String, Object>();
    startMap.put("test", "test");
    startMap.put("test2", "test2");
    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "", startMap).getId());
      if (i == 0) {
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
        taskService.complete(task.getId());
      }
    }
    startMap.clear();
    startMap.put("anothertest", 123);
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "1", startMap).getId());
    
    startMap.clear();
    startMap.put("casetest", "MyTest");
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_3, "1", startMap).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }
  
  public void testQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .variableValueEquals("anothertest", 123).singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
      
      List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().list();
      assertEquals(6, instanceList.size());
      
      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).list();
      assertEquals(4, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      assertEquals(PROCESS_DEFINITION_KEY, instanceList.get(0).getProcessDefinitionKey());
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();
      variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().finished().singleResult();
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().listPage(0, 50);
      assertEquals(6, instanceList.size());
      assertEquals(6, historyService.createHistoricProcessInstanceQuery().includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueEquals("test", "test")
          .includeProcessVariables()
          .listPage(0, 50);
      assertEquals(4, instanceList.size());
      assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("test", "test").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLike("test", "te%")
          .includeProcessVariables()
          .list();
      assertEquals(4, instanceList.size());
      assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueLike("test", "te%").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLike("test2", "te%2")
          .includeProcessVariables()
          .list();
      assertEquals(4, instanceList.size());
      assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueLike("test2", "te%2").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLikeIgnoreCase("test", "te%")
          .includeProcessVariables()
          .list();
      assertEquals(4, instanceList.size());
      assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("test", "te%").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLikeIgnoreCase("test", "t3%")
          .includeProcessVariables()
          .list();
      assertEquals(0, instanceList.size());
      assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("test", "t3%").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().listPage(0, 50);
      assertEquals(6, instanceList.size());
      assertEquals(6, historyService.createHistoricProcessInstanceQuery().includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueEquals("test", "test")
          .includeProcessVariables()
          .listPage(0, 1);
      assertEquals(1, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      assertEquals(4, historyService.createHistoricProcessInstanceQuery().variableValueEquals("test", "test").includeProcessVariables().count());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .listPage(1, 2);
      assertEquals(2, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .listPage(3, 4);
      assertEquals(1, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .listPage(4, 2);
      assertEquals(0, instanceList.size());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueEquals("test", "test")
          .includeProcessVariables()
          .orderByProcessInstanceId()
          .asc()
          .listPage(0, 50);
      assertEquals(4, instanceList.size());
    }
  }
  
  public void testQueryByprocessDefinition() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // DeploymentId
        String deploymentId = repositoryService.createDeploymentQuery().list().get(0).getId();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).deploymentId(deploymentId).singleResult();
        Map<String, Object> variableMap = processInstance.getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        assertEquals(deploymentId, processInstance.getDeploymentId());
        
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", "invalid").deploymentId(deploymentId).singleResult();
        assertNull(processInstance);
        
    	// ProcessDefinitionName
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).processDefinitionName(PROCESS_DEFINITION_NAME_2).singleResult();
        variableMap = processInstance.getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        assertEquals(PROCESS_DEFINITION_NAME_2, processInstance.getProcessDefinitionName());
        
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("test", "test").processDefinitionName(PROCESS_DEFINITION_NAME_2).singleResult();
        assertNull(processInstance);
        
        // ProcessDefinitionCategory
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).singleResult();
        variableMap = processInstance.getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("test", "test").processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).singleResult();
        assertNull(processInstance);
    }
  }
  
  public void testOrQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or().variableValueEquals("anothertest", 123).processDefinitionId("undefined").endOr().singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or()
                .variableValueEquals("anothertest", 123)
                .processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr()
              .singleResult();
      variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or()
            .variableValueEquals("anothertest", 123)
            .processDefinitionId("undefined")
          .endOr()
          .or()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .processDefinitionId("undefined")
          .endOr()
          .singleResult();
      assertNull(processInstance);
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or()
            .variableValueLikeIgnoreCase("casetest", "mytest")
            .processDefinitionId("undefined")
          .endOr()
          .singleResult();
      assertNotNull(processInstance);
      variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals("MyTest", variableMap.get("casetest"));
      
      List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionId("undefined").endOr().list();
      assertEquals(4, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or().processDefinitionKey(PROCESS_DEFINITION_KEY_2).processDefinitionId("undefined").endOr().singleResult();
      variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
      
      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or().finished().processDefinitionId("undefined").endOr().singleResult();
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .or()
          .variableValueEquals("test", "test")
          .processDefinitionId("undefined").endOr()
          .includeProcessVariables()
          .listPage(0, 50);
      assertEquals(4, instanceList.size());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .or()
          .variableValueEquals("test", "test")
          .processDefinitionId("undefined").endOr()
          .includeProcessVariables()
          .listPage(0, 1);
      assertEquals(1, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined").endOr()
          .listPage(1, 2);
      assertEquals(2, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined").endOr()
          .listPage(3, 4);
      assertEquals(1, instanceList.size());
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertEquals(2, variableMap.size());
      assertEquals("test", variableMap.get("test"));
      assertEquals("test2", variableMap.get("test2"));
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .includeProcessVariables()
          .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined").endOr()
          .listPage(4, 2);
      assertEquals(0, instanceList.size());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .or()
          .variableValueEquals("test", "test")
          .processDefinitionId("undefined").endOr()
          .includeProcessVariables()
          .orderByProcessInstanceId()
          .asc()
          .listPage(0, 50);
      assertEquals(4, instanceList.size());
      
      instanceList = historyService.createHistoricProcessInstanceQuery()
          .or()
            .variableValueEquals("test", "test")
            .processDefinitionId("undefined")
          .endOr()
          .or()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .processDefinitionId("undefined")
          .endOr()
          .includeProcessVariables()
          .orderByProcessInstanceId()
          .asc()
          .listPage(0, 50);
      assertEquals(4, instanceList.size());
    }
  }

  public void testOrQueryMultipleVariableValues() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceQuery query0 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or();
      for (int i = 0; i < 20; i++) {
          query0 = query0.variableValueEquals("anothertest", i);
      }
      query0 = query0.processDefinitionId("undefined").endOr();

      assertNull(query0.singleResult());

      HistoricProcessInstanceQuery query1 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().variableValueEquals("anothertest", 123);
      for (int i = 0; i < 20; i++) {
          query1 = query1.variableValueEquals("anothertest", i);
      }
      query1 = query1.processDefinitionId("undefined").endOr();

      HistoricProcessInstance processInstance = query1.singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));

      HistoricProcessInstanceQuery query2 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or();
      for (int i = 0; i < 20; i++) {
          query2 = query2.variableValueEquals("anothertest", i);
      }
      query2 = query2.processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr();
      assertNull(query2.singleResult());

      HistoricProcessInstanceQuery query3 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or().variableValueEquals("anothertest", 123);
      for (int i = 0; i < 20; i++) {
          query3 = query3.variableValueEquals("anothertest", i);
      }
      query3 = query3.processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr();
      variableMap = query3.singleResult().getProcessVariables();
      assertEquals(1, variableMap.size());
      assertEquals(123, variableMap.get("anothertest"));
    }
  }
  
  public void testOrQueryByprocessDefinition() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // DeploymentId
        String deploymentId = repositoryService.createDeploymentQuery().list().get(0).getId();
        HistoricProcessInstanceQuery historicprocessInstanceQuery = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").deploymentId(deploymentId).endOr();
        assertEquals(6, historicprocessInstanceQuery.list().size());
        assertEquals(6, historicprocessInstanceQuery.count());
        Map<String, Object> variableMap = historicprocessInstanceQuery.list().get(4).getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        for(HistoricProcessInstance processInstance : historicprocessInstanceQuery.list()){
        	assertEquals(deploymentId, processInstance.getDeploymentId());
        }
        
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").deploymentId("invalid").endOr().singleResult();
        assertNull(processInstance);
        
        // ProcessDefinitionName
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionName(PROCESS_DEFINITION_NAME_2).endOr().singleResult();
        variableMap = processInstance.getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        assertEquals(PROCESS_DEFINITION_NAME_2, processInstance.getProcessDefinitionName());
        
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionName("invalid").endOr().singleResult();
        assertNull(processInstance);
        
        // ProcessDefinitionCategory
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).endOr().singleResult();
        variableMap = processInstance.getProcessVariables();
        assertEquals(1, variableMap.size());
        assertEquals(123, variableMap.get("anothertest"));
        
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionCategory("invalid").endOr().singleResult();
        assertNull(processInstance);
    }
  }
}
