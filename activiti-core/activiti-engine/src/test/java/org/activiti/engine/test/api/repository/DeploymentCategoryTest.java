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


package org.activiti.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;

/**


 */
public class DeploymentCategoryTest extends PluggableActivitiTestCase {

  public void testDeploymentCategory() {
    String noCategoryDeploymentId = null;
    String deploymentOneId = null;
    String deploymentTwoV1Id = null;
    String deploymentTwoV2Id = null;
    String deploymentTwoNoCategory = null;

    try {
      noCategoryDeploymentId = repositoryService.createDeployment().name("0").addClasspathResource("org/activiti/engine/test/service/oneTaskProcess.bpmn20.xml").deploy().getId();

      deploymentOneId = repositoryService.createDeployment().name("1").category("one").addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml").deploy().getId();

      deploymentTwoV1Id = repositoryService.createDeployment().name("2v1").category("two").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      deploymentTwoV2Id = repositoryService.createDeployment().name("2v2").category("two").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      DeploymentQuery query = repositoryService.createDeploymentQuery();
      assertThat(query.list()).hasSize(4);

      Set<String> deploymentNames = getDeploymentNames(repositoryService.createDeploymentQuery().deploymentCategory("one").list());

      Set<String> expectedDeploymentNames = new HashSet<String>();
      expectedDeploymentNames.add("1");

      assertThat(deploymentNames).isEqualTo(expectedDeploymentNames);

      deploymentNames = getDeploymentNames(repositoryService.createDeploymentQuery().deploymentCategoryNotEquals("two").list());

      expectedDeploymentNames.add("0");

      assertThat(deploymentNames).isEqualTo(expectedDeploymentNames);

      deploymentTwoNoCategory = repositoryService.createDeployment().name("noCategory").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      Deployment deploymentNoCategory = repositoryService.createDeploymentQuery().deploymentId(deploymentTwoNoCategory).singleResult();
      assertThat(deploymentNoCategory.getCategory()).isNull();

      repositoryService.setDeploymentCategory(deploymentTwoNoCategory, "newCategory");
      deploymentNoCategory = repositoryService.createDeploymentQuery().deploymentId(deploymentTwoNoCategory).singleResult();
      assertThat(deploymentNoCategory.getCategory()).isEqualTo("newCategory");

    } finally {
      if (noCategoryDeploymentId != null)
        undeploy(noCategoryDeploymentId);
      if (deploymentOneId != null)
        undeploy(deploymentOneId);
      if (deploymentTwoV1Id != null)
        undeploy(deploymentTwoV1Id);
      if (deploymentTwoV2Id != null)
        undeploy(deploymentTwoV2Id);
      if (deploymentTwoNoCategory != null)
        undeploy(deploymentTwoNoCategory);
    }
  }

  public void testDeploymentKey() {
    String noKeyDeploymentId = null;
    String deploymentOneId = null;
    String deploymentTwoV1Id = null;
    String deploymentTwoV2Id = null;
    String deploymentTwoNoKey = null;

    try {
      noKeyDeploymentId = repositoryService.createDeployment().name("0").addClasspathResource("org/activiti/engine/test/service/oneTaskProcess.bpmn20.xml").deploy().getId();

      deploymentOneId = repositoryService.createDeployment().name("1").key("one").addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml").deploy().getId();

      deploymentTwoV1Id = repositoryService.createDeployment().name("2v1").key("two").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      deploymentTwoV2Id = repositoryService.createDeployment().name("2v2").key("two").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      DeploymentQuery query = repositoryService.createDeploymentQuery();
      assertThat(query.list()).hasSize(4);

      Set<String> deploymentNames = getDeploymentNames(repositoryService.createDeploymentQuery().deploymentKey("one").list());

      Set<String> expectedDeploymentNames = new HashSet<String>();
      expectedDeploymentNames.add("1");

      assertThat(deploymentNames).isEqualTo(expectedDeploymentNames);

      deploymentTwoNoKey = repositoryService.createDeployment().name("noCategory").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

      Deployment deploymentNoCategory = repositoryService.createDeploymentQuery().deploymentId(deploymentTwoNoKey).singleResult();
      assertThat(deploymentNoCategory.getCategory()).isNull();

      repositoryService.setDeploymentKey(deploymentTwoNoKey, "newKey");
      deploymentNoCategory = repositoryService.createDeploymentQuery().deploymentId(deploymentTwoNoKey).singleResult();
      assertThat(deploymentNoCategory.getKey()).isEqualTo("newKey");

    } finally {
      if (noKeyDeploymentId != null)
        undeploy(noKeyDeploymentId);
      if (deploymentOneId != null)
        undeploy(deploymentOneId);
      if (deploymentTwoV1Id != null)
        undeploy(deploymentTwoV1Id);
      if (deploymentTwoV2Id != null)
        undeploy(deploymentTwoV2Id);
      if (deploymentTwoNoKey != null)
        undeploy(deploymentTwoNoKey);
    }
  }

  private Set<String> getDeploymentNames(List<Deployment> deployments) {
    Set<String> deploymentNames = new HashSet<String>();
    for (Deployment deployment : deployments) {
      deploymentNames.add(deployment.getName());
    }
    return deploymentNames;
  }

  private void undeploy(String deploymentId) {
    try {
      repositoryService.deleteDeployment(deploymentId);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
