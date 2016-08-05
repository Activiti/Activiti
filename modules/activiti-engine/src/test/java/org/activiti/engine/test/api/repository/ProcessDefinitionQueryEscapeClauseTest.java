package org.activiti.engine.test.api.repository;

import org.activiti.engine.repository.ProcessDefinitionQuery;

public class ProcessDefinitionQueryEscapeClauseTest extends DeploymentQueryEscapeClauseTest {

  public void testQueryByNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%\\%%");
    assertEquals("One%", query.singleResult().getName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%\\_%");
    assertEquals("Two_", query.singleResult().getName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByCategoryLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%\\_%");
    assertEquals("Examples_", query.singleResult().getCategory());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%\\_%");
    assertEquals("two_", query.singleResult().getKey());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByResourceNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%\\%%");
    assertEquals("org/activiti/engine/test/repository/one%.bpmn20.xml", query.singleResult().getResourceName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%\\_%");
    assertEquals("org/activiti/engine/test/repository/two_.bpmn20.xml", query.singleResult().getResourceName());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByTenantIdLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("%\\%%");
    assertEquals("One%", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createProcessDefinitionQuery().processDefinitionTenantIdLike("%\\_%");
    assertEquals("Two_", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionTenantIdLike("%\\%%");
    assertEquals("One%", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    
    query = repositoryService.createProcessDefinitionQuery().latestVersion().processDefinitionTenantIdLike("%\\_%");
    assertEquals("Two_", query.singleResult().getTenantId());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
}
