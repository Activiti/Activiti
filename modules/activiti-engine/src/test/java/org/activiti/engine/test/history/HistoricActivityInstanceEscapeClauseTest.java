package org.activiti.engine.test.history;

import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

public class HistoricActivityInstanceEscapeClauseTest extends PluggableActivitiTestCase {

  private String deploymentOneId;
  
  private String deploymentTwoId;
  
  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceQuery.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/engine/test/history/HistoricActivityInstanceTest.testHistoricActivityInstanceQuery.bpmn20.xml")
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
  
  public void testQueryByTenantIdLike() {
    runtimeService.startProcessInstanceByKeyAndTenantId("noopProcess", "One%");
    runtimeService.startProcessInstanceByKeyAndTenantId("noopProcess", "Two_");

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().activityId("noop").activityTenantIdLike("%\\%%");
    assertEquals("One%", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = historyService.createHistoricActivityInstanceQuery().activityId("noop").activityTenantIdLike("%\\_%");
    assertEquals("Two_", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
}