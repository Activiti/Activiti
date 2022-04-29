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
package org.activiti.spring.impl.test;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * Removes all deployments at the end of a complete test class.
 * <p>
 * Use this as follows in a Spring test:
 *

 * {@literal @}RunWith(SpringJUnit4ClassRunner.class)
 * {@literal @}TestExecutionListeners(CleanTestExecutionListener.class)
 * {@literal @}ContextConfiguration("...")
 */
public class CleanTestExecutionListener extends AbstractTestExecutionListener {

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    RepositoryService repositoryService = testContext.getApplicationContext().getBean(RepositoryService.class);
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
