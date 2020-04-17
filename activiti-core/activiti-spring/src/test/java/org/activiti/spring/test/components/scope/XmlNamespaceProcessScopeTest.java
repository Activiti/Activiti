package org.activiti.spring.test.components.scope;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * tests the scoped beans
 * 

 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/ScopingTests-context.xml")
@Ignore
// Ignored for the moment. Josh is working on this.
public class XmlNamespaceProcessScopeTest {

  private ProcessScopeTestEngine processScopeTestEngine;

  @Autowired
  private ProcessEngine processEngine;

  @Before
  public void before() throws Throwable {
    this.processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/spring/test/components/spring-component-waiter.bpmn20.xml").deploy();

    processScopeTestEngine = new ProcessScopeTestEngine(this.processEngine);
  }

  @After
  public void after() {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testScopedProxyCreation() throws Throwable {
    processScopeTestEngine.testScopedProxyCreation();
  }

}
