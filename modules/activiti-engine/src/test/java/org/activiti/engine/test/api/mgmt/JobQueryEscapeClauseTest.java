package org.activiti.engine.test.api.mgmt;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.JobQuery;

public class JobQueryEscapeClauseTest extends PluggableActivitiTestCase {

  private String deploymentId;
  private String deploymentTwoId;
  private String deploymentThreeId;

  protected void setUp() throws Exception {
    super.setUp();
    
    deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
        .tenantId("tenant%")
        .deploy()
        .getId();

    deploymentTwoId = repositoryService.createDeployment()
            .addClasspathResource("org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
            .tenantId("tenant_")
            .deploy()
            .getId();

    deploymentThreeId = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/mgmt/timerOnTask.bpmn20.xml")
        .tenantId("test")
        .deploy()
        .getId();

    runtimeService.startProcessInstanceByKeyAndTenantId("timerOnTask", "tenant%").getId();
    
    runtimeService.startProcessInstanceByKeyAndTenantId("timerOnTask", "tenant_").getId();
    
    runtimeService.startProcessInstanceByKeyAndTenantId("timerOnTask", "test").getId();
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
    repositoryService.deleteDeployment(deploymentThreeId, true);
    super.tearDown();
  }

  public void testQueryByTenantIdLike() {
    JobQuery query = managementService.createJobQuery().jobTenantIdLike("%\\%%");
    assertEquals("tenant%", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    query = managementService.createJobQuery().jobTenantIdLike("%\\_%");
    assertEquals("tenant_", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());

    query = managementService.createJobQuery().jobTenantIdLike("%test%");
    assertEquals("test", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
}
