package org.activiti.engine.test.api.repository;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.DeploymentQuery;

public class DeploymentQueryEscapeClauseTest extends PluggableActivitiTestCase {

  private String deploymentOneId;
  
  private String deploymentTwoId;
  
  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .name("one%")
      .category("testCategory")
      .addClasspathResource("org/activiti/engine/test/repository/one%.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .name("two_")
      .addClasspathResource("org/activiti/engine/test/repository/two_.bpmn20.xml")
      .deploy()
      .getId();
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }
  
  public void testQueryByNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("%\\%%");
    assertEquals("one%", query.singleResult().getName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createDeploymentQuery().deploymentNameLike("%\\_%");
    assertEquals("two_", query.singleResult().getName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByProcessDefinitionKeyLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKeyLike("%\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByTenantIdLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentTenantIdLike("%\\%%");
    assertEquals("One%", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createDeploymentQuery().deploymentTenantIdLike("%\\_%");
    assertEquals("Two_", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
}