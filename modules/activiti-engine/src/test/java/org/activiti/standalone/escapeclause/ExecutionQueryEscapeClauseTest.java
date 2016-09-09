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
import java.util.Map;

import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

public class ExecutionQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

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
    
    vars = new HashMap<String, Object>();
    vars.put("var1", "Two_");
    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, "Two_");
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  public void testQueryByTenantIdLike() {
    Execution execution = runtimeService.createExecutionQuery().executionTenantIdLike("%\\%%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().executionTenantIdLike("%\\_%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    Execution execution = runtimeService.createExecutionQuery().variableValueLike("var1", "%\\%%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLike("var1", "%\\_%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
  }
  
  @Test
  public void testQueryLikeIgnoreCaseByQueryVariableValue() {
    Execution execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().variableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
  }
  
  @Test
  public void testQueryLikeByQueryProcessVariableValue() {
    Execution execution = runtimeService.createExecutionQuery().processVariableValueLike("var1", "%\\%%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().processVariableValueLike("var1", "%\\_%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
  }
  
  @Test
  public void testQueryLikeIgnoreCaseByQueryProcessVariableValue() {
    Execution execution = runtimeService.createExecutionQuery().processVariableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance1.getId(), execution.getId());
    
    execution = runtimeService.createExecutionQuery().processVariableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
    assertNotNull(execution);
    assertEquals(processInstance2.getId(), execution.getId());
  }
}
