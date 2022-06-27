/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
