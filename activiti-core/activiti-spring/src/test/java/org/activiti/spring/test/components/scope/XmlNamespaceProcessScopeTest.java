package org.activiti.spring.test.components.scope;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * tests the scoped beans
 *

 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/ScopingTests-context.xml")
@Ignore
// Ignored for the moment. Josh is working on this.
public class XmlNamespaceProcessScopeTest {

  private ProcessScopeTestEngine processScopeTestEngine;

  @Autowired
  private ProcessEngine processEngine;

  @BeforeEach
  public void before() throws Throwable {
    this.processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/spring/test/components/spring-component-waiter.bpmn20.xml").deploy();

    processScopeTestEngine = new ProcessScopeTestEngine(this.processEngine);
  }

  @AfterEach
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
