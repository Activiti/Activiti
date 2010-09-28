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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;


/**
 * @author Joram Barrez
 */
public class ExecutionQueryTest extends ActivitiInternalTestCase {
  
  private static String CONCURRENT_PROCESS_KEY = "concurrent";
  private static String SEQUENTIAL_PROCESS_KEY = "oneTaskProcess";
  
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
      concurrentProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(CONCURRENT_PROCESS_KEY).getId());
    }
    sequentialProcessInstanceIds.add(runtimeService.startProcessInstanceByKey(SEQUENTIAL_PROCESS_KEY).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }
    super.tearDown();
  }
  
  public void testQueryByProcessDefinitionKey() {
    // Concurrent process with 3 executions for each process instance
    assertEquals(12, runtimeService.createExecutionQuery().processDefinitionKey(CONCURRENT_PROCESS_KEY).list().size());
    assertEquals(1, runtimeService.createExecutionQuery().processDefinitionKey(SEQUENTIAL_PROCESS_KEY).list().size());
  }
  
  public void testQueryByInvalidProcessDefinitionKey() {
    ExecutionQuery query = runtimeService.createExecutionQuery().processDefinitionKey("invalid");
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
  
  public void testQueryPaging() {
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

}
