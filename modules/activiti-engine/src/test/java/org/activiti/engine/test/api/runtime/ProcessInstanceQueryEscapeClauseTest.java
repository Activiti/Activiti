package org.activiti.engine.test.api.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

public class ProcessInstanceQueryEscapeClauseTest extends PluggableActivitiTestCase {

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
    
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  public void testQueryByTenantIdLike() {
    // tenantIdLike
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%\\%%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().processInstanceTenantIdLike("%\\_%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
    
    // orQuery
    processInstance = runtimeService.createProcessInstanceQuery().or().processInstanceTenantIdLike("%\\%%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().or().processInstanceTenantIdLike("%\\_%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
  }
  
  public void testQueryByProcessInstanceNameLike() {
    // processInstanceNameLike
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceNameLike("%\\%%").singleResult());
    assertEquals(processInstance1.getId(), runtimeService.createProcessInstanceQuery().processInstanceNameLike("%\\%%").singleResult().getId());
    
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceNameLike("%\\_%").singleResult());
    assertEquals(processInstance2.getId(), runtimeService.createProcessInstanceQuery().processInstanceNameLike("%\\_%").singleResult().getId());
    
    // orQuery
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%\\%%").processDefinitionId("undefined").singleResult());
    assertEquals(processInstance1.getId(), runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%\\%%").processDefinitionId("undefined").singleResult().getId());
    
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%\\_%").processDefinitionId("undefined").singleResult());
    assertEquals(processInstance2.getId(), runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%\\_%").processDefinitionId("undefined").singleResult().getId());
  }
  
  public void testQueryProcessInstanceNameLikeIgnoreCase() {
    // processInstanceNameLike
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\%%").singleResult());
    assertEquals(processInstance1.getId(), runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\%%").singleResult().getId());
    
    assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\_%").singleResult());
    assertEquals(processInstance2.getId(), runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%\\_%").singleResult().getId());
    
    // orQuery
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult());
    assertEquals(processInstance1.getId(), runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult().getId());
    
    assertNotNull(runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult());
    assertEquals(processInstance2.getId(), runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult().getId());
  }
  
  @Test
  public void testQueryLikeByQueryVariableValue() {
    // queryVariableValue
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueLike("var1", "%\\%%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().variableValueLike("var1", "%\\_%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
    
    // orQuery
    processInstance = runtimeService.createProcessInstanceQuery().or().variableValueLike("var1", "%\\%%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().or().variableValueLike("var1", "%\\_%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
  }
  
  @Test
  public void testQueryLikeByQueryVariableValueIgnoreCase() {
    // queryVariableValueIgnoreCase
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
    
    // orQuery
    processInstance = runtimeService.createProcessInstanceQuery().or().variableValueLikeIgnoreCase("var1", "%\\%%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance1.getId(), processInstance.getId());
    
    processInstance = runtimeService.createProcessInstanceQuery().or().variableValueLikeIgnoreCase("var1", "%\\_%").processDefinitionId("undefined").singleResult();
    assertNotNull(processInstance);
    assertEquals(processInstance2.getId(), processInstance.getId());
  }
}
