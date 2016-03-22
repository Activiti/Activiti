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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class ProcessInstanceQueryTest extends PluggableActivitiTestCase {

  private static final int PROCESS_DEFINITION_KEY_DEPLOY_COUNT = 4;
  private static final int PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT = 1;
  private static final int PROCESS_DEPLOY_COUNT = PROCESS_DEFINITION_KEY_DEPLOY_COUNT + PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT;
  private static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static final String PROCESS_DEFINITION_KEY_2 = "oneTaskProcess2";
  private static final String PROCESS_DEFINITION_NAME = "oneTaskProcessName";
  private static final String PROCESS_DEFINITION_NAME_2 = "oneTaskProcess2Name";
  private static final String PROCESS_DEFINITION_CATEGORY = "org.activiti.enginge.test.api.runtime.Category";
  private static final String PROCESS_DEFINITION_CATEGORY_2 = "org.activiti.enginge.test.api.runtime.2Category";
  
  private org.activiti.engine.repository.Deployment deployment;
  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneTaskProcess 
   * and 1 instance of oneTaskProcess2
   */
  protected void setUp() throws Exception {
    super.setUp();
    deployment = repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml")
      .deploy();

    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < PROCESS_DEFINITION_KEY_DEPLOY_COUNT; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "1").getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  public void testQueryNoSpecificsList() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(PROCESS_DEPLOY_COUNT, query.count());
    assertEquals(PROCESS_DEPLOY_COUNT, query.list().size());
  }
  
  public void testQueryNoSpecificsSingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    try { 
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }
  
  public void testQueryByProcessDefinitionKeySingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2);
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, query.count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, query.list().size());
    assertNotNull(query.singleResult());
  }
  
  public void testQueryByInvalidProcessDefinitionKey() {
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").list().size());
  }

  public void testQueryByProcessDefinitionKeyMultipleResults() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY);
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, query.count());
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, query.list().size());

    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByProcessDefinitionKeys() {
    final Set<String> processDefinitionKeySet = new HashSet<String>(2);
    processDefinitionKeySet.add(PROCESS_DEFINITION_KEY);
    processDefinitionKeySet.add(PROCESS_DEFINITION_KEY_2);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKeys(processDefinitionKeySet);
    assertEquals(PROCESS_DEPLOY_COUNT, query.count());
    assertEquals(PROCESS_DEPLOY_COUNT, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByInvalidProcessDefinitionKeys() {
    try {
      runtimeService.createProcessInstanceQuery().processDefinitionKeys(null);
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }

    try {
      runtimeService.createProcessInstanceQuery().processDefinitionKeys(Collections.<String>emptySet());
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByProcessInstanceId() {
    for (String processInstanceId : processInstanceIds) {
      assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult());
      assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).list().size());
    }
  }
  
  public void testQueryByProcessInstanceName() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceName("new name").singleResult());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceName("new name").list().size());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceName("unexisting").singleResult());
  }
  
  public void testOrQueryByProcessInstanceName() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceName("new name").processDefinitionId("undefined").endOr().singleResult());
    assertEquals(1, runtimeService.createProcessInstanceQuery().or().processInstanceName("new name").processDefinitionId("undefined").endOr().list().size());
    
    assertNotNull(runtimeService.createProcessInstanceQuery()
        .or()
          .processInstanceName("new name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .singleResult());
    
    assertNull(runtimeService.createProcessInstanceQuery().or().processInstanceName("unexisting").processDefinitionId("undefined").endOr().singleResult());
    
    assertNull(runtimeService.createProcessInstanceQuery()
        .or()
          .processInstanceName("unexisting")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .singleResult());
  }
  
  public void testQueryByProcessInstanceNameLike() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceNameLike("% name").singleResult());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceNameLike("new name").list().size());
    
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceNameLike("%nope").singleResult());
  }
  
  public void testOrQueryByProcessInstanceNameLike() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("% name").processDefinitionId("undefined").endOr().singleResult());
    assertEquals(1, runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("new name").processDefinitionId("undefined").endOr().list().size());
    
    assertNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%nope").processDefinitionId("undefined").endOr().singleResult());
  }
  
  public void testOrQueryByProcessInstanceNameLikeIgnoreCase() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    runtimeService.setProcessInstanceName(processInstanceIds.get(1), "other Name!");
    
    // Runtime
    assertEquals(2, runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%name%").processDefinitionId("undefined").endOr().list().size());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%name%").list().size());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NAME%").list().size());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NaM%").list().size());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%the%").list().size());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("new%").list().size());
    assertNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%nope").processDefinitionId("undefined").endOr().singleResult());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertEquals(2, historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%name%").processDefinitionId("undefined").endOr().list().size());
      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%name%").list().size());
      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NAME%").list().size());
      assertEquals(2, historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NaM%").list().size());
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%the%").list().size());
      assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("new%").list().size());
      assertNull(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%nope").processDefinitionId("undefined").endOr().singleResult());
    }
  }
  
  public void testQueryByBusinessKeyAndProcessDefinitionKey() {
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("2", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("3", PROCESS_DEFINITION_KEY).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1", PROCESS_DEFINITION_KEY_2).count());
  }
  
  public void testQueryByBusinessKey() {
    assertEquals(1, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0").count());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1").count());
  }
  
  public void testQueryByInvalidBusinessKey() {
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("invalid").count());
    
    try {
      runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(null).count();
      fail();
    } catch(ActivitiIllegalArgumentException e) {
      
    }
  }

  public void testQueryByProcessDefinitionId() {
    final ProcessDefinition processDefinition1 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    ProcessInstanceQuery query1 = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition1.getId());
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, query1.count());
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, query1.list().size());
    try {
      query1.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }

    final ProcessDefinition processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();
    ProcessInstanceQuery query2 = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition2.getId());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, query2.count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, query2.list().size());
    assertNotNull(query2.singleResult());
  }

  public void testQueryByProcessDefinitionIds() {
    final ProcessDefinition processDefinition1 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    final ProcessDefinition processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();

    final Set<String> processDefinitionIdSet = new HashSet<String>(2);
    processDefinitionIdSet.add(processDefinition1.getId());
    processDefinitionIdSet.add(processDefinition2.getId());

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionIds(processDefinitionIdSet);
    assertEquals(PROCESS_DEPLOY_COUNT, query.count());
    assertEquals(PROCESS_DEPLOY_COUNT, query.list().size());
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByInvalidProcessDefinitionIds() {
    try {
      runtimeService.createProcessInstanceQuery().processDefinitionIds(null);
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }

    try {
      runtimeService.createProcessInstanceQuery().processDefinitionIds(Collections.<String>emptySet());
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByProcessDefinitionCategory() {
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY).count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).count());
  }
  
  public void testOrQueryByProcessDefinitionCategory() {
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY).processDefinitionId("undefined").endOr().count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).processDefinitionId("undefined").endOr().count());
  }

  public void testQueryByProcessDefinitionName() {
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionName(PROCESS_DEFINITION_NAME).count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionName(PROCESS_DEFINITION_NAME_2).count());
  }
  
  public void testOrQueryByProcessDefinitionName() {
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().processDefinitionName(PROCESS_DEFINITION_NAME).processDefinitionId("undefined").endOr().count());
    assertEquals(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().processDefinitionName(PROCESS_DEFINITION_NAME_2).processDefinitionId("undefined").endOr().count());
  }

  public void testQueryByInvalidProcessDefinitionName() {
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionName("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionName("invalid").count());
  }
  
  public void testQueryByDeploymentId() {
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().deploymentId(deployment.getId()).list();
    assertEquals(PROCESS_DEPLOY_COUNT, instances.size());
    ProcessInstance processInstance = instances.get(0);
    assertEquals(deployment.getId(), processInstance.getDeploymentId());
    assertEquals(new Integer(1), processInstance.getProcessDefinitionVersion());
    assertEquals(PROCESS_DEFINITION_KEY, processInstance.getProcessDefinitionKey());
    assertEquals("oneTaskProcessName", processInstance.getProcessDefinitionName());
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().deploymentId(deployment.getId()).count());
  }
  
  public void testQueryByDeploymentIdIn() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().deploymentIdIn(deploymentIds).list();
    assertEquals(PROCESS_DEPLOY_COUNT, instances.size());
    
    ProcessInstance processInstance = instances.get(0);
    assertEquals(deployment.getId(), processInstance.getDeploymentId());
    assertEquals(new Integer(1), processInstance.getProcessDefinitionVersion());
    assertEquals(PROCESS_DEFINITION_KEY, processInstance.getProcessDefinitionKey());
    assertEquals("oneTaskProcessName", processInstance.getProcessDefinitionName());
    
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().deploymentIdIn(deploymentIds).count());
  }
  
  public void testOrQueryByDeploymentId() {
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().or().deploymentId(deployment.getId()).processDefinitionId("undefined").endOr().list();
    assertEquals(PROCESS_DEPLOY_COUNT, instances.size());
    ProcessInstance processInstance = instances.get(0);
    assertEquals(deployment.getId(), processInstance.getDeploymentId());
    assertEquals(new Integer(1), processInstance.getProcessDefinitionVersion());
    assertEquals(PROCESS_DEFINITION_KEY, processInstance.getProcessDefinitionKey());
    assertEquals("oneTaskProcessName", processInstance.getProcessDefinitionName());
    
    instances = runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .list();
    assertEquals(4, instances.size());
    
    instances = runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .list();
    assertEquals(0, instances.size());
    
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().deploymentId(deployment.getId()).processDefinitionId("undefined").endOr().count());
    
    assertEquals(4, runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .count());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .count());
  }
  
  public void testOrQueryByDeploymentIdIn() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("undefined").endOr().list();
    assertEquals(PROCESS_DEPLOY_COUNT, instances.size());
    
    ProcessInstance processInstance = instances.get(0);
    assertEquals(deployment.getId(), processInstance.getDeploymentId());
    assertEquals(new Integer(1), processInstance.getProcessDefinitionVersion());
    assertEquals(PROCESS_DEFINITION_KEY, processInstance.getProcessDefinitionKey());
    assertEquals("oneTaskProcessName", processInstance.getProcessDefinitionName());
    
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("undefined").endOr().count());
  }
  
  public void testQueryByInvalidDeploymentId() {
    assertNull(runtimeService.createProcessInstanceQuery().deploymentId("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().deploymentId("invalid").count());
  }
  
  public void testOrQueryByInvalidDeploymentId() {
    assertNull(runtimeService.createProcessInstanceQuery().or().deploymentId("invalid").processDefinitionId("undefined").endOr().singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().or().deploymentId("invalid").processDefinitionId("undefined").endOr().count());
  }

  public void testQueryByInvalidProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").list().size());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId());
    ProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
    "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testOrQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().or().superProcessInstanceId(superProcessInstance.getId()).processDefinitionId("undefined").endOr();
    ProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidSuperProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").list().size());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
    "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testOrQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().or().superProcessInstanceId(superProcessInstance.getId()).processDefinitionId("undefined").singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().or().subProcessInstanceId(subProcessInstance.getId()).processDefinitionId("undefined").singleResult().getId());
  }
  
  public void testQueryByInvalidSubProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").list().size());
  }
  
  // Nested subprocess make the query complexer, hence this test
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);
    
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertNotNull(nestedSubProcessInstance);
  }
  
  //Nested subprocess make the query complexer, hence this test
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId());
    
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertEquals(subProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstanceId(nestedSubProcessInstance.getId()).singleResult().getId());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryWithExcludeSubprocesses() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    
    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().excludeSubprocesses(true).list();
    assertEquals(6, instanceList.size());
    
    boolean superProcessFound = false;
    boolean subProcessFound = false;
    boolean nestedSubProcessFound = false;
    for (ProcessInstance processInstance : instanceList) {
      if (processInstance.getId().equals(superProcessInstance.getId())) {
        superProcessFound = true;
      } else if (processInstance.getId().equals(subProcessInstance.getId())) {
        subProcessFound = true;
      } else if (processInstance.getId().equals(nestedSubProcessInstance.getId())) {
        nestedSubProcessFound = true;
      }
    }
    assertTrue(superProcessFound);
    assertFalse(subProcessFound);
    assertFalse(nestedSubProcessFound);
    
    instanceList = runtimeService.createProcessInstanceQuery().excludeSubprocesses(false).list();
    assertEquals(8, instanceList.size());
  }
  
  public void testQueryPaging() {
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).count());
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(0, 2).size());
    assertEquals(3, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(1, 3).size());
  }
  
  public void testQuerySorting() {
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().asc().list().size());
    
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(PROCESS_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().desc().list().size());
    
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().asc().list().size());
    assertEquals(PROCESS_DEFINITION_KEY_DEPLOY_COUNT, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().desc().list().size());
  }
  
  public void testQueryInvalidSorting() {
    try {
      runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().list(); // asc - desc not called -> exception
      fail();
    }catch (ActivitiIllegalArgumentException e) {}
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Test EQUAL on two string variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    assertNull(resultInstance);
    
    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count());
    
    // Test LESS_THAN, should return 2 results
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    assertEquals(2, processInstances.size());
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count());
    
    // Test LIKE
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "a%").count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%x%").count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("azerty").singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals("abcdef").list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("notmatchinganyvalues").singleResult();
    assertNull(resultInstance);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two long variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L).variableValueEquals("longVar2", 67890L);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 999L).singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 44444L).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 55555L).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar",1L).count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 44444L).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 55555L).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar",1L).count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 55555L).list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 12345L).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar",66666L).count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 55555L).list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12344L).count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555L).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345L).list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(999L).singleResult();
    assertNull(resultInstance);
    
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two double variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789).variableValueEquals("doubleVar2", 9876.54321);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 9999.99).singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("doubleVar", 12345.6789).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 44444.4444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 55555.5555).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar",1.234).count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 44444.4444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 55555.5555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar",1.234).count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 55555.5555).list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 12345.6789).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar",66666.6666).count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 55555.5555).list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 12344.6789).count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555.5555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345.6789).list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(999.999).singleResult();
    assertNull(resultInstance);
    
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two integer variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345).variableValueEquals("integerVar2", 67890);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 9999).singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("integerVar", 12345).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 44444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 55555).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar",1).count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 44444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 55555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar",1).count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 55555).list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 12345).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar",66666).count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 55555).list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 12344).count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345).list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(9999).singleResult();
    assertNull(resultInstance);
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testOrQueryIntegerVariable() {
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().or().variableValueEquals("integerVar", 12345).processDefinitionId("undefined").endOr();
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
    
    query = runtimeService.createProcessInstanceQuery()
        .or()
          .variableValueEquals("integerVar", 12345)
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("undefined")
        .endOr();
    processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two integer variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345).or().variableValueEquals("integerVar2", 67890).processDefinitionId("undefined").endOr();
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals("integerVar", 9999).processDefinitionId("undefined").endOr().singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueNotEquals("integerVar", 12345).processDefinitionId("undefined").endOr().singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar", 44444).processDefinitionId("undefined").endOr().singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery()
        .or()
          .variableValueGreaterThan("integerVar", 44444)
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("undefined")
        .endOr()
        .singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar", 55555).processDefinitionId("undefined").endOr().count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar",1).processDefinitionId("undefined").endOr().count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar", 44444).processDefinitionId("undefined").endOr().singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar", 55555).processDefinitionId("undefined").endOr().singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar",1).processDefinitionId("undefined").endOr().count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar", 55555).processDefinitionId("undefined").endOr().list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar", 12345).processDefinitionId("undefined").endOr().count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar",66666).processDefinitionId("undefined").endOr().count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueLessThanOrEqual("integerVar", 55555).processDefinitionId("undefined").endOr().list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().or().variableValueLessThanOrEqual("integerVar", 12344).processDefinitionId("undefined").endOr().count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals(55555).processDefinitionId("undefined").endOr().singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueEquals(12345).processDefinitionId("undefined").endOr().list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals(9999).processDefinitionId("undefined").endOr().singleResult();
    assertNull(resultInstance);
    
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar).variableValueEquals("shortVar2", shortVar2);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    short unexistingValue = (short)9999;
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", unexistingValue).singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("shortVar", (short)1234).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short)4444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short)5555).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar",(short)1).count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short)4444).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short)5555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar",(short)1).count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short)5555).list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short)1234).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar",(short)6666).count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short)5555).list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short)1233).count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals((short) 5555).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals((short)1234).list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals((short) 999).singleResult();
    assertNull(resultInstance);
    
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(2, processInstances.size());
  
    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    ProcessInstance resultInstance = query.singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance2.getId(), resultInstance.getId());
    
    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    assertNull(resultInstance);
    
    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count());
    
    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar",oneYearAgo.getTime()).count());
    
    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    assertEquals(2, processInstances.size());
    
    List<String> expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", date1).count());
    assertEquals(3, runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count());
  
    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    assertEquals(3, processInstances.size());
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count());
    
    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(nextYear.getTime()).singleResult();
    assertNotNull(resultInstance);
    assertEquals(processInstance3.getId(), resultInstance.getId());
    
    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(date1).list();
    assertEquals(2, processInstances.size());
    expecedIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(Arrays.asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertTrue(ids.isEmpty());
    
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(twoYearsLater.getTime()).singleResult();
    assertNull(resultInstance);
    
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
    instances = runtimeService.createProcessInstanceQuery().variableValueEquals(true).list();
    assertNotNull(instances);
    assertEquals(1, instances.size());
    assertEquals(processInstance1.getId(), instances.get(0).getId());
    
    
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
    
    // Test value-only matching, no results present
    instances = runtimeService.createProcessInstanceQuery().variableValueEquals(true).list();
    assertNotNull(instances);
    assertEquals(0, instances.size());
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
    variables.put("dateVar", new Date());
    variables.put("booleanVar", true);
    variables.put("nullVar", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .variableValueEquals("longVar", null)
      .variableValueEquals("shortVar", null)
      .variableValueEquals("integerVar", null)
      .variableValueEquals("stringVar", null)
      .variableValueEquals("booleanVar", null)
      .variableValueEquals("dateVar", null);
    
    ProcessInstanceQuery notQuery = runtimeService.createProcessInstanceQuery()
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
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "booleanVar", null);
    
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("nullVar", null);
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(1, processInstances.size());
    assertEquals(processInstance1.getId(), processInstances.get(0).getId());
    
    // Test NOT_EQUALS null
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarLong", null).count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarDouble", null).count());
    // When a byte-array refrence is present, the variable is not considered null
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarByte", null).count());
    
    // Test value-only
    assertEquals(1, runtimeService.createProcessInstanceQuery().variableValueEquals(null).count());
    
    
    // All other variable queries with null should throw exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'greater than or equal' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Booleans and null cannot be used in 'less than or equal' condition", ae.getMessage());
    }
    
    try {
      runtimeService.createProcessInstanceQuery().variableValueLike("nullVar", null);
      fail("Excetion expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Only string values can be used with 'like' condition", ae.getMessage());
    }
    
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");
    
    // Test value-only, no more null-variables exist
    assertEquals(0, runtimeService.createProcessInstanceQuery().variableValueEquals(null).count());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryEqualsIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("upper", "ABCDEFG");
    vars.put("lower", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    ProcessInstance instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("mixed", "abcdefg").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("lower", "abcdefg").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "abcdefg").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    // Pass in non-lower-case string
    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    // Pass in null-value, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
    
    // Test NOT equals 
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", "UIOP").singleResult();
    assertNotNull(instance);
    
    // Should return result when using "ABCdefg" case-insensitive while normal not-equals won't
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertNull(instance);
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("upper", "ABCdefg").singleResult();
    assertNotNull(instance);
    
    // Pass in null-value, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLikeIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("upper", "ABCDEFG");
    vars.put("lower", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    ProcessInstance instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("mixed", "abcd%").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("lower", "abcde%").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("upper", "abcd%").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    // Pass in non-lower-case string
    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("upper", "ABCde%").singleResult();
    assertNotNull(instance);
    assertEquals(processInstance1.getId(), instance.getId());
    
    // Pass in null-value, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", null).singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("value is null", ae.getMessage());
    }
    
    // Pass in null name, should cause exception
    try {
      runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult();
      fail("Exception expected");
    } catch(ActivitiIllegalArgumentException ae) {
      assertEquals("name is null", ae.getMessage());
    }
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryInvalidTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("bytesVar", "test".getBytes());
    vars.put("serializableVar",new DummySerializable());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
    
    try {
      runtimeService.createProcessInstanceQuery()
        .variableValueEquals("bytesVar", "test".getBytes())
        .list();
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("Variables of type ByteArray cannot be used to query", ae.getMessage());
    }
    
    try {
      runtimeService.createProcessInstanceQuery()
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
      runtimeService.createProcessInstanceQuery().variableValueEquals(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueNotEquals(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThan(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThan(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual(null, "value");
      fail("Expected exception");
    } catch(ActivitiIllegalArgumentException ae) {
      assertTextPresent("name is null", ae.getMessage());
    }   
    try {
      runtimeService.createProcessInstanceQuery().variableValueLike(null, "value");
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
    
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .variableValueEquals("nullVar", null)
      .variableValueEquals("stringVar", "string")
      .variableValueEquals("longVar", 10L)
      .variableValueEquals("doubleVar", 1.2)
      .variableValueEquals("integerVar", 1234)
      .variableValueEquals("booleanVar", true)
      .variableValueEquals("shortVar", (short) 123);
    
    List<ProcessInstance> processInstances = query.list();
    assertNotNull(processInstances);
    assertEquals(1, processInstances.size());
    assertEquals(processInstance.getId(), processInstances.get(0).getId());
  
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
      
      List<ProcessInstance> foundInstances = runtimeService.createProcessInstanceQuery()
      .processDefinitionKey("oneTaskProcess")
      .variableValueEquals("var", 1234L)
      .list();
      
      assertEquals(1, foundInstances.size());
      assertEquals(processInstance.getId(), foundInstances.get(0).getId());
      
      runtimeService.deleteProcessInstance(processInstance.getId(), "test");
      runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  public void testQueryByProcessInstanceIds() {
    Set<String> processInstanceIds = new HashSet<String>(this.processInstanceIds);

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("oneTaskProcess2", "2");
   
    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertEquals(5, processInstanceQuery.count());
    
    List<ProcessInstance> processInstances = processInstanceQuery.list();
    assertNotNull(processInstances);
    assertEquals(5, processInstances.size());
    
    for (ProcessInstance processInstance : processInstances) {
      assertTrue(processInstanceIds.contains(processInstance.getId()));
    }
  }

  public void testQueryByProcessInstanceIdsEmpty() {
    try {
      runtimeService.createProcessInstanceQuery().processInstanceIds(new HashSet<String>());
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("Set of process instance ids is empty", re.getMessage());
    }
  }

  public void testQueryByProcessInstanceIdsNull() {
    try {
      runtimeService.createProcessInstanceQuery().processInstanceIds(null);
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException re) {
      assertTextPresent("Set of process instance ids is null", re.getMessage());
    }
  }
  
  public void testNativeQuery() {
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertEquals("ACT_RU_EXECUTION", managementService.getTableName(ProcessInstance.class));
    
    long piCount = runtimeService.createProcessInstanceQuery().count();
    
    assertEquals(piCount, runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).list().size());
    assertEquals(piCount, runtimeService.createNativeProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(ProcessInstance.class)).count());
  }
  
  /**
   * Test confirming fix for ACT-1731
   */
  @Deployment(resources={
  "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testIncludeBinaryVariables() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("binaryVariable", (Object)"It is I, le binary".getBytes()));
    
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
              .includeProcessVariables().singleResult();
    assertNotNull(processInstance);
    // Query process, including variables
    byte[] bytes = (byte[]) processInstance.getProcessVariables().get("binaryVariable");
    assertEquals("It is I, le binary", new String(bytes));
  }
  
  public void testNativeQueryPaging() {
    assertEquals(5, runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).listPage(0, 5).size());
  }
  
  public void testLocalizeProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<ProcessInstance> processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, processes.size());
    assertNull(processes.get(0).getName());
    assertNull(processes.get(0).getDescription());

    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
    dynamicBpmnService.changeLocalizationName("en-GB", "oneTaskProcess", "The One Task Process 'en-GB' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-GB", "oneTaskProcess", "The One Task Process 'en-GB' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);
   
    dynamicBpmnService.changeLocalizationName("en", "oneTaskProcess", "The One Task Process 'en' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "oneTaskProcess", "The One Task Process 'en' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);
   
    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, processes.size());
    assertNull(processes.get(0).getName());
    assertNull(processes.get(0).getDescription());

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").list();
    assertEquals(1, processes.size());
    assertEquals("Nombre del proceso", processes.get(0).getName());
    assertEquals("Descripción del proceso", processes.get(0).getDescription());

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").list();
    assertEquals(1, processes.size());
    assertEquals("The One Task Process 'en-GB' localized name", processes.get(0).getName());
    assertEquals("The One Task Process 'en-GB' localized description", processes.get(0).getDescription());

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).listPage(0, 10);
    assertEquals(1, processes.size());
    assertNull(processes.get(0).getName());
    assertNull(processes.get(0).getDescription());

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").listPage(0,10);
    assertEquals(1, processes.size());
    assertEquals("Nombre del proceso", processes.get(0).getName());
    assertEquals("Descripción del proceso", processes.get(0).getDescription());

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").listPage(0, 10);
    assertEquals(1, processes.size());
    assertEquals("The One Task Process 'en-GB' localized name", processes.get(0).getName());
    assertEquals("The One Task Process 'en-GB' localized description", processes.get(0).getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(processInstance.getName());
    assertNull(processInstance.getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").singleResult();
    assertEquals("Nombre del proceso", processInstance.getName());
    assertEquals("Descripción del proceso", processInstance.getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").singleResult();
    assertEquals("The One Task Process 'en-GB' localized name", processInstance.getName());
    assertEquals("The One Task Process 'en-GB' localized description", processInstance.getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNull(processInstance.getName());
    assertNull(processInstance.getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en").singleResult();
    assertEquals("The One Task Process 'en' localized name", processInstance.getName());
    assertEquals("The One Task Process 'en' localized description", processInstance.getDescription());

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-AU").withLocalizationFallback().singleResult();
    assertEquals("The One Task Process 'en' localized name", processInstance.getName());
    assertEquals("The One Task Process 'en' localized description", processInstance.getDescription());
  }
}
