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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.runtime.ProcessInstanceQueryProperty;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ProcessInstanceQueryTest extends ActivitiInternalTestCase {

  private static String PROCESS_KEY = "oneTaskProcess";
  private static String PROCESS_KEY_2 = "oneTaskProcess2";
  
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
      .deploy();

    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_KEY).getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_KEY_2).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }
    super.tearDown();
  }

  public void testQueryNoSpecificsList() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
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
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY_2);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());
  }
  
  public void testQueryByInvalidProcessDefinitionKey() {
    assertNull(runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").list().size());
  }

  public void testQueryByProcessDefinitionKeyMultipleResults() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());

    try {
      query.singleResult();
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

  public void testQueryByInvalidProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").list().size());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().superProcessInstance(superProcessInstance.getId());
    ProcessInstance subProcessInstance = query.singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidSuperProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().superProcessInstance("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().superProcessInstance("invalid").list().size());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstance(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstance(subProcessInstance.getId()).singleResult().getId());
  }
  
  public void testQueryByInvalidSubProcessInstanceId() {
    assertNull(runtimeService.createProcessInstanceQuery().subProcessInstance("invalid").singleResult());
    assertEquals(0, runtimeService.createProcessInstanceQuery().subProcessInstance("invalid").list().size());
  }
  
  // Nested subprocess make the query complexer, hence this test
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
                           "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySuperProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstance(superProcessInstance.getId()).singleResult();
    assertNotNull(subProcessInstance);
    
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstance(subProcessInstance.getId()).singleResult();
    assertNotNull(nestedSubProcessInstance);
  }
  
  //Nested subprocess make the query complexer, hence this test
  @Deployment(resources = {"org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"})
  public void testQueryBySubProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstance(superProcessInstance.getId()).singleResult();
    assertEquals(superProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstance(subProcessInstance.getId()).singleResult().getId());
    
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstance(subProcessInstance.getId()).singleResult();
    assertEquals(subProcessInstance.getId(), runtimeService.createProcessInstanceQuery().subProcessInstance(nestedSubProcessInstance.getId()).singleResult().getId());
  }
  
  public void testQueryPaging() {
    assertEquals(2, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).listPage(0, 2).size());
    assertEquals(3, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).listPage(1, 3).size());

  }
  
  public void testQuerySorting() {
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID).asc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID).asc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY).asc().list().size());
    
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID).desc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID).desc().list().size());
    assertEquals(5, runtimeService.createProcessInstanceQuery().orderBy(ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY).desc().list().size());
    
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).orderBy(ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID).asc().list().size());
    assertEquals(4, runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY).orderBy(ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID).desc().list().size());
  }
  
  public void testQueryInvalidSorting() {
    try {
      runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().list(); // asc - desc not called -> exception
      fail();
    }catch (ActivitiException e) {}
  }

}
