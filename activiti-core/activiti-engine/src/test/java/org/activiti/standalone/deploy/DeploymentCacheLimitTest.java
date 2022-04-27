/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.standalone.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.MessageFormat;

import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.repository.Deployment;


public class DeploymentCacheLimitTest extends ResourceActivitiTestCase {

  public DeploymentCacheLimitTest() {
    super("org/activiti/standalone/deploy/deployment.cache.limit.test.activiti.cfg.xml");
  }

  public void testDeploymentCacheLimit() {
    int processDefinitionCacheLimit = 3; // This is set in the configuration
                                         // above

    DefaultDeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache = (DefaultDeploymentCache<ProcessDefinitionCacheEntry>) processEngineConfiguration.getProcessDefinitionCache();
    assertThat(processDefinitionCache.size()).isEqualTo(0);

    String processDefinitionTemplate = DeploymentCacheTestUtil.readTemplateFile("/org/activiti/standalone/deploy/deploymentCacheTest.bpmn20.xml");
    for (int i = 1; i <= 5; i++) {
      repositoryService.createDeployment().addString("Process " + i + ".bpmn20.xml", MessageFormat.format(processDefinitionTemplate, i)).deploy();

      if (i < processDefinitionCacheLimit) {
        assertThat(processDefinitionCache.size()).isEqualTo(i);
      } else {
        assertThat(processDefinitionCache.size()).isEqualTo(processDefinitionCacheLimit);
      }
    }

    // Cleanup
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
